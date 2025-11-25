package com.example.promisclamping.presentation.auth

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.promisclamping.network.ApiClient
import com.example.promisclamping.domain.auth.LoginUseCase
import com.example.promisclamping.data.auth.LegacyAuthRepositoryAdapter
import com.example.promisclamping.data.repository.AuthRepository as LegacyAuthRepository

class AuthViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {

            // 1️⃣ Use your existing ApiClient to build the "legacy" auth repo
            val legacyRepo: LegacyAuthRepository = ApiClient.makeAuthRepo(application)

            // 2️⃣ Wrap it in our adapter, which implements domain.auth.AuthRepository
            val authRepo = LegacyAuthRepositoryAdapter(legacyRepo)

            // 3️⃣ Inject that into the use case & ViewModel
            val loginUseCase = LoginUseCase(authRepo)

            return AuthViewModel(
                authRepository = authRepo,
                loginUseCase = loginUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
