package tech.estacionkus.camerastream.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.estacionkus.camerastream.ui.theme.*

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthenticated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface900)
    ) {
        // Top gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Brush.verticalGradient(
                    colors = listOf(CameraRed.copy(alpha = 0.1f), Color.Transparent)
                ))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(CameraRed.copy(alpha = 0.15f), MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.RadioButtonChecked,
                    contentDescription = null,
                    tint = CameraRed,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("CameraStream", style = MaterialTheme.typography.displaySmall, color = OnSurface)
            Text("by EstacionKUS", style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceMuted, letterSpacing = 2.sp)

            Spacer(Modifier.height(40.dp))

            // Toggle sign in / sign up
            var isSignUp by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .background(Surface700, MaterialTheme.shapes.medium)
                    .padding(4.dp)
            ) {
                listOf("Iniciar sesión" to false, "Crear cuenta" to true).forEach { (label, mode) ->
                    Surface(
                        color = if (isSignUp == mode) Surface800 else Color.Transparent,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.weight(1f)
                    ) {
                        TextButton(
                            onClick = { isSignUp = mode; viewModel.clearError() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                label,
                                color = if (isSignUp == mode) OnSurface else OnSurfaceMuted,
                                fontWeight = if (isSignUp == mode) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Email
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::setEmail,
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CameraRed, focusedLabelColor = CameraRed)
            )

            Spacer(Modifier.height(12.dp))

            // Password
            var showPass by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::setPassword,
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    if (isSignUp) viewModel.signUp() else viewModel.signIn()
                }),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CameraRed, focusedLabelColor = CameraRed)
            )

            // Error
            uiState.error?.let { err ->
                Spacer(Modifier.height(8.dp))
                Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { if (isSignUp) viewModel.signUp() else viewModel.signIn() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = CameraRed),
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (isSignUp) "Crear cuenta" else "Entrar", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Coupon section
            HorizontalDivider(color = Surface600)
            Spacer(Modifier.height(16.dp))
            Text("\u00bfTienes cupón de creador?", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceMuted)
            Spacer(Modifier.height(8.dp))

            var couponCode by remember { mutableStateOf("") }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = couponCode,
                    onValueChange = { couponCode = it.uppercase() },
                    label = { Text("Código") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CameraRed)
                )
                Button(
                    onClick = { viewModel.redeemCoupon(couponCode) },
                    enabled = couponCode.isNotBlank() && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Surface700)
                ) {
                    Text("Aplicar")
                }
            }

            uiState.couponMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = if (it.contains("error", ignoreCase = true) || it.contains("inválido", ignoreCase = true))
                    MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
