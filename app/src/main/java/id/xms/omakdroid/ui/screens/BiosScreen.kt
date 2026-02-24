package id.xms.omakdroid.ui.screens

import id.xms.omakdroid.BiosViewModel

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.omakdroid.R
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun AmericanMegatrendsBiosScreen(
    viewModel: BiosViewModel,
    onBootComplete: () -> Unit
) {
    val context = LocalContext.current
    var blinkingCursor by remember { mutableStateOf(true) }
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

    // Modern macOS/Ubuntu blur-like background
    val bgBrush = Brush.radialGradient(
        colors = listOf(
            Color(0xFF2D2D35), // Lighter dark center
            Color(0xFF1A1A1E)  // Deep dark edge
        )
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
            .padding(24.dp)
    ) {
        val isLandscape = maxWidth > maxHeight
        
        // Outer Glass Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
        ) {
            if (isLandscape) {
                // Landscape: Sidebar + Content
                Row(modifier = Modifier.fillMaxSize()) {
                    BiosSidebar(biosInfo = biosInfo, modifier = Modifier.weight(1f).fillMaxHeight())
                    Divider(
                        color = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxHeight().width(1.dp)
                    )
                    BiosMainContent(
                        biosInfo = biosInfo,
                        blinkingCursor = blinkingCursor,
                        modifier = Modifier.weight(2f).fillMaxHeight().padding(24.dp)
                    )
                }
            } else {
                // Portrait: Stacked
                Column(modifier = Modifier.fillMaxSize()) {
                    BiosSidebar(biosInfo = biosInfo, modifier = Modifier.fillMaxWidth().wrapContentHeight())
                    Divider(
                        color = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth().height(1.dp)
                    )
                    BiosMainContent(
                        biosInfo = biosInfo,
                        blinkingCursor = blinkingCursor,
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BiosSidebar(biosInfo: BiosHardwareInfo, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "OmakDroid Setup",
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFE95420) // Ubuntu Orange
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "OmakDroid UEFI BIOS(C)2026 XMS, Com.",
            color = Color.White,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = "Version 2.0.4",
            color = Color.White.copy(alpha = 0.5f),
            fontFamily = FontFamily.SansSerif,
            fontSize = 12.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Quick Stats vertical layout
        BiosStatBlock(label = "CPU Speed", value = biosInfo.cpuSpeed)
        Spacer(modifier = Modifier.height(16.dp))
        BiosStatBlock(label = "Memory", value = biosInfo.totalMemory)
        Spacer(modifier = Modifier.height(16.dp))
        BiosStatBlock(label = "Primary Storage", value = "NVMe 1")
    }
}

@Composable
private fun BiosStatBlock(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontFamily = FontFamily.SansSerif
        )
        Text(
            text = value,
            color = Color(0xFFE95420),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Composable
private fun BiosMainContent(
    biosInfo: BiosHardwareInfo,
    blinkingCursor: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "System Information",
            color = Color.White,
            fontFamily = FontFamily.SansSerif,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        InfoRow("Motherboard", biosInfo.motherboard)
        InfoRow("Processor", biosInfo.cpu)
        InfoRow("Total Memory", biosInfo.totalMemory)
        InfoRow("Storage Size", biosInfo.storageSize)
        InfoRow("USB Devices", "0 Drive, 1 Keyboard, 1 Mouse, 0 Hub")
        
        Spacer(modifier = Modifier.weight(1f))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "> Performing POST (Power-On Self-Test)...",
                    color = Color(0xFF32CD32),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
                if (blinkingCursor) {
                    Text(
                        text = " █",
                        color = Color(0xFF32CD32),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Press F1 to enter setup",
                color = Color.White.copy(alpha = 0.6f),
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp
            )
            val ubuntuOrange = Color(0xFFE95420)
            Row {
                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(ubuntuOrange))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(Color.Gray))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(Color.Gray))
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontFamily = FontFamily.SansSerif,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontFamily = FontFamily.SansSerif,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
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
    val motherboard = "$manufacturer $model ACPI BIOS Ver 1.0"
    
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
                totalRamGB >= 12 -> "3000MHz"  
                totalRamGB >= 6 -> "2400MHz"   
                else -> "1800MHz"              
            }
        }
    } catch (e: Exception) {
        "2400MHz"  
    }
    
    // Total Memory
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    val totalRamMB = memoryInfo.totalMem / (1024 * 1024)
    val totalMemory = "${totalRamMB}MB"
    
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
