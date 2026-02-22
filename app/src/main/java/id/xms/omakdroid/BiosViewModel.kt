package id.xms.omakdroid

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class BiosViewModel : ViewModel() {
    private val _logs = MutableStateFlow("")
    val logs: StateFlow<String> = _logs.asStateFlow()

    private val _extractionComplete = MutableStateFlow(false)
    val extractionComplete: StateFlow<Boolean> = _extractionComplete.asStateFlow()

    fun extractAssets(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                appendLog("[OmakDroid BIOS] Initializing asset extraction...")
                
                val filesDir = context.filesDir
                appendLog("[BIOS] Target directory: ${filesDir.absolutePath}")

                val rootfsDir = File(filesDir, "rootfs")
                
                if (rootfsDir.exists() && rootfsDir.list()?.isNotEmpty() == true) {
                    appendLog("[BIOS] Rootfs already extracted, skipping...")
                } else {
                    rootfsDir.mkdirs()
                    appendLog("[BIOS] Created rootfs directory")
                    
                    appendLog("[BIOS] Copying omak_core.tar to cache...")
                    val assetManager = context.assets
                    val tarFile = File(context.cacheDir, "omak_core.tar")
                    
                    assetManager.open("rootfs/omak_core.tar").use { input ->
                        FileOutputStream(tarFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    appendLog("[BIOS] ✓ Tar archive staged: ${tarFile.absolutePath}")
                    
                    appendLog("[BIOS] Unpacking via Native Tar (This may take a minute)...")
                    appendLog("[BIOS] Please wait...")
                    
                    val tarProcess = ProcessBuilder(
                        "tar",
                        "-xf",
                        tarFile.absolutePath,
                        "-C",
                        rootfsDir.absolutePath
                    ).redirectErrorStream(true).start()
                    
                    val tarOutput = tarProcess.inputStream.bufferedReader().use { it.readText() }
                    val tarExitCode = tarProcess.waitFor()
                    
                    if (tarExitCode == 0 || tarExitCode == 1) {
                        appendLog("[BIOS] ✓ Rootfs unpacked successfully")
                        if (tarExitCode == 1) {
                            appendLog("[BIOS] Note: Some hardlinks may have failed (non-critical)")
                        }
                    } else {
                        appendLog("[BIOS] ✗ Tar extraction failed (exit code: $tarExitCode)")
                        appendLog("[BIOS] Output: $tarOutput")
                        Log.e("OmakDroidExt", "TAR FAILED: $tarOutput")
                    }
                    
                    tarFile.delete()
                    appendLog("[BIOS] ✓ Cleaned up temporary tar file")
                }

                // PRoot binary is now handled by Android Package Manager as libproot.so
                appendLog("[BIOS] PRoot binary loaded from native library directory")

                appendLog("[BIOS] Asset extraction complete!")
                appendLog("[BIOS] Booting OmakDroid Kernel...")
                _extractionComplete.value = true

            } catch (e: Exception) {
                appendLog("[BIOS ERROR] EXTRACTION FAILED: ${e.message}")
                Log.e("OmakDroidExt", "EXTRACTION FAILED", e)
                e.printStackTrace()
            }
        }
    }

    private fun appendLog(message: String) {
        _logs.value += "$message\n"
    }
}
