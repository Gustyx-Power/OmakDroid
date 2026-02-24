package id.xms.omakdroid.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import id.xms.omakdroid.MainViewModel
import id.xms.omakdroid.core.SystemChecker
import id.xms.omakdroid.ui.theme.UbuntuOrange

@Composable
fun EngineScreen(
    viewModel: SetupViewModel,
    onInstall: () -> Unit,
    onBack: () -> Unit
) {
    val isRootAvailable = remember { SystemChecker.isRootAvailable() }

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
                .verticalScroll(rememberScrollState())
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
                    .padding(32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Title
                    Text(
                        text = "Select Engine",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Choose how OmakDroid will run the Linux environment",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // PRoot Option
                    EngineOption(
                        title = "PRoot (Recommended)",
                        description = "Runs without root access. Compatible with all devices. Slightly slower but more secure.",
                        isSelected = viewModel.executionMode.value == MainViewModel.ExecutionMode.PROOT,
                        isEnabled = true,
                        onClick = { viewModel.executionMode.value = MainViewModel.ExecutionMode.PROOT }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Chroot Option
                    EngineOption(
                        title = "Chroot (Native)",
                        description = "Requires root access. Faster performance with native system calls. Only available on rooted devices.",
                        isSelected = viewModel.executionMode.value == MainViewModel.ExecutionMode.CHROOT,
                        isEnabled = isRootAvailable,
                        onClick = {
                            if (isRootAvailable) {
                                viewModel.executionMode.value = MainViewModel.ExecutionMode.CHROOT
                            }
                        }
                    )

                    if (!isRootAvailable) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Root access not detected. Chroot mode is unavailable.",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 13.sp,
                            color = Color(0xFFFF9F0A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier.width(minOf(cardWidth, maxCardWidth)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White.copy(alpha = 0.8f)
                    ),
                    border = null
                ) {
                    Text("Back", fontFamily = FontFamily.SansSerif, fontSize = 15.sp)
                }

                Button(
                    onClick = onInstall,
                    modifier = Modifier.height(48.dp).width(140.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UbuntuOrange,
                        contentColor = Color.White
                    )
                ) {
                    Text("Install System", fontFamily = FontFamily.SansSerif, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EngineOption(
    title: String,
    description: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) UbuntuOrange.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
            .border(
                width = 1.dp,
                color = if (isSelected) UbuntuOrange else if (isEnabled) Color.White.copy(alpha = 0.15f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = isEnabled) { onClick() }
            .padding(20.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    enabled = isEnabled,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = UbuntuOrange,
                        unselectedColor = if (isEnabled) Color.White.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.2f)
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEnabled) Color.White else Color.White.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                color = if (isEnabled) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.padding(start = 48.dp)
            )
        }
    }
}
