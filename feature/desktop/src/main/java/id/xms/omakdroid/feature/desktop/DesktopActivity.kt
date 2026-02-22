package id.xms.omakdroid.feature.desktop

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import id.xms.omakdroid.core.system.NativeBridge

class DesktopActivity : ComponentActivity() {

    private var isPhysicalOTGAttached = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enforceKioskTearDown()
        checkHardwareManifest()
        interceptPointerEvents()

        val prootHandshakeResponse = NativeBridge.initProotEnvironment()

        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OmakDroid Shared-Mem Framebuffer Placeholder\nKernel Interface Feedback: $prootHandshakeResponse",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }

    /**
     * Secures the activity context to prevent escape. Assumes DeviceOwner privilege if properly provisioned.
     */
    private fun enforceKioskTearDown() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        try {
            startLockTask() // Trigger intent pinning
        } catch (e: Exception) {
            e.printStackTrace() // Allowed to fail safely if not fully granted admin privs yet
        }

        // De-legitimize UI decorations
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun checkHardwareManifest() {
        val config = resources.configuration
        isPhysicalOTGAttached = config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO
        remapVirtualKeyboardOverlay(isPhysicalOTGAttached)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isPhysicalOTGAttached = newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO
        remapVirtualKeyboardOverlay(isPhysicalOTGAttached)
    }

    private fun remapVirtualKeyboardOverlay(physicalAttached: Boolean) {
        if (physicalAttached) {
            // Signal Virtual Keyboard layer to disable processing queue
        } else {
            // Signal Virtual Keyboard layer to wake
        }
    }

    /**
     * Sniffs the root window view tree for raw peripheral mouse interactions over OTG
     */
    private fun interceptPointerEvents() {
        window.decorView.rootView.setOnGenericMotionListener { _: View, event: MotionEvent ->
            if (event.isFromSource(InputDevice.SOURCE_MOUSE)) {
                when (event.actionMasked) {
                    MotionEvent.ACTION_HOVER_MOVE, MotionEvent.ACTION_MOVE -> {
                        val absoluteX = event.x
                        val absoluteY = event.y
                        
                        // To be dispatched natively: NativeBridge.dispatchX11PointerEvent(absoluteX, absoluteY)
                        
                        return@setOnGenericMotionListener true // Consume pointer lifecycle entirely
                    }
                }
            }
            false
        }
    }
}
