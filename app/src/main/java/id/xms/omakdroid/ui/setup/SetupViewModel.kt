package id.xms.omakdroid.ui.setup

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import id.xms.omakdroid.MainViewModel


class SetupViewModel : ViewModel() {
    
    // User Information
    val fullName = mutableStateOf("")
    val hostname = mutableStateOf("omakdroid")
    val username = mutableStateOf("")
    val password = mutableStateOf("")
    val confirmPassword = mutableStateOf("")
    
    // Localization
    val language = mutableStateOf("English")
    val timezone = mutableStateOf("Asia/Jakarta")
    
    // Execution Mode (PROOT or CHROOT)
    val executionMode = mutableStateOf(MainViewModel.ExecutionMode.PROOT)
    
    // Installation Progress
    val installProgress = mutableFloatStateOf(0f)
    val installStatusText = mutableStateOf("Preparing installation...")
    
    // Validation
    fun isUserSetupValid(): Boolean {
        return fullName.value.isNotBlank() &&
                hostname.value.isNotBlank() &&
                username.value.isNotBlank() &&
                password.value.isNotBlank() &&
                password.value == confirmPassword.value &&
                username.value.matches(Regex("^[a-z_][a-z0-9_-]*$"))
    }
    
    fun resetInstallation() {
        installProgress.floatValue = 0f
        installStatusText.value = "Preparing installation..."
    }
}
