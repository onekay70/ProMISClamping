package com.example.promisclamping.data.repository

// data/repository/AuthRepository.kt
import com.example.promisclamping.data.local.TokenStore
import com.example.promisclamping.data.remote.api.AuthApi
import com.example.promisclamping.data.remote.model.AuthRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val api: AuthApi,
    private val store: TokenStore,
    private val clientId: String,
    private val clientSecret: String
) {
    private val skewSec = 60 // refresh 1 minute early

    suspend fun getBearer(): String {
        val now = System.currentTimeMillis() / 1000
        val existing = store.accessToken
        if (existing != null && now + skewSec < store.expiryEpochSec) {
            return "Bearer $existing"
        }
        return refresh()
    }

    suspend fun refresh(): String = withContext(Dispatchers.IO) {
        val res = api.authenticate(AuthRequest(clientId, clientSecret))
        val now = System.currentTimeMillis() / 1000
        store.accessToken = res.accessToken
        store.expiryEpochSec = now + res.expiresIn
        return@withContext "${res.tokenType} ${res.accessToken}"
    }

    fun clear() = store.clear()
}
