package com.example.promisclamping.data.auth

import com.example.promisclamping.data.repository.AuthRepository as LegacyAuthRepository
import com.example.promisclamping.domain.auth.AuthRepository
import com.example.promisclamping.domain.auth.AuthState
import com.example.promisclamping.domain.auth.AuthToken
import com.example.promisclamping.domain.auth.LoginCredentials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LegacyAuthRepositoryAdapter(
    private val legacy: LegacyAuthRepository
) : AuthRepository {

    // UI only needs to know: logged in / not logged in
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun login(credentials: LoginCredentials) {
        // For now we ignore username/password and use SEC_TOKEN_LOGIN / SEC_TOKEN_PASSWORD
        // that are already configured inside legacy AuthRepository via ApiClient.makeAuthRepo()

        val bearer = legacy.getBearer()   // calls your real /authenticate and caches token

        val now = System.currentTimeMillis()
        val token = AuthToken(
            token = bearer,                             // this is "Bearer <token>"
            expiresAtMillis = now + 24L * 60L * 60L * 1000L  // 24h client TTL
        )

        _authState.value = AuthState.Authenticated(token)
    }

    override suspend fun logout() {
        legacy.clear()
        _authState.value = AuthState.Unauthenticated
    }

    override suspend fun getValidTokenOrNull(): String? {
        return try {
            legacy.getBearer() // will reuse token or refresh as needed
        } catch (e: Exception) {
            null
        }
    }
}
