package id.xms.omakdroid.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.omakdroid.ui.theme.UbuntuOrange

@Composable
fun InstallProgressScreen(
    viewModel: SetupViewModel,
    settingsRepository: id.xms.omakdroid.core.SettingsRepository,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var setupFailed by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        viewModel.resetInstallation()
        
        viewModel.executeSetup(context, settingsRepository) { success, message ->
            if (success) {
                onComplete()
            } else {
                setupFailed = true
                errorMessage = message
                android.util.Log.e("InstallProgressScreen", "Setup failed: $message")
            }
        }
    }

    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2C2C2E),
            Color(0xFF1C1C1E)
        )
    )
    
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        val isLandscape = maxWidth > maxHeight
        val cardWidth = if (isLandscape) maxWidth * 0.6f else maxWidth * 0.9f
        val maxCardWidth = 550.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(minOf(cardWidth, maxCardWidth))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .shadow(12.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.5f))
                    .padding(48.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (setupFailed) "Installation Failed" else "Installing OmakDroid",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (setupFailed) Color(0xFFFF453A) else Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (setupFailed) 
                            "An error occurred during setup" 
                        else 
                            "Please wait while we set up your Linux environment",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    if (!setupFailed) {
                        CircularProgressIndicator(
                            progress = { viewModel.installProgress.floatValue },
                            modifier = Modifier.size(100.dp),
                            color = UbuntuOrange,
                            strokeWidth = 6.dp,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "${(viewModel.installProgress.floatValue * 100).toInt()}%",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = UbuntuOrange
                        )
                        
                        Spacer(modifier = Modifier.height(40.dp))
                        
                        LinearProgressIndicator(
                            progress = { viewModel.installProgress.floatValue },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = UbuntuOrange,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Current Action
                        if (viewModel.currentAction.value.isNotEmpty()) {
                            Text(
                                text = viewModel.currentAction.value,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        // Status Text
                        Text(
                            text = viewModel.installStatusText.value,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        
                        // Download Speed and ETA
                        if (viewModel.downloadSpeed.value.isNotEmpty() || viewModel.etaText.value.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (viewModel.downloadSpeed.value.isNotEmpty()) {
                                    Text(
                                        text = viewModel.downloadSpeed.value,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 12.sp,
                                        color = UbuntuOrange.copy(alpha = 0.8f)
                                    )
                                }
                                
                                if (viewModel.etaText.value.isNotEmpty()) {
                                    Text(
                                        text = viewModel.etaText.value,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = errorMessage,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp,
                            color = Color(0xFFFF453A),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
