package id.xms.omakdroid.ui.screens

import id.xms.omakdroid.BiosViewModel

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun AmericanMegatrendsBiosScreen(
    viewModel: BiosViewModel,
    onBootComplete: () -> Unit
) {
    val context = LocalContext.current
    
    var blinkingCursor by remember { mutableStateOf(true) }
    
    // Fetch hardware info
    val biosInfo = remember { getBiosHardwareInfo(context) }
    
    // Blinking cursor animation
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            blinkingCursor = !blinkingCursor
        }
    }
    
    // Auto-navigate after 3 seconds (POST complete)
    LaunchedEffect(Unit) {
        delay(3000)
        onBootComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Logo Section
            Text(
                text = "    /\\",
                color = Color(0xFFFF6B35),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "   /  \\    OmakDroid",
                color = Color(0xFFFF6B35),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "  /____\\   BIOS",
                color = Color(0xFFFF6B35),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // BIOS Information
            Text(
                text = "OMAKBIOS(C)2026 XMS Community, Inc.",
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = biosInfo.motherboard,
                color = Color(0xFFD3D3D3),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // CPU Information
            Text(
                text = "CPU: ${biosInfo.cpu} Processor",
                color = Color(0xFFD3D3D3),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            Text(
                text = "Speed: ${biosInfo.cpuSpeed}",
                color = Color(0xFFD3D3D3),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Memory Information
            Text(
                text = "Total Memory: ${biosInfo.totalMemory}",
                color = Color(0xFFD3D3D3),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // USB Devices
            Text(
                text = "USB Devices total: 0 Drive, 1 Keyboard, 1 Mouse, 0 Hub",
                color = Color(0xFFD3D3D3),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Storage Detection
            Text(
                text = "Detected ATA/ATAPI Devices...",
                color = Color(0xFFD3D3D3),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "NVMe_1: UFS Host Controller (${biosInfo.storageSize})",
                color = Color(0xFFD3D3D3),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // POST Status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Performing POST (Power-On Self-Test)...",
                    color = Color(0xFF00FF00),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                if (blinkingCursor) {
                    Text(
                        text = "_",
                        color = Color(0xFF00FF00),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Footer
            Text(
                text = "Please enter setup to recover BIOS setting.",
                color = Color(0xFFD3D3D3),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
            Text(
                text = "When RAID configuration was built, ensure to set SATA Configuration to RAID mode.",
                color = Color(0xFFD3D3D3),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Press F1 to Run SETUP",
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class BiosHardwareInfo(
    val motherboard: String,
    val cpu: String,
    val cpuSpeed: String,
    val totalMemory: String,
    val storageSize: String
)

fun getBiosHardwareInfo(context: Context): BiosHardwareInfo {
    // Motherboard
    val manufacturer = Build.MANUFACTURER.uppercase()
    val model = Build.MODEL.uppercase()
    val motherboard = "$manufacturer $model GAMING ACPI BIOS Revision 1.0"
    
    // CPU
    val cpu = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Build.SOC_MODEL ?: Build.HARDWARE
    } else {
        Build.HARDWARE
    }
    
    // CPU Speed - Try to read from system, fallback to default
    val cpuSpeed = try {
        val cpuFreqFile = File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
        if (cpuFreqFile.exists() && cpuFreqFile.canRead()) {
            val freqKHz = cpuFreqFile.readText().trim().toLongOrNull() ?: 0L
            val freqMHz = freqKHz / 1000
            "${freqMHz}MHz"
        } else {
            // Fallback based on device tier
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            val totalRamGB = memoryInfo.totalMem / (1024 * 1024 * 1024)
            
            when {
                totalRamGB >= 12 -> "3000MHz"  // High-end
                totalRamGB >= 6 -> "2400MHz"   // Mid-range
                else -> "1800MHz"              // Low-end
            }
        }
    } catch (e: Exception) {
        "2400MHz"  // Safe default
    }
    
    // Total Memory
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    val totalRamMB = memoryInfo.totalMem / (1024 * 1024)
    val totalMemory = "${totalRamMB}MB (LPDDR5)"
    
    // Storage Size
    val storageSize = try {
        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalBytes = statFs.blockCountLong * statFs.blockSizeLong
        val totalGB = totalBytes / (1024 * 1024 * 1024)
        "${totalGB}GB"
    } catch (e: Exception) {
        "128GB"  // Fallback
    }
    
    return BiosHardwareInfo(
        motherboard = motherboard,
        cpu = cpu,
        cpuSpeed = cpuSpeed,
        totalMemory = totalMemory,
        storageSize = storageSize
    )
}
