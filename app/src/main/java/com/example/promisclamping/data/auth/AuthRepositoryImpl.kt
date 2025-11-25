package com.example.promisclamping.data.auth

import com.example.promisclamping.data.auth.local.TokenStoreImpl
import com.example.promisclamping.data.auth.remote.AuthApi
import com.example.promisclamping.data.auth.remote.AuthRequest
import com.example.promisclamping.domain.auth.AuthRepository
import com.example.promisclamping.domain.auth.AuthState
import com.example.promisclamping.domain.auth.AuthToken
import com.example.promisclamping.domain.auth.LoginCredentials
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TOKEN_TTL_MILLIS = 24L * 60L * 60L * 1000L

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenStore: TokenStoreImpl,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AuthRepository {

    private val scope = CoroutineScope(ioDispatcher)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unknown)
    override val authState = _authState.asStateFlow()

    init {
        scope.launch {
            val now = System.currentTimeMillis()
            val stored = tokenStore.getToken()
            if (stored != null && now < stored.expiresAtMillis) {
                _authState.value = AuthState.Authenticated(stored)
            } else {
                tokenStore.clear()
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    override suspend fun login(credentials: LoginCredentials) = withContext(ioDispatcher) {
        val response = authApi.authenticate(
            AuthRequest(credentials.username, credentials.password)
        )

        val now = System.currentTimeMillis()
        val token = AuthToken(
            token = response.token,
            expiresAtMillis = now + TOKEN_TTL_MILLIS
        )

        tokenStore.saveToken(token)
        _authState.value = AuthState.Authenticated(token)
    }

    override suspend fun logout() = withContext(ioDispatcher) {
        tokenStore.clear()
        _authState.value = AuthState.Unauthenticated
    }

    override suspend fun getValidTokenOrNull(): String? = withContext(ioDispatcher) {
        val stored = tokenStore.getToken() ?: return@withContext null
        val now = System.currentTimeMillis()
        if (now < stored.expiresAtMillis) stored.token else {
            tokenStore.clear()
            null
        }
    }
}
