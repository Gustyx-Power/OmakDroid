package id.xms.omakdroid.core.system

/**
 * Singleton Native Router serving as the bridge to the PRoot abstraction layer.
 * Calls to low-level socket interception or memFd mapping should route through this.
 */
object NativeBridge {
    init {
        System.loadLibrary("core_system")
    }

    /**
     * Initializes the core isolation bindings in the Native Rust layer.
     * Guaranteed to allocate thread barriers.
     */
    external fun initProotEnvironment(): String
}
