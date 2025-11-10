package com.example.promisclamping.data.remote.model

data class AuthResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long // seconds
)