package id.xms.omakdroid.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.omakdroid.ui.theme.UbuntuOrange

@Composable
fun DesktopScreen(
    viewModel: SetupViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(minOf(cardWidth, maxCardWidth))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .padding(32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Choose Desktop Environment",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Select the interface you want to use",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // KDE Option
                    DesktopOption(
                        title = "KDE Plasma",
                        description = "Modern, feature-rich desktop with beautiful animations",
                        isSelected = viewModel.selectedDE.value == SetupViewModel.DesktopEnv.KDE,
                        onClick = { viewModel.selectedDE.value = SetupViewModel.DesktopEnv.KDE }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // XFCE Option
                    DesktopOption(
                        title = "XFCE",
                        description = "Lightweight, fast, and resource-efficient desktop",
                        isSelected = viewModel.selectedDE.value == SetupViewModel.DesktopEnv.XFCE,
                        onClick = { viewModel.selectedDE.value = SetupViewModel.DesktopEnv.XFCE }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // CLI Only Option
                    DesktopOption(
                        title = "CLI Only",
                        description = "Command-line interface only, no graphical desktop",
                        isSelected = viewModel.selectedDE.value == SetupViewModel.DesktopEnv.NONE,
                        onClick = { viewModel.selectedDE.value = SetupViewModel.DesktopEnv.NONE }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = onBack,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Back")
                        }
                        
                        Button(
                            onClick = onNext,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = UbuntuOrange
                            )
                        ) {
                            Text("Continue", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DesktopOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) UbuntuOrange.copy(alpha = 0.2f)
                else Color.White.copy(alpha = 0.05f)
            )
            .border(
                width = 2.dp,
                color = if (isSelected) UbuntuOrange else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = title,
                fontFamily = FontFamily.SansSerif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) UbuntuOrange else Color.White
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
