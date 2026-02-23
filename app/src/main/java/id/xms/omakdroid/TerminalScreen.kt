package id.xms.omakdroid

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun TerminalScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    var terminalHistory by remember { mutableStateOf(listOf<String>()) }
    var currentInput by remember { mutableStateOf("") }
    var isExecuting by remember { mutableStateOf(false) }
    
    val prootPath = remember { File(context.applicationInfo.nativeLibraryDir, "libproot.so").absolutePath }
    val rootfsPath = remember { File(context.filesDir, "rootfs").absolutePath }
    val tmpDir = remember { context.cacheDir.absolutePath }
    
    // Initialize terminal with welcome message
    LaunchedEffect(Unit) {
        val bootResult = withContext(Dispatchers.IO) {
            try {
                NativeEngine.bootLinuxKernel(prootPath, rootfsPath, tmpDir)
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }
        
        terminalHistory = listOf(
            bootResult,
            "",
            "Welcome to OmakDroid Terminal",
            "Type 'help' for available commands, or any Linux command to execute.",
            ""
        )
    }
    
    // Auto-scroll to bottom when history changes
    LaunchedEffect(terminalHistory.size) {
        if (terminalHistory.isNotEmpty()) {
            listState.animateScrollToItem(terminalHistory.size - 1)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Terminal output area
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                items(terminalHistory) { line ->
                    Text(
                        text = line,
                        color = Color(0xFF00FF00),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 14.sp
                    )
                }
            }
            
            // Input area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prompt
                Text(
                    text = "root@localhost:~# ",
                    color = Color(0xFF00FF00),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                
                // Input field
                BasicTextField(
                    value = currentInput,
                    onValueChange = { currentInput = it },
                    enabled = !isExecuting,
                    textStyle = TextStyle(
                        color = Color(0xFF00FF00),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    ),
                    cursorBrush = SolidColor(Color(0xFF00FF00)),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (currentInput.isNotBlank() && !isExecuting) {
                                val command = currentInput.trim()
                                currentInput = ""
                                isExecuting = true
                                
                                // Add command to history
                                terminalHistory = terminalHistory + "root@localhost:~# $command"
                                
                                // Execute command
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        try {
                                            NativeEngine.executeLinuxCommand(
                                                command,
                                                prootPath,
                                                rootfsPath,
                                                tmpDir
                                            )
                                        } catch (e: Exception) {
                                            "Error: ${e.message}\n"
                                        }
                                    }
                                    
                                    // Add result to history
                                    terminalHistory = terminalHistory + result.trimEnd()
                                    isExecuting = false
                                }
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
