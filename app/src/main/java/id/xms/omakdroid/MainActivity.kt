package id.xms.omakdroid

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import id.xms.omakdroid.ui.theme.OmakDroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)        
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        // Enable immersive mode
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            OmakDroidTheme {
                OmakDroidApp()
            }
        }
    }
}

@Composable
fun OmakDroidApp() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "fake_bios") {
        composable("fake_bios") {
            FakeBiosScreen(
                onBootComplete = {
                    navController.navigate("desktop") {
                        popUpTo("fake_bios") { inclusive = true }
                    }
                }
            )
        }
        
        composable("desktop") {
            DesktopScreen()
        }
    }
}

@Composable
fun FakeBiosScreen(
    viewModel: BiosViewModel = viewModel(),
    onBootComplete: () -> Unit
) {
    val context = LocalContext.current
    val logs by viewModel.logs.collectAsState()
    val extractionComplete by viewModel.extractionComplete.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.extractAssets(context)
    }
    
    LaunchedEffect(extractionComplete) {
        if (extractionComplete) {
            kotlinx.coroutines.delay(2000)
            onBootComplete()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = logs,
            color = Color(0xFF00FF00),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        )
    }
}

@Composable
fun DesktopScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "OmakDroid Kernel Ready",
                color = Color(0xFF00FF00),
                fontFamily = FontFamily.Monospace,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Kiosk Mode Active",
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp
            )
        }
    }
}
