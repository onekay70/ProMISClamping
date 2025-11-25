package com.example.promisclamping.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun login(username: String, password: String) {
        viewModelScope.launch {
            loginUseCase(username, password)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
