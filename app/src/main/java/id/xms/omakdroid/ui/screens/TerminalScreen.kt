package id.xms.omakdroid.ui.screens

import id.xms.omakdroid.core.engine.NativeEngine
import id.xms.omakdroid.core.engine.RootfsPathResolver
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
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
    var username by remember { mutableStateOf<String?>(null) }
    var hostname by remember { mutableStateOf("localhost") }
    
    val prootPath = remember { File(context.applicationInfo.nativeLibraryDir, "libproot.so").absolutePath }
    val rootfsPath = remember { RootfsPathResolver.getTrueRootfsPath(context) }
    val tmpDir = remember { context.cacheDir.absolutePath }
    
    // Load username from settings
    LaunchedEffect(Unit) {
        val settingsRepository = id.xms.omakdroid.core.SettingsRepository(context)
        settingsRepository.usernameFlow.collect { savedUsername ->
            username = savedUsername
        }
    }
    
    // Initialize terminal with welcome message and dynamic login
    LaunchedEffect(username) {
        if (username != null) {
            val bootResult = withContext(Dispatchers.IO) {
                try {
                    // Boot with user login
                    val loginCommand = "/bin/su - $username"
                    android.util.Log.i("TerminalScreen", "Booting with user login: $loginCommand")
                    NativeEngine.bootLinuxKernel(prootPath, rootfsPath, tmpDir)
                } catch (e: Exception) {
                    "Error: ${e.message}"
                }
            }
            
            terminalHistory = listOf(
                bootResult,
                "",
                "Welcome to OmakDroid Terminal",
                "Logged in as: $username",
                "Type 'help' for available commands, or any Linux command to execute.",
                ""
            )
        } else {
            // Fallback to root if no username configured
            val bootResult = withContext(Dispatchers.IO) {
                try {
                    android.util.Log.i("TerminalScreen", "Booting as root (no username configured)")
                    NativeEngine.bootLinuxKernel(prootPath, rootfsPath, tmpDir)
                } catch (e: Exception) {
                    "Error: ${e.message}"
                }
            }
            
            terminalHistory = listOf(
                bootResult,
                "",
                "Welcome to OmakDroid Terminal",
                "Logged in as: root (setup not completed)",
                "Type 'help' for available commands, or any Linux command to execute.",
                ""
            )
        }
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
                val promptText = if (username != null) {
                    "$username@$hostname:~$ "
                } else {
                    "root@$hostname:~# "
                }
                
                Text(
                    text = promptText,
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
                                
                                // Dynamic prompt for history
                                val promptText = if (username != null) {
                                    "$username@$hostname:~$ $command"
                                } else {
                                    "root@$hostname:~# $command"
                                }
                                
                                // Add command to history
                                terminalHistory = terminalHistory + promptText
                                
                                // Wrap command with su if username exists
                                val actualCommand = if (username != null) {
                                    "/bin/su - $username -c '$command'"
                                } else {
                                    command
                                }
                                
                                // Set up streaming callback
                                NativeEngine.onTerminalOutput = { line ->
                                    terminalHistory = terminalHistory + line
                                }
                                
                                // Execute command in background with streaming
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        NativeEngine.executeLinuxCommand(
                                            actualCommand,
                                            prootPath,
                                            rootfsPath,
                                            tmpDir
                                        )
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            terminalHistory = terminalHistory + "Error: ${e.message}"
                                        }
                                    } finally {
                                        withContext(Dispatchers.Main) {
                                            isExecuting = false
                                            NativeEngine.onTerminalOutput = null
                                        }
                                    }
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
