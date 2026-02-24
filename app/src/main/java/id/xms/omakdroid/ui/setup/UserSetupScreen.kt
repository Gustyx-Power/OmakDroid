package id.xms.omakdroid.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.omakdroid.ui.theme.UbuntuOrange

@Composable
fun UserSetupScreen(
    viewModel: SetupViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
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
                text = "Who are you?",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Create your user account for the Linux environment",
                fontSize = 16.sp,
                color = Color(0xFFD3D3D3)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Full Name
            OutlinedTextField(
                value = viewModel.fullName.value,
                onValueChange = { viewModel.fullName.value = it },
                label = { Text("Your name") },
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UbuntuOrange,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = UbuntuOrange,
                    unfocusedLabelColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Hostname
            OutlinedTextField(
                value = viewModel.hostname.value,
                onValueChange = { viewModel.hostname.value = it },
                label = { Text("Computer's name") },
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UbuntuOrange,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = UbuntuOrange,
                    unfocusedLabelColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Username
            OutlinedTextField(
                value = viewModel.username.value,
                onValueChange = { viewModel.username.value = it.lowercase() },
                label = { Text("Pick a username") },
                supportingText = { 
                    Text(
                        "Lowercase letters, numbers, hyphens, and underscores only",
                        fontSize = 12.sp
                    )
                },
                isError = viewModel.username.value.isNotEmpty() && 
                         !viewModel.username.value.matches(Regex("^[a-z_][a-z0-9_-]*$")),
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UbuntuOrange,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = UbuntuOrange,
                    unfocusedLabelColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password
            OutlinedTextField(
                value = viewModel.password.value,
                onValueChange = { viewModel.password.value = it },
                label = { Text("Choose a password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UbuntuOrange,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = UbuntuOrange,
                    unfocusedLabelColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Confirm Password
            OutlinedTextField(
                value = viewModel.confirmPassword.value,
                onValueChange = { viewModel.confirmPassword.value = it },
                label = { Text("Confirm password") },
                visualTransformation = PasswordVisualTransformation(),
                isError = viewModel.confirmPassword.value.isNotEmpty() && 
                         viewModel.password.value != viewModel.confirmPassword.value,
                supportingText = {
                    if (viewModel.confirmPassword.value.isNotEmpty() && 
                        viewModel.password.value != viewModel.confirmPassword.value) {
                        Text("Passwords do not match", color = Color.Red)
                    }
                },
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UbuntuOrange,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = UbuntuOrange,
                    unfocusedLabelColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Navigation Buttons
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
                    onClick = onNext,
                    enabled = viewModel.isUserSetupValid(),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UbuntuOrange,
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    Text("Next", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
