package id.xms.omakdroid.ui.screens

import android.content.pm.ActivityInfo
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

    // Modern Ubuntu-inspired background
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF300A24), // Ubuntu Aubergine Dark
            Color(0xFF1E0616)  // Deeper Aubergine
        )
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
            .padding(32.dp)
    ) {
        val isLandscape = maxWidth > maxHeight
        val cardWidth = if (isLandscape) maxWidth * 0.6f else maxWidth * 0.9f
        val maxCardWidth = 600.dp

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            
            // Header
            Text(
                text = "OmakDroid Boot Manager",
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = if (isLandscape) 28.sp else 24.sp
            )
            
            Text(
                text = "GRUB version 1.00",
                color = Color.White.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main menu box
            Box(
                modifier = Modifier
                    .width(minOf(cardWidth, maxCardWidth))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    BootOptionItem(
                        text = "Ubuntu 24.04 LTS (OmakDroid)",
                        isSelected = selectedOption == 1,
                        onClick = {
                            isTimerActive = false
                            selectedOption = 1
                            onBootSequenceTriggered()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    BootOptionItem(
                        text = "Advanced options for Ubuntu",
                        isSelected = selectedOption == 2,
                        onClick = {
                            isTimerActive = false
                            selectedOption = 2
                            showOrientationDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    BootOptionItem(
                        text = "Memory test (memtest86+)",
                        isSelected = selectedOption == 3,
                        onClick = {
                            isTimerActive = false
                            selectedOption = 3
                            // TODO: Memory test placeholder
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer instructions
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Tap an option to select and boot.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isTimerActive) {
                        "Booting automatically in ${countdown}s."
                    } else {
                        "Timer cancelled. Waiting for selection."
                    },
                    color = if (isTimerActive) Color(0xFFE95420) else Color.White.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isTimerActive) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
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
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "Choose your preferred screen orientation for the OmakDroid environment.",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 15.sp
                )
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
                            containerColor = Color(0xFFE95420) // Ubuntu Orange
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Landscape", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                settingsRepository.saveOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                showOrientationDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE95420)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Portrait", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium)
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = { showOrientationDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", fontFamily = FontFamily.SansSerif)
                }
            },
            containerColor = Color(0xFF2A2A2A),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.8f),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun BootOptionItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFE95420).copy(alpha = 0.9f) else Color.Transparent,
        animationSpec = tween(150),
        label = "bg_color"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
        animationSpec = tween(150),
        label = "text_color"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontFamily = FontFamily.SansSerif,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 15.sp
        )
    }
}
