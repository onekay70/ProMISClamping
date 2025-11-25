package com.example.promisclamping.domain.auth

import kotlinx.coroutines.flow.Flow

sealed class AuthState {
    data object Unknown : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val token: AuthToken) : AuthState()
}

interface AuthRepository {
    val authState: Flow<AuthState>

    suspend fun login(credentials: LoginCredentials)
    suspend fun logout()
    suspend fun getValidTokenOrNull(): String?
}
