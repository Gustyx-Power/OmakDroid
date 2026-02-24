package id.xms.omakdroid.core.installer

import android.content.Context
import id.xms.omakdroid.core.engine.RootfsPathResolver
import android.os.StatFs
import android.system.Os
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * ViewModel for managing system initialization and rootfs extraction.
 * Handles downloading, extracting, and validating the Ubuntu base image.
 */
class InstallerViewModel : ViewModel() {
    companion object {
        private const val TAG = "BootSplash"
        private const val CORE_URL = "https://github.com/Gustyx-Power/OmakDroid/releases/download/image-ubuntubased/omak_core.tar.gz"
        private const val MIN_FREE_SPACE_BYTES = 15L * 1024 * 1024 * 1024
        private const val MAX_HEADER_ERRORS = 5
        private const val MAX_PATH_DEPTH = 40
        private const val MAX_COMPONENT_REPEATS = 8
        private const val MIN_VALID_ROOTFS_BYTES = 100L * 1024 * 1024
    }

    private val _statusText = MutableStateFlow("Initializing...")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _bootComplete = MutableStateFlow(false)
    val bootComplete: StateFlow<Boolean> = _bootComplete.asStateFlow()

    /**
     * Initialize the system by checking for existing rootfs or downloading and extracting it.
     */
    fun initializeSystem(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val filesDir = context.filesDir
                val rootfsDir = File(filesDir, "rootfs")

                if (RootfsPathResolver.rootfsExists(context)) {
                    _statusText.value = "System Ready"
                    _progress.value = 1f
                    _bootComplete.value = true
                    return@launch
                }

                val stat = StatFs(filesDir.absolutePath)
                val freeBytes = stat.availableBytes
                if (freeBytes < MIN_FREE_SPACE_BYTES) {
                    val freeGB = freeBytes / (1024 * 1024 * 1024)
                    throw IOException("Insufficient storage: ${freeGB}GB free, need 15GB+")
                }
                Log.i(TAG, "Storage check passed: ${freeBytes / (1024 * 1024 * 1024)}GB free")

                val tarGzFile = File(filesDir, "omak_core.tar.gz")

                if (!tarGzFile.exists()) {
                    _statusText.value = "Connecting to Server..."
                    _progress.value = 0.05f
                    downloadFile(CORE_URL, tarGzFile)
                } else {
                    _statusText.value = "Reusing cached archive..."
                    _progress.value = 0.7f
                    Log.i(TAG, "Found existing omak_core.tar.gz, skipping download")
                }

                if (rootfsDir.exists()) {
                    Log.i(TAG, "Cleaning stale rootfs before extraction")
                    rootfsDir.deleteRecursively()
                }
                rootfsDir.mkdirs()

                _statusText.value = "Finalizing write cache..."
                delay(2000)

                _statusText.value = "Decompressing & Extracting... Please wait."
                _progress.value = 0.7f
                extractTarGz(tarGzFile, rootfsDir)

                _statusText.value = "Fixing rootfs layout..."
                _progress.value = 0.85f
                ensureEssentialSymlinks(rootfsDir)

                _statusText.value = "Optimizing File Permissions..."
                _progress.value = 0.9f
                fixPermissions(rootfsDir)

                Log.i(TAG, "Keeping omak_core.tar.gz for development re-use")

                val rootfsSize = rootfsDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
                if (rootfsSize < MIN_VALID_ROOTFS_BYTES) {
                    val sizeMB = rootfsSize / (1024 * 1024)
                    Log.e(TAG, "ROOTFS TOO SMALL: ${sizeMB}MB extracted. Archive likely broken.")
                    _statusText.value = "ERROR: Rootfs only ${sizeMB}MB — archive is broken."
                    _progress.value = 0f
                    return@launch
                }

                _statusText.value = "System Ready"
                _progress.value = 1f
                _bootComplete.value = true

            } catch (e: Exception) {
                _statusText.value = "ERROR: ${e.message}"
                _progress.value = 0f
                Log.e(TAG, "Boot failed", e)
                e.printStackTrace()

                try {
                    val rootfsDir = File(context.filesDir, "rootfs")
                    val tarGzFile = File(context.filesDir, "omak_core.tar.gz")
                    if (rootfsDir.exists()) rootfsDir.deleteRecursively()
                    if (tarGzFile.exists()) tarGzFile.delete()
                    Log.w(TAG, "Deleted corrupted archive + rootfs for clean retry")
                } catch (cleanupEx: Exception) {
                    Log.e(TAG, "Cleanup after failure also failed", cleanupEx)
                }
            }
        }
    }

    /**
     * Download the rootfs archive from the remote server.
     */
    private fun downloadFile(urlString: String, destination: File) {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 30000
        connection.readTimeout = 30000
        connection.connect()

        val fileSize = connection.contentLength
        var downloadedSize = 0L

        connection.inputStream.use { input ->
            FileOutputStream(destination).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    downloadedSize += bytesRead

                    if (fileSize > 0) {
                        val progressPercent = (downloadedSize * 100 / fileSize).toInt()
                        val downloadedMB = downloadedSize / (1024 * 1024)
                        val totalMB = fileSize / (1024 * 1024)

                        _statusText.value = "Downloading System Image: $progressPercent% ($downloadedMB MB / $totalMB MB)"
                        _progress.value = 0.05f + (downloadedSize.toFloat() / fileSize * 0.65f)
                    }
                }
            }
        }
    }

    /**
     * Extract tar.gz archive with symlink-loop protection and error recovery.
     */
    private suspend fun extractTarGz(tarGzFile: File, outputDir: File) {
        withContext(Dispatchers.IO) {
            var filesExtracted = 0
            var symlinksCreated = 0
            var dirsCreated = 0
            var hardlinksCreated = 0
            var skipped = 0
            var headerErrors = 0
            var lastEntryName = "(none)"

            try {
                if (outputDir.exists()) outputDir.deleteRecursively()
                outputDir.mkdirs()

                FileInputStream(tarGzFile).use { fis ->
                    BufferedInputStream(fis, 2048 * 1024).use { bis ->
                        GzipCompressorInputStream(bis, true).use { gis ->
                            TarArchiveInputStream(gis, "UTF-8").use { tar ->
                                while (true) {
                                    val entry: TarArchiveEntry?
                                    try {
                                        entry = tar.nextTarEntry
                                        if (entry == null) break
                                        headerErrors = 0
                                    } catch (headerEx: Exception) {
                                        headerErrors++
                                        Log.w(
                                            TAG,
                                            "Header error #$headerErrors after $filesExtracted files " +
                                            "(last OK: $lastEntryName): ${headerEx.message}"
                                        )
                                        if (headerErrors >= MAX_HEADER_ERRORS) {
                                            Log.e(TAG, "Too many consecutive header errors, stopping extraction")
                                            break
                                        }
                                        continue
                                    }

                                    val outFile = File(outputDir, entry.name)
                                    lastEntryName = entry.name

                                    if (isSymlinkLoop(entry.name)) {
                                        skipped++
                                        continue
                                    }

                                    try {
                                        when {
                                            entry.isSymbolicLink -> {
                                                outFile.parentFile?.mkdirs()
                                                if (outFile.exists()) outFile.delete()
                                                Os.symlink(entry.linkName, outFile.absolutePath)
                                                symlinksCreated++
                                            }

                                            entry.isLink -> {
                                                outFile.parentFile?.mkdirs()
                                                val linkTarget = File(outputDir, entry.linkName)
                                                if (linkTarget.exists()) {
                                                    try {
                                                        Os.link(linkTarget.absolutePath, outFile.absolutePath)
                                                    } catch (_: Exception) {
                                                        linkTarget.copyTo(outFile, overwrite = true)
                                                    }
                                                    hardlinksCreated++
                                                }
                                            }

                                            entry.isDirectory -> {
                                                outFile.mkdirs()
                                                dirsCreated++
                                            }

                                            else -> {
                                                outFile.parentFile?.mkdirs()
                                                FileOutputStream(outFile).use { fos ->
                                                    tar.copyTo(fos)
                                                }
                                                outFile.setExecutable(true, false)
                                                filesExtracted++
                                            }
                                        }
                                    } catch (entryEx: Exception) {
                                        Log.w(TAG, "Skipping entry: ${entry.name}: ${entryEx.message}")
                                        skipped++
                                    }
                                }
                            }
                        }
                    }
                }

                Log.i(
                    TAG,
                    "Extraction complete: $filesExtracted files, $dirsCreated dirs, " +
                    "$symlinksCreated symlinks, $hardlinksCreated hardlinks, $skipped skipped"
                )

            } catch (e: Exception) {
                Log.e(TAG, "Extraction failed after $filesExtracted files, nuking corrupted state", e)
                tarGzFile.delete()
                outputDir.deleteRecursively()
                throw e
            }
        }
    }

    /**
     * Create essential Ubuntu rootfs symlinks if missing.
     */
    private fun ensureEssentialSymlinks(rootfsDir: File) {
        val essentialLinks = mapOf(
            "bin"   to "usr/bin",
            "lib"   to "usr/lib",
            "lib64" to "usr/lib64",
            "sbin"  to "usr/sbin"
        )

        for ((linkName, target) in essentialLinks) {
            val linkFile = File(rootfsDir, linkName)
            val targetDir = File(rootfsDir, target)

            if (targetDir.exists() && !linkFile.exists()) {
                try {
                    Os.symlink(target, linkFile.absolutePath)
                    Log.i(TAG, "Created essential symlink: $linkName -> $target")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to create essential symlink $linkName -> $target: ${e.message}")
                }
            }
        }
    }

    /**
     * Detect symlink-loop paths created by archives with cyclic symlinks.
     */
    private fun isSymlinkLoop(entryName: String): Boolean {
        val components = entryName.split('/')
        if (components.size > MAX_PATH_DEPTH) {
            Log.w(TAG, "Skipping too-deep path (${components.size} levels): $entryName")
            return true
        }
        var repeats = 1
        for (i in 1 until components.size) {
            if (components[i] == components[i - 1] && components[i].isNotEmpty()) {
                repeats++
                if (repeats > MAX_COMPONENT_REPEATS) {
                    Log.w(TAG, "Skipping symlink-loop path (${components[i]} repeated $repeats×): $entryName")
                    return true
                }
            } else {
                repeats = 1
            }
        }
        return false
    }

    /**
     * Fix file permissions for the extracted rootfs.
     */
    private fun fixPermissions(rootfsDir: File) {
        try {
            val process = Runtime.getRuntime().exec(
                arrayOf("chmod", "-R", "777", rootfsDir.absolutePath)
            )
            process.waitFor()
        } catch (e: Exception) {
            Log.e(TAG, "chmod failed", e)
        }
    }
}
