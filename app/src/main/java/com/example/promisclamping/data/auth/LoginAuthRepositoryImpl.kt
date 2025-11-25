package com.example.promisclamping.data.auth

import com.example.promisclamping.Config
import com.example.promisclamping.data.local.TokenStore
import com.example.promisclamping.data.remote.api.AuthApi
import com.example.promisclamping.data.remote.api.UserAuthApi
import com.example.promisclamping.data.remote.model.AuthRequest
import com.example.promisclamping.data.remote.model.UserLoginRequest
import com.example.promisclamping.data.remote.model.UserLoginResponse
import com.example.promisclamping.domain.auth.AuthRepository
import com.example.promisclamping.domain.auth.AuthState
import com.example.promisclamping.domain.auth.AuthToken
import com.example.promisclamping.domain.auth.LoginCredentials
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

private const val TOKEN_TTL_SECONDS = 24L * 60L * 60L  // 24h

class LoginAuthRepositoryImpl(
    private val publicApi: AuthApi,
    private val userApi: UserAuthApi,
    private val tokenStore: TokenStore,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AuthRepository {

    private val scope = CoroutineScope(ioDispatcher)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unknown)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        scope.launch {
            val token = tokenStore.accessToken
            val expirySec = tokenStore.expiryEpochSec
            val nowSec = System.currentTimeMillis() / 1000

            if (!token.isNullOrBlank() && expirySec > nowSec) {
                _authState.value = AuthState.Authenticated(
                    AuthToken(
                        token = "Bearer $token",
                        expiresAtMillis = expirySec * 1000
                    )
                )
            } else {
                tokenStore.clear()
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    override suspend fun login(credentials: LoginCredentials) = withContext(ioDispatcher) {
        // STEP 1: public auth to get temp/session token
        val publicResp = publicApi.authenticate(
            AuthRequest(
                clientId = Config.SEC_TOKEN_LOGIN,
                clientSecret = Config.SEC_TOKEN_PASSWORD
            )
        )

        // Here we assume the field is called "securityToken" in AuthResponse.
        // If your real API uses "access_token", change AuthResponse accordingly.
        val tempToken = publicResp.securityToken

        // STEP 2: user login using that temp token
        val userResp: UserLoginResponse = try {
            userApi.login(
                bearer = "Bearer $tempToken",
                body = UserLoginRequest(
                    login = credentials.username,
                    password = credentials.password
                )
            )
        } catch (e: HttpException) {
            if (e.code() in 400..499) {
                // wrong username/password or user not found
                throw InvalidCredentialsException()
            } else {
                // server error etc. -> let caller handle
                throw e
            }
        }

        val nowSec = System.currentTimeMillis() / 1000

        // Save REAL securityToken from user login
        tokenStore.accessToken = userResp.securityToken
        tokenStore.expiryEpochSec = nowSec + TOKEN_TTL_SECONDS
        tokenStore.userName = userResp.namaPengguna ?: credentials.username
        tokenStore.userId = userResp.idPengguna

        val authToken = AuthToken(
            token = "Bearer ${userResp.securityToken}",
            expiresAtMillis = (nowSec + TOKEN_TTL_SECONDS) * 1000
        )

        _authState.value = AuthState.Authenticated(authToken)
    }

    override suspend fun logout() = withContext(ioDispatcher) {
        tokenStore.clear()
        _authState.value = AuthState.Unauthenticated
    }

    override suspend fun getValidTokenOrNull(): String? = withContext(ioDispatcher) {
        val token = tokenStore.accessToken
        val expiry = tokenStore.expiryEpochSec
        val nowSec = System.currentTimeMillis() / 1000
        return@withContext if (!token.isNullOrBlank() && expiry > nowSec) {
            "Bearer $token"
        } else {
            tokenStore.clear()
            null
        }
    }
}

class InvalidCredentialsException : Exception()
