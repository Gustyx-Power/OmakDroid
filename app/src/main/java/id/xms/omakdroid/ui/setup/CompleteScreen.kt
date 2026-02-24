package id.xms.omakdroid.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.omakdroid.core.SettingsRepository
import id.xms.omakdroid.ui.theme.UbuntuOrange
import kotlinx.coroutines.launch

@Composable
fun CompleteScreen(
    settingsRepository: SettingsRepository,
    onRestart: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
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
            Spacer(modifier = Modifier.weight(1f, fill = false))

            // Main Setup Card
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFF34C759) // macOS success green
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Installation Complete",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "OmakDroid is ready to use!",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Your Ubuntu-based Linux environment has been successfully configured.",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))
            
            // Restart Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(minOf(cardWidth, maxCardWidth))
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            settingsRepository.markSetupComplete()
                            onRestart()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UbuntuOrange,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Restart Now",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "The system will reboot and start your Linux environment",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
