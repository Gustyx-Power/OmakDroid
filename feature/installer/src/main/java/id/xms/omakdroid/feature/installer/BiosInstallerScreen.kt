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
import id.xms.omakdroid.core.common.AssetExtractor

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

class BiosViewModel(private val assetExtractor: AssetExtractor) : ViewModel() {
    private val _state = MutableStateFlow(BiosState())
    val state: StateFlow<BiosState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<BiosEffect>()
    val effect: SharedFlow<BiosEffect> = _effect.asSharedFlow()

    private val installSteps = listOf(
        "Initializing Fake BIOS...",
        "Validating CPU Microcode...",
        "Mounting /proc...",
        "Mounting /sys...",
        "Mounting /dev..."
    )

    fun processIntent(intent: BiosIntent) {
        when (intent) {
            is BiosIntent.StartInstallation -> viewModelScope.launch { runInstallationSequence() }
        }
    }

    private suspend fun runInstallationSequence() {
        if (assetExtractor.isSystemInstalled()) {
            _state.update { 
                it.copy(
                    logs = it.logs + "System already installed. Fast-forwarding boot sequence...",
                    progress = 100
                )
            }
            delay(1000)
            _effect.emit(BiosEffect.NavigateToDesktop)
            return
        }

        installSteps.forEachIndexed { index, log ->
            delay((200..400).random().toLong())
            val currentProgress = ((index + 1) * 20) / installSteps.size
            _state.update {
                it.copy(
                    logs = it.logs + log,
                    progress = currentProgress
                )
            }
        }

        try {
            _state.update { it.copy(logs = it.logs + "Extracting binaries (PRoot namespace)...", progress = 25) }
            assetExtractor.installBinary()

            _state.update { it.copy(logs = it.logs + "Unpacking OmakDroid rootfs [debian12-arm64]...", progress = 30) }
            assetExtractor.unpackRootfs().collect { logUpdate ->
                _state.update {
                    val logsToKeep = if (it.logs.size > 20) it.logs.drop(1) else it.logs
                    it.copy(
                        logs = logsToKeep + logUpdate,
                        progress = minOf(95, it.progress + 2)
                    )
                }
            }

            _state.update {
                it.copy(
                    logs = it.logs + "Boot sequence complete. Configuring Framebuffer /dev/graphics/fb0...",
                    progress = 100
                )
            }
            delay(1000)
            _effect.emit(BiosEffect.NavigateToDesktop)
            
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    logs = it.logs + "FATAL KERNEL PANIC: Extraction failed - ${e.message}",
                    progress = 0
                )
            }
        }
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
