package com.example.promisclamping.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.promisclamping.data.auth.InvalidCredentialsException
import com.example.promisclamping.domain.auth.AuthRepository
import com.example.promisclamping.domain.auth.AuthState
import com.example.promisclamping.domain.auth.LoginUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(viewModelScope, SharingStarted.Eagerly, AuthState.Unknown)

    var isLoading by mutableStateOf(false)
        private set

    var loginError by mutableStateOf<String?>(null)
        private set

    fun login(username: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            loginError = null

            try {
                loginUseCase(username, password)
                // if we reach here, AuthRepository will update authState to Authenticated
            } catch (e: InvalidCredentialsException) {
                loginError = "Nama pengguna atau kata laluan tidak sah."
            } catch (e: Exception) {
                loginError = "Ralat log masuk. Sila cuba lagi."
            } finally {
                isLoading = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun clearError() {
        loginError = null
    }
}
