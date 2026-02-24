package id.xms.omakdroid

import android.content.Context
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
import id.xms.omakdroid.core.engine.NativeEngine
import id.xms.omakdroid.core.engine.RootfsPathResolver
import id.xms.omakdroid.ui.screens.*
import id.xms.omakdroid.ui.theme.OmakDroidTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)        
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
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
    
    NavHost(navController = navController, startDestination = "boot_splash") {
        composable("boot_splash") {
            BootSplashScreen(
                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                onBootComplete = {
                    navController.navigate("fake_bios") {
                        popUpTo("boot_splash") { inclusive = true }
                    }
                }
            )
        }
        
        composable("fake_bios") {
            FakeBiosScreen(
                onBootComplete = {
                    navController.navigate("grub") {
                        popUpTo("fake_bios") { inclusive = true }
                    }
                }
            )
        }
        
        composable("grub") {
            GrubScreen(
                onBootSequenceTriggered = {
                    navController.navigate("boot_log") {
                        popUpTo("grub") { inclusive = true }
                    }
                }
            )
        }
        
        composable("boot_log") {
            BootLogScreen(
                onBootComplete = {
                    navController.navigate("terminal") {
                        popUpTo("boot_log") { inclusive = true }
                    }
                }
            )
        }
        
        composable("terminal") {
            TerminalScreen()
        }
    }
}

@Composable
fun FakeBiosScreen(
    viewModel: BiosViewModel = viewModel(),
    onBootComplete: () -> Unit
) {
    AmericanMegatrendsBiosScreen(
        viewModel = viewModel,
        onBootComplete = onBootComplete
    )
}

@Composable
fun DesktopScreen() {
    val context = LocalContext.current
    var rustEngineStatus by remember { mutableStateOf("Initializing Rust Engine...") }
    var testResult by remember { mutableStateOf("Booting OmakDroid Kernel...") }
    var testColor by remember { mutableStateOf(Color.Yellow) }
    
    LaunchedEffect(Unit) {
        try {
            rustEngineStatus = NativeEngine.pingEngine()
        } catch (e: Exception) {
            rustEngineStatus = "Rust Engine Error: ${e.message}"
        }
        
        kotlinx.coroutines.delay(500)
        val result = bootOmakDroidKernel(context)
        testResult = result
        testColor = if (result.contains("Error") || result.contains("killed") || result.contains("Bad ELF")) {
            Color.Red
        } else {
            Color(0xFF00FF00)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(24.dp),
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                color = Color.Gray,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "RUST ENGINE STATUS",
                color = Color.Cyan,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = rustEngineStatus,
                color = if (rustEngineStatus.contains("ONLINE")) Color(0xFF00FF00) else Color.Red,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                color = Color.Gray,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "KERNEL INITIALIZATION",
                color = Color.Cyan,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = testResult,
                color = testColor,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}

/**
 * Boot the OmakDroid kernel using PRoot and the Rust engine.
 */
fun bootOmakDroidKernel(context: Context): String {
    return try {
        val prootPath = File(context.applicationInfo.nativeLibraryDir, "libproot.so").absolutePath
        val rootfsPath = RootfsPathResolver.getTrueRootfsPath(context)
        
        val prootFile = File(prootPath)
        if (!prootFile.exists()) {
            return "Error: PRoot binary not found at $prootPath"
        }
        
        if (!RootfsPathResolver.rootfsExists(context)) {
            return "Error: Rootfs not found"
        }
        
        android.util.Log.i("OmakKernel", "Booting Linux environment via Rust Engine...")
        android.util.Log.i("OmakKernel", "PRoot: $prootPath")
        android.util.Log.i("OmakKernel", "Rootfs: $rootfsPath")
        
        val result = NativeEngine.bootLinuxKernel(
            prootPath,
            rootfsPath,
            context.cacheDir.absolutePath
        )
        
        android.util.Log.i("OmakKernel", "Rust Engine Result: $result")
        
        result
        
    } catch (e: Exception) {
        android.util.Log.e("OmakKernel", "Exception during kernel boot", e)
        "Error: Exception occurred during kernel boot\n" +
        "Type: ${e.javaClass.simpleName}\n" +
        "Message: ${e.message}"
    }
}
