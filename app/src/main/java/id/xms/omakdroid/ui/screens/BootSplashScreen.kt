package id.xms.omakdroid.ui.screens

import id.xms.omakdroid.R
import id.xms.omakdroid.core.installer.InstallerViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
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
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // App Icon (using drawable resource)
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "OmakDroid Logo",
                modifier = Modifier.size(120.dp),
                tint = Color(0xFFFF6B35)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Progress Bar and Status (20% from bottom)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp)
            ) {
                // Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .width(280.dp)
                        .height(4.dp),
                    color = Color(0xFFE0E0E0),
                    trackColor = Color(0xFF404040),
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Status Text
                Text(
                    text = statusText,
                    color = Color(0xFFB0B0B0),
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp
                )
            }
        }
    }
}
