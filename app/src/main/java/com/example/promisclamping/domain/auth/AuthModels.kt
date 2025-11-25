package com.example.promisclamping.domain.auth

data class AuthToken(
    val token: String,
    val expiresAtMillis: Long
)

data class LoginCredentials(
    val username: String,
    val password: String
)
