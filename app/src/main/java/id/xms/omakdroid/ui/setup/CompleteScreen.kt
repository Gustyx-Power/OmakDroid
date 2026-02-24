package id.xms.omakdroid.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111111))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Success Icon
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                modifier = Modifier.size(120.dp),
                tint = Color(0xFF4CAF50)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Title
            Text(
                text = "Installation Complete",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "OmakDroid is ready to use!",
                fontSize = 18.sp,
                color = Color(0xFFD3D3D3)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Your Ubuntu-based Linux environment has been successfully configured.",
                fontSize = 16.sp,
                color = Color(0xFFD3D3D3),
                modifier = Modifier.fillMaxWidth(0.6f)
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Restart Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        settingsRepository.markSetupComplete()
                        onRestart()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UbuntuOrange
                )
            ) {
                Text(
                    text = "Restart Now",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "The system will reboot and start your Linux environment",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}
