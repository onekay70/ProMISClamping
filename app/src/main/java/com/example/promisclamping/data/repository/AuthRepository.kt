package com.example.promisclamping.data.repository

// data/repository/AuthRepository.kt
// AuthRepository.kt (only the logic inside changed spots)
import com.example.promisclamping.data.local.TokenStore
import com.example.promisclamping.data.remote.api.AuthApi
import com.example.promisclamping.data.remote.model.AuthRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AuthRepository(
    private val api: AuthApi,
    private val store: TokenStore,
    private val clientId: String,
    private val clientSecret: String
) {
    private val skewSec = 60                          // refresh 1 min early

    //    private val defaultTtlSec = TimeUnit.HOURS.toSeconds(24) // <— your requested 24h TTL
    private val defaultTtlSec = TimeUnit.HOURS.toSeconds(12) // e.g., 12h

    suspend fun getBearer(): String {
        val now = System.currentTimeMillis() / 1000
        val token = store.accessToken
        val exp = store.expiryEpochSec
        return if (token != null && now + skewSec < exp) {
            "Bearer $token"
        } else {
            refresh()
        }
    }

    suspend fun refresh(): String = withContext(Dispatchers.IO) {
        try {
            val res = api.authenticate(AuthRequest(clientId, clientSecret))
            val now = System.currentTimeMillis() / 1000
            store.accessToken = res.securityToken
            store.expiryEpochSec = now + defaultTtlSec   // <— set your own 24h expiry
            return@withContext "Bearer ${res.securityToken}"
        } catch (e: Exception) {
            // if auth fails, drop any stale token so the next attempt starts clean
            store.clear()
            throw e
        }
    }

    fun clear() = store.clear()
}

