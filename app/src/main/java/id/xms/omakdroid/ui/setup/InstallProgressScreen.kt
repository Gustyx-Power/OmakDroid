package id.xms.omakdroid.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.omakdroid.ui.theme.UbuntuOrange
import kotlinx.coroutines.delay

@Composable
fun InstallProgressScreen(
    viewModel: SetupViewModel,
    onComplete: () -> Unit
) {
    val installSteps = listOf(
        "Preparing installation..." to 0.1f,
        "Copying system files..." to 0.3f,
        "Configuring hardware..." to 0.5f,
        "Setting up user account..." to 0.7f,
        "Installing packages..." to 0.85f,
        "Finalizing configuration..." to 1.0f
    )
    
    LaunchedEffect(Unit) {
        viewModel.resetInstallation()
        
        for ((statusText, progress) in installSteps) {
            viewModel.installStatusText.value = statusText

            val startProgress = viewModel.installProgress.floatValue
            val steps = 20
            val increment = (progress - startProgress) / steps
            
            repeat(steps) {
                delay(100L)
                viewModel.installProgress.floatValue += increment
            }
            
            delay(500L)
        }
        
        delay(1000L)
        onComplete()
    }
    
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
            // Title
            Text(
                text = "Installing OmakDroid",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Please wait while we set up your Linux environment",
                fontSize = 16.sp,
                color = Color(0xFFD3D3D3)
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Circular Progress Indicator
            CircularProgressIndicator(
                progress = { viewModel.installProgress.floatValue },
                modifier = Modifier.size(120.dp),
                color = UbuntuOrange,
                strokeWidth = 8.dp,
                trackColor = Color(0xFF333333)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Progress Percentage
            Text(
                text = "${(viewModel.installProgress.floatValue * 100).toInt()}%",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = UbuntuOrange
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Linear Progress Bar
            LinearProgressIndicator(
                progress = { viewModel.installProgress.floatValue },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(8.dp),
                color = UbuntuOrange,
                trackColor = Color(0xFF333333)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Status Text
            Text(
                text = viewModel.installStatusText.value,
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}
