package id.xms.omakdroid.core.common

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream

class AssetExtractor(private val context: Context) {

    private val filesDir = context.filesDir.absolutePath

    fun isSystemInstalled(): Boolean {
        val bashFile = File(filesDir, "rootfs/bin/bash")
        return bashFile.exists()
    }

    suspend fun installBinary(): Unit = withContext(Dispatchers.IO) {
        val binDir = File(filesDir, "bin")
        if (!binDir.exists()) binDir.mkdirs()

        val prootFile = File(binDir, "proot")
        context.assets.open("bin/proot").use { input ->
            FileOutputStream(prootFile).use { output ->
                input.copyTo(output)
            }
        }
        prootFile.setExecutable(true, false)
    }

    fun unpackRootfs(): Flow<String> = flow {
        emit("Initializing payload extraction sequence...")
        val rootfsDir = File(filesDir, "rootfs")
        if (!rootfsDir.exists()) rootfsDir.mkdirs()

        try {
            var extractedCount = 0
            context.assets.open("rootfs/omak_core.tar.gz").use { assetStream ->
                GZIPInputStream(assetStream).use { gzipStream ->
                    TarArchiveInputStream(gzipStream).use { tarStream ->
                        var entry: TarArchiveEntry? = tarStream.nextTarEntry
                        while (entry != null) {
                            val destFile = File(rootfsDir, entry.name)
                            if (entry.isDirectory) {
                                destFile.mkdirs()
                            } else {
                                destFile.parentFile?.mkdirs()
                                FileOutputStream(destFile).use { output ->
                                    tarStream.copyTo(output)
                                }
                                if ((entry.mode and 0b001_001_001) != 0) {
                                    destFile.setExecutable(true, false)
                                }
                            }
                            
                            extractedCount++
                            if (extractedCount % 100 == 0) {
                                emit("Extracted $extractedCount core files...")
                            }
                            
                            entry = tarStream.nextTarEntry
                        }
                    }
                }
            }
            emit("Extraction complete. Extracted $extractedCount total files.")
        } catch (e: Exception) {
            emit("CRITICAL ERROR during extraction: ${e.message}")
            throw e
        }
    }.flowOn(Dispatchers.IO)
}
