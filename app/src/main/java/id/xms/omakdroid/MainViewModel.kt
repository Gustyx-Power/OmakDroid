package id.xms.omakdroid

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    
    
    enum class ExecutionMode {
        PROOT,
        CHROOT
    }
    val currentExecutionMode = mutableStateOf(ExecutionMode.PROOT)
    val isRootAvailable = mutableStateOf(false)
    
    fun setExecutionMode(mode: ExecutionMode) {
        currentExecutionMode.value = mode
    }
    fun updateRootStatus(available: Boolean) {
        isRootAvailable.value = available
    }
}
