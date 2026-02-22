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

                // Extract rootfs/
                val rootfsDir = File(filesDir, "rootfs")
                if (!rootfsDir.exists()) {
                    rootfsDir.mkdirs()
                    appendLog("[BIOS] Created rootfs directory")
                }

                try {
                    val assetManager = context.assets
                    val rootfsAssets = assetManager.list("rootfs") ?: emptyArray()
                    appendLog("[BIOS] Found ${rootfsAssets.size} assets in rootfs/")
                    
                    rootfsAssets.forEach { asset ->
                        appendLog("[BIOS] Extracting rootfs/$asset...")
                        val inputStream = assetManager.open("rootfs/$asset")
                        val outputFile = File(rootfsDir, asset)
                        FileOutputStream(outputFile).use { output ->
                            inputStream.copyTo(output)
                        }
                        appendLog("[BIOS] ✓ Extracted: ${outputFile.absolutePath}")
                    }
                } catch (e: Exception) {
                    appendLog("[BIOS] No rootfs assets found or extraction failed")
                    Log.e("OmakDroidExt", "ROOTFS EXTRACTION FAILED", e)
                }

                // Extract bin/proot
                val binDir = File(filesDir, "bin")
                if (!binDir.exists()) {
                    binDir.mkdirs()
                    appendLog("[BIOS] Created bin directory")
                }

                try {
                    val assetManager = context.assets
                    val binAssets = assetManager.list("bin") ?: emptyArray()
                    appendLog("[BIOS] Found ${binAssets.size} assets in bin/")
                    
                    binAssets.forEach { asset ->
                        appendLog("[BIOS] Extracting bin/$asset...")
                        val inputStream = assetManager.open("bin/$asset")
                        val outputFile = File(binDir, asset)
                        FileOutputStream(outputFile).use { output ->
                            inputStream.copyTo(output)
                        }
                        // Make executable
                        outputFile.setExecutable(true, false)
                        appendLog("[BIOS] ✓ Extracted & chmod +x: ${outputFile.absolutePath}")
                    }
                } catch (e: Exception) {
                    appendLog("[BIOS] No bin assets found or extraction failed")
                    Log.e("OmakDroidExt", "BIN EXTRACTION FAILED", e)
                }

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
