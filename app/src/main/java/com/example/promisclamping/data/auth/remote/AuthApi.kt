package com.example.promisclamping.data.auth.remote

import retrofit2.http.Body
import retrofit2.http.POST

data class AuthRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val token: String
)

interface AuthApi {
    @POST("mobile/clamping/authenticate")
    suspend fun authenticate(@Body body: AuthRequest): AuthResponse
}
