package id.xms.omakdroid


object NativeEngine {
    init {
        try {
            System.loadLibrary("rust_engine")
        } catch (e: UnsatisfiedLinkError) {
            android.util.Log.w("NativeEngine", "Rust library not loaded: ${e.message}")
        }
    }
    external fun initEngine(): Boolean
    external fun executeCommand(command: String): Boolean
    external fun pingEngine(): String
}
