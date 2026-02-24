package id.xms.omakdroid.ui.screens

import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.omakdroid.core.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GrubScreen(
    settingsRepository: SettingsRepository,
    onBootSequenceTriggered: () -> Unit
) {
    var countdown by remember { mutableStateOf(30) }
    var isTimerActive by remember { mutableStateOf(true) }
    var selectedOption by remember { mutableStateOf(1) }
    var showOrientationDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(isTimerActive) {
        if (isTimerActive) {
            while (countdown > 0 && isTimerActive) {
                delay(1000L)
                countdown--
            }
            if (isTimerActive && countdown == 0) {
                onBootSequenceTriggered()
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "GNU GRUB  version 2.06",
                color = Color(0xFFD3D3D3),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Main menu box
            Column(
                modifier = Modifier
                    .width(500.dp)
                    .border(2.dp, Color.White)
                    .padding(16.dp)
            ) {
                // Option 1
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (selectedOption == 1) Color.White else Color.Black)
                        .clickable {
                            isTimerActive = false
                            selectedOption = 1
                            onBootSequenceTriggered()
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Text(
                        text = if (selectedOption == 1) "*Ubuntu (OmakDroid Kernel)" else " Ubuntu (OmakDroid Kernel)",
                        color = if (selectedOption == 1) Color.Black else Color(0xFFD3D3D3),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Advanced Options
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (selectedOption == 2) Color.White else Color.Black)
                        .clickable {
                            isTimerActive = false
                            selectedOption = 2
                            showOrientationDialog = true
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Text(
                        text = if (selectedOption == 2) "*Advanced options for Ubuntu" else " Advanced options for Ubuntu",
                        color = if (selectedOption == 2) Color.Black else Color(0xFFD3D3D3),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Option 3
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (selectedOption == 3) Color.White else Color.Black)
                        .clickable {
                            isTimerActive = false
                            selectedOption = 3
                            // TODO: Memory test placeholder
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Text(
                        text = if (selectedOption == 3) "*Memory test (memtest86+)" else " Memory test (memtest86+)",
                        color = if (selectedOption == 3) Color.Black else Color(0xFFD3D3D3),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Footer instructions
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Use the ↑ and ↓ keys to select which entry is highlighted.",
                    color = Color(0xFFD3D3D3),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Press enter to boot the selected OS.",
                    color = Color(0xFFD3D3D3),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isTimerActive) {
                        "The highlighted entry will be executed automatically in ${countdown}s."
                    } else {
                        "Timer cancelled. Press Enter or tap an option to continue."
                    },
                    color = Color(0xFFD3D3D3),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
    }
    
    // Orientation Selection Dialog
    if (showOrientationDialog) {
        AlertDialog(
            onDismissRequest = { showOrientationDialog = false },
            title = {
                Text(
                    text = "Display Orientation",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Choose your preferred screen orientation.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                settingsRepository.saveOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                                showOrientationDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Text("Landscape", fontFamily = FontFamily.Monospace)
                    }
                    
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                settingsRepository.saveOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                showOrientationDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Text("Portrait", fontFamily = FontFamily.Monospace)
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = { showOrientationDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray
                    )
                ) {
                    Text("Cancel", fontFamily = FontFamily.Monospace)
                }
            },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFD3D3D3)
        )
    }
}
