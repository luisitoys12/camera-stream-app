package tech.estacionkus.camerastream.data.auth

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor() {
    private val auth get() = Supabase.client.auth

    val currentUser get() = auth.currentUserOrNull()
    val isLoggedIn get() = currentUser != null
    val sessionFlow = auth.sessionStatus

    suspend fun signUp(email: String, password: String) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() = auth.signOut()

    fun getAccessToken(): String? = auth.currentSessionOrNull()?.accessToken
}
