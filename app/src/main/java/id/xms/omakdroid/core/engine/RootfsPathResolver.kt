package id.xms.omakdroid.core.engine

import android.content.Context
import java.io.File

object RootfsPathResolver {
    /**
     * Dynamically resolves the true rootfs path, handling both flat and nested archive structures.
     * 
     * If the archive was packed with a parent directory (nested structure), this function
     * will return the path to the nested directory. Otherwise, it returns the base rootfs path.
     * 
     * This approach avoids the need to move files, which can be risky with large directories.
     * 
     * @param context Android context
     * @return Absolute path to the true rootfs directory
     */
    fun getTrueRootfsPath(context: Context): String {
        val baseRootfs = File(context.filesDir, "rootfs")
        val nestedRootfs = File(baseRootfs, "rootfs")
        
        // Check if nested structure exists (inception folder)
        return if (nestedRootfs.exists() && nestedRootfs.isDirectory) {
            // Nested structure detected, use the inner directory
            nestedRootfs.absolutePath
        } else {
            // Flat structure, use the base directory
            baseRootfs.absolutePath
        }
    }
    
    /**
     * Checks if rootfs exists at either the base or nested location.
     * 
     * @param context Android context
     * @return true if rootfs exists, false otherwise
     */
    fun rootfsExists(context: Context): Boolean {
        val baseRootfs = File(context.filesDir, "rootfs")
        val nestedRootfs = File(baseRootfs, "rootfs")
        
        return when {
            nestedRootfs.exists() && nestedRootfs.isDirectory -> {
                // Check for key files in nested structure
                File(nestedRootfs, "bin/bash").exists() || 
                File(nestedRootfs, "usr/bin").exists()
            }
            baseRootfs.exists() && baseRootfs.isDirectory -> {
                // Check for key files in flat structure
                File(baseRootfs, "bin/bash").exists() || 
                File(baseRootfs, "usr/bin").exists()
            }
            else -> false
        }
    }
}
