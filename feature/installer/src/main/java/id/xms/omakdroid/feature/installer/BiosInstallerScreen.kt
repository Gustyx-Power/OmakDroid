package id.xms.omakdroid.feature.installer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- MVI Intents & Contracts ---

data class BiosState(
    val logs: List<String> = emptyList(),
    val progress: Int = 0
)

sealed interface BiosIntent {
    object StartInstallation : BiosIntent()
}

sealed interface BiosEffect {
    object NavigateToDesktop : BiosEffect()
}

// --- ViewModel Core Logic ---

class BiosViewModel : ViewModel() {
    private val _state = MutableStateFlow(BiosState())
    val state: StateFlow<BiosState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<BiosEffect>()
    val effect: SharedFlow<BiosEffect> = _effect.asSharedFlow()

    private val installSteps = listOf(
        "Initializing Fake BIOS...",
        "Validating CPU Microcode...",
        "Mounting /proc...",
        "Mounting /sys...",
        "Mounting /dev...",
        "Unpacking OmakDroid rootfs [debian12-arm64]...",
        "Extracting binaries...",
        "Setting up PRoot namespace bindings...",
        "Starting kernel translation layer...",
        "Configuring Framebuffer /dev/graphics/fb0...",
        "Boot sequence complete."
    )

    fun processIntent(intent: BiosIntent) {
        when (intent) {
            is BiosIntent.StartInstallation -> viewModelScope.launch { runInstallationSequence() }
        }
    }

    private suspend fun runInstallationSequence() {
        installSteps.forEachIndexed { index, log ->
            delay((200..800).random().toLong()) // Faux jitter allocation simulate
            val currentProgress = ((index + 1) * 100) / installSteps.size
            _state.update {
                it.copy(
                    logs = it.logs + log,
                    progress = currentProgress
                )
            }
        }
        delay(1000)
        _effect.emit(BiosEffect.NavigateToDesktop)
    }
}

// --- Presentation Layer ---

@Composable
fun BiosInstallerScreen(
    viewModel: BiosViewModel,
    onNavigateToDesktop: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BiosEffect.NavigateToDesktop -> onNavigateToDesktop()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.processIntent(BiosIntent.StartInstallation)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Column {
            state.logs.forEach { log ->
                Text(
                    text = log,
                    color = Color.Green,
                    fontFamily = FontFamily.Monospace
                )
            }
            if (state.progress == 100) {
                Text(
                    text = "System Ready. Handing over vector space...",
                    color = Color.Green,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
