package id.xms.omakdroid.core

import com.topjohnwu.superuser.Shell


object SystemChecker {
    
    private var initialized = false
    fun initialize() {
        if (!initialized) {
            Shell.setDefaultBuilder(
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_REDIRECT_STDERR)
                    .setTimeout(10)
            )
            initialized = true
        }
    }
    
    fun isRootAvailable(): Boolean {
        return try {
            Shell.getShell().isRoot
        } catch (e: Exception) {
            android.util.Log.e("SystemChecker", "Error checking root access", e)
            false
        }
    }
   
    fun executeRootCommand(command: String): List<String> {
        return try {
            val result = Shell.cmd(command).exec()
            if (result.isSuccess) {
                result.out
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("SystemChecker", "Error executing root command: $command", e)
            emptyList()
        }
    }
}
