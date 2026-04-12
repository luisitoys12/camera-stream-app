package tech.estacionkus.camerastream.ui.screens.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tech.estacionkus.camerastream.data.auth.AuthRepository
import tech.estacionkus.camerastream.data.auth.LicenseRepository
import tech.estacionkus.camerastream.data.auth.Supabase
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val couponMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val licenseRepository: LicenseRepository
) : ViewModel() {

    private val TAG = "AuthViewModel"
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        if (authRepository.isLoggedIn) {
            _uiState.value = _uiState.value.copy(isAuthenticated = true)
        }
    }

    fun setEmail(v: String) { _uiState.value = _uiState.value.copy(email = v) }
    fun setPassword(v: String) { _uiState.value = _uiState.value.copy(password = v) }
    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }

    fun signIn() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        try {
            authRepository.signIn(_uiState.value.email, _uiState.value.password)
            _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error al iniciar sesion")
        }
    }

    fun signUp() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        try {
            authRepository.signUp(_uiState.value.email, _uiState.value.password)
            _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error al crear cuenta")
        }
    }

    /**
     * Initiates Google OAuth flow via Supabase.
     * Opens browser to Supabase's Google OAuth URL for sign-in.
     */
    fun signInWithGoogle(context: Context) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                // Build the Supabase Google OAuth URL
                val supabaseUrl = Supabase.client.supabaseUrl
                val redirectUrl = "tech.estacionkus.camerastream://auth/callback"
                val googleOAuthUrl = "${supabaseUrl}/auth/v1/authorize?provider=google&redirect_to=$redirectUrl"

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleOAuthUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                Log.e(TAG, "Google OAuth error: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se pudo iniciar sesion con Google: ${e.message}"
                )
            }
        }
    }

    fun resetPassword(email: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        try {
            authRepository.resetPassword(email)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = null,
                couponMessage = "Se envio un link de recuperacion a $email"
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
        }
    }

    fun redeemCoupon(code: String) = viewModelScope.launch {
        if (!authRepository.isLoggedIn) {
            _uiState.value = _uiState.value.copy(couponMessage = "Primero inicia sesion para canjear el cupon")
            return@launch
        }
        _uiState.value = _uiState.value.copy(isLoading = true, couponMessage = null)
        val result = licenseRepository.redeemCoupon(code)
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            couponMessage = result.message ?: result.error ?: "Respuesta inesperada",
            isAuthenticated = result.success
        )
    }
}
