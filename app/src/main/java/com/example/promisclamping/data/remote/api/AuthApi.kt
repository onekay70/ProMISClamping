package com.example.promisclamping.data.remote.api

import com.example.promisclamping.data.remote.model.AuthRequest
import com.example.promisclamping.data.remote.model.AuthResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("public/authenticate")
    suspend fun authenticate(@Body body: AuthRequest): AuthResponse
}
