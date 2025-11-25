package com.example.promisclamping.domain.auth

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String) {
        val creds = LoginCredentials(username, password)
        authRepository.login(creds)
    }
}
