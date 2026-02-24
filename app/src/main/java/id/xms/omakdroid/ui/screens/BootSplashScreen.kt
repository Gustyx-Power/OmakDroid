package id.xms.omakdroid.ui.screens

import id.xms.omakdroid.R
import id.xms.omakdroid.core.installer.InstallerViewModel

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun BootSplashScreen(
    viewModel: InstallerViewModel,
    onBootComplete: () -> Unit
) {
    val context = LocalContext.current
    val statusText by viewModel.statusText.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val bootComplete by viewModel.bootComplete.collectAsState()

    // Ubuntu Background Gradient
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF300A24), // Ubuntu Aubergine Dark
            Color(0xFF5E2750), // Ubuntu Aubergine
        )
    )

    // Pulsing effect for the logo
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_anim"
    )

    // Start initialization
    LaunchedEffect(Unit) {
        viewModel.initializeSystem(context)
    }

    // Navigate when boot is complete
    LaunchedEffect(bootComplete) {
        if (bootComplete) {
            delay(500)
            onBootComplete()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        val isLandscape = maxWidth > maxHeight
        val contentPadding = if (isLandscape) 48.dp else 32.dp
        val logoSize = if (maxWidth >= 600.dp) 160.dp else 120.dp
        val progressBarWidth = if (maxWidth >= 600.dp) 400.dp else 280.dp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // App Icon with breathing animation
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "OmakDroid Logo",
                modifier = Modifier.size(logoSize).fillMaxWidth(alpha),
                tint = Color(0xFFE95420) // Ubuntu Orange
            )

            Spacer(modifier = Modifier.weight(1f))

            // Glassmorphic
            Box(
                modifier = Modifier
                    .padding(bottom = contentPadding * 2)
                    .width(progressBarWidth)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // macOS-style thin, rounded Progress Bar
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFFE95420),
                        trackColor = Color.Black.copy(alpha = 0.3f),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status Text
                    Text(
                        text = statusText,
                        color = Color.White.copy(alpha = 0.9f),
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
