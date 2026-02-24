package id.xms.omakdroid.core.engine

/**
 * JNI bridge to the Rust engine for executing Linux commands via PRoot.
 */
object NativeEngine {
    init {
        try {
            System.loadLibrary("rust_engine")
        } catch (e: UnsatisfiedLinkError) {
            android.util.Log.w("NativeEngine", "Rust library not loaded: ${e.message}")
        }
    }
    
    /**
     * Callback for real-time terminal output streaming.
     */
    var onTerminalOutput: ((String) -> Unit)? = null
    
    /**
     * Called from Rust to append output to the terminal.
     */
    @JvmStatic
    fun appendOutput(text: String) {
        onTerminalOutput?.invoke(text)
    }
    
    external fun initEngine(): Boolean
    external fun executeCommand(command: String): Boolean
    external fun pingEngine(): String
    external fun bootLinuxKernel(prootPath: String, rootfsPath: String, tmpDir: String): String
    external fun executeLinuxCommand(command: String, prootPath: String, rootfsPath: String, tmpDir: String)
}
