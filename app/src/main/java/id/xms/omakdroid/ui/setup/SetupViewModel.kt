package id.xms.omakdroid.ui.setup

import android.content.Context
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xms.omakdroid.MainViewModel
import id.xms.omakdroid.core.SystemChecker
import id.xms.omakdroid.core.engine.NativeEngine
import id.xms.omakdroid.core.engine.RootfsPathResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class SetupViewModel : ViewModel() {
    
    // Desktop Environment Selection
    enum class DesktopEnv { XFCE, KDE, NONE }
    val selectedDE = mutableStateOf(DesktopEnv.KDE)
    
    // User Information
    val fullName = mutableStateOf("")
    val hostname = mutableStateOf("omakdroid")
    val username = mutableStateOf("")
    val password = mutableStateOf("")
    val confirmPassword = mutableStateOf("")
    
    // Localization
    val language = mutableStateOf("English")
    val timezone = mutableStateOf("Asia/Jakarta")
    
    // Execution Mode (PROOT or CHROOT)
    val executionMode = mutableStateOf(MainViewModel.ExecutionMode.PROOT)
    
    // Installation Progress
    val installProgress = mutableFloatStateOf(0f)
    val installStatusText = mutableStateOf("Preparing installation...")
    
    // Real-time APT Progress
    val currentAction = mutableStateOf("")
    val downloadSpeed = mutableStateOf("")
    val etaText = mutableStateOf("")
    
    // Progress tracking for ETA calculation
    private var lastProgressTime = 0L
    private var lastProgressPercent = 0f
    
    // Validation
    fun isUserSetupValid(): Boolean {
        return fullName.value.isNotBlank() &&
                hostname.value.isNotBlank() &&
                username.value.isNotBlank() &&
                password.value.isNotBlank() &&
                password.value == confirmPassword.value &&
                username.value.matches(Regex("^[a-z_][a-z0-9_-]*$"))
    }
    
    /**
     * Parse APT machine-readable status output (APT::Status-Fd=1)
     * Format: dlstatus:1:percent:description or pmstatus:package:percent:description
     */
    private fun parseAptStatus(line: String) {
        try {
            when {
                line.startsWith("dlstatus:") -> {
                    val parts = line.split(":")
                    if (parts.size >= 4) {
                        val percent = parts[2].toFloatOrNull() ?: return
                        val description = parts.drop(3).joinToString(":")
                        
                        installProgress.floatValue = percent / 100f
                        currentAction.value = description
                        installStatusText.value = "Downloading: $description"
                        
                        updateETA(percent)
                    }
                }
                line.startsWith("pmstatus:") -> {
                    val parts = line.split(":")
                    if (parts.size >= 4) {
                        val percent = parts[2].toFloatOrNull() ?: return
                        val description = parts.drop(3).joinToString(":")
                        
                        installProgress.floatValue = percent / 100f
                        currentAction.value = description
                        installStatusText.value = "Installing: $description"
                        
                        updateETA(percent)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("SetupViewModel", "Failed to parse APT status: $line", e)
        }
    }
    
    /**
     * Calculate download speed and ETA based on progress changes
     */
    private fun updateETA(currentPercent: Float) {
        val currentTime = System.currentTimeMillis()
        
        if (lastProgressTime > 0 && currentPercent > lastProgressPercent) {
            val timeDelta = (currentTime - lastProgressTime) / 1000.0 // seconds
            val percentDelta = currentPercent - lastProgressPercent
            
            if (timeDelta > 0 && percentDelta > 0) {
                val percentPerSecond = percentDelta / timeDelta
                val remainingPercent = 100f - currentPercent
                val etaSeconds = (remainingPercent / percentPerSecond).toInt()
                
                etaText.value = when {
                    etaSeconds < 60 -> "About ${etaSeconds}s remaining"
                    etaSeconds < 3600 -> "About ${etaSeconds / 60}m ${etaSeconds % 60}s remaining"
                    else -> "About ${etaSeconds / 3600}h ${(etaSeconds % 3600) / 60}m remaining"
                }
                
                // Estimate download speed (rough approximation)
                val speedMBps = percentPerSecond * 0.5 // Rough estimate
                downloadSpeed.value = String.format("%.1f MB/s", speedMBps)
            }
        }
        
        lastProgressTime = currentTime
        lastProgressPercent = currentPercent
    }
    
    fun resetInstallation() {
        installProgress.floatValue = 0f
        installStatusText.value = "Preparing installation..."
        currentAction.value = ""
        downloadSpeed.value = ""
        etaText.value = ""
        lastProgressTime = 0L
        lastProgressPercent = 0f
    }
    
    fun generateSetupScript(): String {
        val deCommand = when (selectedDE.value) {
            DesktopEnv.XFCE -> "apt-get install -y -o APT::Status-Fd=1 xfce4 xfce4-goodies dbus-x11"
            DesktopEnv.KDE -> "apt-get install -y -o APT::Status-Fd=1 kde-plasma-desktop dbus-x11"
            DesktopEnv.NONE -> ""
        }
        
        return """
            #!/bin/bash
            export DEBIAN_FRONTEND=noninteractive
            export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
            
            # Create User
            useradd -m -s /bin/bash ${username.value}
            echo "${username.value}:${password.value}" | chpasswd
            usermod -aG sudo ${username.value}
            
            # Force install gnupg and keyrings FIRST
            apt-get update -o APT::Status-Fd=1 -o Acquire::AllowInsecureRepositories=true -o Acquire::AllowDowngradeToInsecureRepositories=true || true
            apt-get install -y -o APT::Status-Fd=1 --allow-unauthenticated gnupg ubuntu-keyring ca-certificates sudo wget curl nano tzdata
            
            # Try to fetch keys if gnupg is now installed
            apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 871920D1991BC93C || true
            
            # Configure Timezone
            ln -fs /usr/share/zoneinfo/${timezone.value} /etc/localtime
            dpkg-reconfigure -f noninteractive tzdata
            
            # Update package lists
            apt-get update -o APT::Status-Fd=1
            
            # Install Desktop Environment if selected
            ${if (deCommand.isNotEmpty()) deCommand else "echo 'CLI-only mode, skipping DE installation'"}
            
            # Remove the insecure bypass
            rm -f /etc/apt/apt.conf.d/99-allow-unauth
            
            echo "SETUP_COMPLETE"
        """.trimIndent()
    }
    
    private fun injectSystemConfiguration(rootfsPath: String) {
        try {
            val rootfsDir = File(rootfsPath)
            
            // 1. Fix Hostname Resolution
            val hostnameFile = File(rootfsDir, "etc/hostname")
            val hostsFile = File(rootfsDir, "etc/hosts")
            hostnameFile.writeText(hostname.value)
            hostsFile.writeText("127.0.0.1 localhost\n127.0.1.1 ${hostname.value}\n")
            android.util.Log.i("SetupViewModel", "Injected hostname: ${hostname.value}")
            
            // 2. Fix DNS
            val resolvConf = File(rootfsDir, "etc/resolv.conf")
            resolvConf.writeText("nameserver 8.8.8.8\nnameserver 1.1.1.1\n")
            android.util.Log.i("SetupViewModel", "Injected DNS configuration")
            
            // 3. Disable APT Sandboxing & Ignore Auth for core install
            val aptConfDir = File(rootfsDir, "etc/apt/apt.conf.d")
            aptConfDir.mkdirs()
            File(aptConfDir, "99-root-sandbox").writeText("APT::Sandbox::User \"root\";\n")
            File(aptConfDir, "99-allow-unauth").writeText("APT::Get::AllowUnauthenticated \"true\";\nAcquire::AllowInsecureRepositories \"true\";\n")
            android.util.Log.i("SetupViewModel", "Injected APT configuration")
            
            // 4. Sudoers No-Password Injection (Critical Fix)
            val sudoersDir = File(rootfsDir, "etc/sudoers.d")
            sudoersDir.mkdirs()
            val userSudoFile = File(sudoersDir, "omak_user")
            userSudoFile.writeText("${username.value} ALL=(ALL) NOPASSWD:ALL\n")
            android.util.Log.i("SetupViewModel", "Injected sudoers configuration for ${username.value}")
            
            android.util.Log.i("SetupViewModel", "✓ All system configurations injected successfully")
            
        } catch (e: Exception) {
            android.util.Log.e("SetupViewModel", "Failed to inject system configuration", e)
            throw e
        }
    }
    
    fun executeSetup(context: Context, settingsRepository: id.xms.omakdroid.core.SettingsRepository, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val rootfsPath = RootfsPathResolver.getTrueRootfsPath(context)
                val setupScriptFile = File(rootfsPath, "tmp/setup.sh")
                
                // PHASE 1: Inject critical system configuration files directly
                installStatusText.value = "Injecting system configuration..."
                installProgress.floatValue = 0.05f
                delay(300)
                
                withContext(Dispatchers.IO) {
                    injectSystemConfiguration(rootfsPath)
                }
                
                // PHASE 2: Write the setup script
                installStatusText.value = "Generating setup script..."
                installProgress.floatValue = 0.1f
                delay(500)
                
                withContext(Dispatchers.IO) {
                    setupScriptFile.parentFile?.mkdirs()
                    setupScriptFile.writeText(generateSetupScript())
                    setupScriptFile.setExecutable(true)
                }
                
                installStatusText.value = "Executing setup script..."
                installProgress.floatValue = 0.2f
                
                // Execute based on mode
                val result = when (executionMode.value) {
                    MainViewModel.ExecutionMode.PROOT -> executePRootSetup(context, rootfsPath)
                    MainViewModel.ExecutionMode.CHROOT -> executeChrootSetup(rootfsPath)
                }
                
                // Animate progress while waiting
                animateProgress(0.2f, 0.9f, 100)
                
                installStatusText.value = "Finalizing setup..."
                installProgress.floatValue = 0.95f
                delay(500)
                
                // Clean up script for security
                withContext(Dispatchers.IO) {
                    if (setupScriptFile.exists()) {
                        setupScriptFile.delete()
                    }
                }
                
                // Save username to settings if successful
                if (result.first) {
                    settingsRepository.saveUsername(username.value)
                    android.util.Log.i("SetupViewModel", "Username saved: ${username.value}")
                }
                
                installProgress.floatValue = 1.0f
                installStatusText.value = "Setup complete!"
                delay(500)
                
                onComplete(result.first, result.second)
                
            } catch (e: Exception) {
                android.util.Log.e("SetupViewModel", "Setup execution failed", e)
                installStatusText.value = "Setup failed: ${e.message}"
                onComplete(false, e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun executePRootSetup(context: Context, rootfsPath: String): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                val prootPath = File(context.applicationInfo.nativeLibraryDir, "libproot.so").absolutePath
                val command = "/bin/bash /tmp/setup.sh"
                
                android.util.Log.i("SetupViewModel", "=== PRoot Setup Execution ===")
                android.util.Log.i("SetupViewModel", "PRoot Path: $prootPath")
                android.util.Log.i("SetupViewModel", "Rootfs Path: $rootfsPath")
                android.util.Log.i("SetupViewModel", "Command: $command")
                android.util.Log.i("SetupViewModel", "Desktop Environment: ${selectedDE.value}")
                
                // Set up output capture with real-time APT parsing
                val outputBuffer = StringBuilder()
                NativeEngine.onTerminalOutput = { line ->
                    android.util.Log.d("SetupExecution", line)
                    outputBuffer.append(line).append("\n")
                    
                    // Parse APT status lines in real-time
                    if (line.startsWith("dlstatus:") || line.startsWith("pmstatus:")) {
                        parseAptStatus(line)
                    }
                }
                
                // Execute via NativeEngine
                NativeEngine.executeLinuxCommand(command, prootPath, rootfsPath, context.cacheDir.absolutePath)
                
                // Wait for completion and monitor output
                var elapsed = 0L
                val timeout = 300000L // 5 minutes for DE installation
                while (elapsed < timeout) {
                    delay(1000)
                    elapsed += 1000
                    
                    if (outputBuffer.contains("SETUP_COMPLETE")) {
                        android.util.Log.i("SetupViewModel", "Setup completed successfully!")
                        break
                    }
                }
                
                val output = outputBuffer.toString()
                android.util.Log.i("SetupViewModel", "=== Setup Output ===")
                android.util.Log.i("SetupViewModel", output)
                
                val success = output.contains("SETUP_COMPLETE")
                if (success) {
                    Pair(true, "Setup completed via PRoot")
                } else {
                    Pair(false, "Setup did not complete within timeout. Check logs for details.")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("SetupViewModel", "PRoot setup failed", e)
                Pair(false, "PRoot execution failed: ${e.message}")
            } finally {
                NativeEngine.onTerminalOutput = null
            }
        }
    }
    
    private suspend fun executeChrootSetup(rootfsPath: String): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                if (!SystemChecker.isRootAvailable()) {
                    return@withContext Pair(false, "Root access not available for chroot")
                }
                
                val chrootCmd = "chroot $rootfsPath /bin/bash /tmp/setup.sh"
                android.util.Log.i("SetupViewModel", "=== Chroot Setup Execution ===")
                android.util.Log.i("SetupViewModel", "Rootfs Path: $rootfsPath")
                android.util.Log.i("SetupViewModel", "Command: $chrootCmd")
                
                val output = SystemChecker.executeRootCommand(chrootCmd)
                
                android.util.Log.i("SetupViewModel", "=== Setup Output ===")
                output.forEach { line ->
                    android.util.Log.d("SetupExecution", line)
                }
                
                val success = output.any { it.contains("SETUP_COMPLETE") }
                
                if (success) {
                    android.util.Log.i("SetupViewModel", "Setup completed successfully!")
                    Pair(true, "Setup completed via chroot")
                } else {
                    android.util.Log.w("SetupViewModel", "Setup did not complete successfully")
                    Pair(false, "Chroot execution did not complete successfully. Check logs for details.")
                }
            } catch (e: Exception) {
                android.util.Log.e("SetupViewModel", "Chroot setup failed", e)
                Pair(false, "Chroot execution failed: ${e.message}")
            }
        }
    }
    
    private suspend fun animateProgress(from: Float, to: Float, durationMs: Long) {
        val steps = 20
        val increment = (to - from) / steps
        val delayMs = durationMs / steps
        
        repeat(steps) {
            delay(delayMs)
            installProgress.floatValue += increment
        }
    }
}
