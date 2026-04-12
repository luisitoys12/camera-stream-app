package tech.estacionkus.camerastream.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var displayName by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo area
            Icon(
                Icons.Default.Videocam,
                contentDescription = null,
                tint = Color(0xFFE53935),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "CameraStream",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Radio Broadcast Beta",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(0.6f)
            )
            Spacer(Modifier.height(32.dp))

            // Tab row
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                            Text("Login", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                        }
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                            Text("Registro", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Display name (register only)
                    AnimatedVisibility(visible = selectedTab == 1) {
                        Column {
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text("Nombre", color = Color.White.copy(0.7f)) },
                                leadingIcon = { Icon(Icons.Default.Person, null, tint = Color.White.copy(0.5f)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = authFieldColors()
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    // Email
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.setEmail(it) },
                        label = { Text("Email", color = Color.White.copy(0.7f)) },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = Color.White.copy(0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = authFieldColors()
                    )
                    Spacer(Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.setPassword(it) },
                        label = { Text("Contrasena", color = Color.White.copy(0.7f)) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color.White.copy(0.5f)) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null, tint = Color.White.copy(0.5f)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = authFieldColors()
                    )
                    Spacer(Modifier.height(8.dp))

                    // Forgot password
                    if (selectedTab == 0) {
                        TextButton(
                            onClick = { showForgotPassword = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Olvidaste tu contrasena?", color = Color(0xFF64B5F6), fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Error
                    uiState.error?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE53935).copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                error,
                                color = Color(0xFFEF9A9A),
                                modifier = Modifier.padding(12.dp),
                                fontSize = 13.sp
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // Login/Register button
                    Button(
                        onClick = {
                            if (selectedTab == 0) viewModel.signIn()
                            else viewModel.signUp()
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !uiState.isLoading && uiState.email.isNotBlank() && uiState.password.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                if (selectedTab == 0) "Iniciar Sesion" else "Crear Cuenta",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Google OAuth divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(0.3f))
                        Text("  o  ", color = Color.White.copy(0.5f), fontSize = 13.sp)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(0.3f))
                    }

                    Spacer(Modifier.height(16.dp))

                    // Google button - Supabase Google OAuth
                    OutlinedButton(
                        onClick = {
                            viewModel.signInWithGoogle(context)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        border = BorderStroke(1.dp, Color.White.copy(0.3f)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.AccountCircle, null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Continuar con Google", color = Color.White)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Skip for now
            TextButton(onClick = onAuthenticated) {
                Text("Continuar sin cuenta", color = Color.White.copy(0.5f))
            }
        }
    }

    // Forgot password dialog
    if (showForgotPassword) {
        AlertDialog(
            onDismissRequest = { showForgotPassword = false },
            title = { Text("Recuperar Contrasena") },
            text = {
                Column {
                    Text("Ingresa tu email para recibir un link de recuperacion")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.resetPassword(forgotEmail)
                    showForgotPassword = false
                }) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPassword = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White.copy(0.8f),
    cursorColor = Color(0xFFE53935),
    focusedBorderColor = Color(0xFFE53935),
    unfocusedBorderColor = Color.White.copy(0.3f),
    focusedLabelColor = Color(0xFFE53935),
    unfocusedLabelColor = Color.White.copy(0.5f)
)
