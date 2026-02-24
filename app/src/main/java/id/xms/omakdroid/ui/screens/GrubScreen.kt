package id.xms.omakdroid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun GrubScreen(
    onBootSequenceTriggered: () -> Unit
) {
    var countdown by remember { mutableStateOf(3) }
    
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
        onBootSequenceTriggered()
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
                // Option 1 - Highlighted
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Text(
                        text = "*Ubuntu (OmakDroid Kernel)",
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Option 2
                Text(
                    text = " Advanced options for Ubuntu",
                    color = Color(0xFFD3D3D3),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Option 3
                Text(
                    text = " Memory test (memtest86+)",
                    color = Color(0xFFD3D3D3),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                )
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
                    text = "The highlighted entry will be executed automatically in ${countdown}s.",
                    color = Color(0xFFD3D3D3),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
    }
}
