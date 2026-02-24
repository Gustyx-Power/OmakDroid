package id.xms.omakdroid.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111111))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Select Engine",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Choose how OmakDroid will run the Linux environment",
                fontSize = 16.sp,
                color = Color(0xFFD3D3D3)
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // PROOT Option
            EngineOption(
                title = "PRoot (Recommended)",
                description = "Runs without root access. Compatible with all devices. Slightly slower but more secure.",
                isSelected = viewModel.executionMode.value == MainViewModel.ExecutionMode.PROOT,
                isEnabled = true,
                onClick = { viewModel.executionMode.value = MainViewModel.ExecutionMode.PROOT }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // CHROOT Option
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
                    fontSize = 14.sp,
                    color = Color(0xFFFFAA00)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = UbuntuOrange
                    )
                ) {
                    Text("Back", fontSize = 18.sp)
                }
                
                Button(
                    onClick = onInstall,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UbuntuOrange
                    )
                ) {
                    Text("Install System", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
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
            .fillMaxWidth(0.8f)
            .border(
                width = 2.dp,
                color = if (isSelected) UbuntuOrange else if (isEnabled) Color.Gray else Color.DarkGray
            )
            .background(if (isSelected) Color(0xFF2C001E) else Color(0xFF1E1E1E))
            .clickable(enabled = isEnabled) { onClick() }
            .padding(24.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    enabled = isEnabled,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = UbuntuOrange,
                        unselectedColor = if (isEnabled) Color.Gray else Color.DarkGray
                    )
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) Color.White else Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                fontSize = 14.sp,
                color = if (isEnabled) Color(0xFFD3D3D3) else Color.DarkGray,
                modifier = Modifier.padding(start = 56.dp)
            )
        }
    }
}
