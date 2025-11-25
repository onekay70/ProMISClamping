package com.example.promisclamping.presentation.auth

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.promisclamping.data.auth.LoginAuthRepositoryImpl
import com.example.promisclamping.data.local.TokenStore
import com.example.promisclamping.data.remote.api.AuthApi
import com.example.promisclamping.data.remote.api.UserAuthApi
import com.example.promisclamping.domain.auth.LoginUseCase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {

            // Shared Retrofit for both auth endpoints
            val retrofit = Retrofit.Builder()
                .baseUrl("https://gerbang.bph.gov.my/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val publicApi = retrofit.create(AuthApi::class.java)
            val userApi = retrofit.create(UserAuthApi::class.java)
            val tokenStore = TokenStore(application.applicationContext)

            val authRepo = LoginAuthRepositoryImpl(
                publicApi = publicApi,
                userApi = userApi,
                tokenStore = tokenStore
            )

            val loginUseCase = LoginUseCase(authRepo)

            return AuthViewModel(
                authRepository = authRepo,
                loginUseCase = loginUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
