package com.example.promisclamping

import android.app.Application
import com.example.promisclamping.Config.SEC_TOKEN_LOGIN
import com.example.promisclamping.Config.SEC_TOKEN_PASSWORD
import com.example.promisclamping.data.local.TokenStore
import com.example.promisclamping.data.remote.api.AuthApi
import com.example.promisclamping.data.repository.AuthRepository
import com.example.promisclamping.network.ApiClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {
    lateinit var authApi: AuthApi

    override fun onCreate() {
        super.onCreate()
        authApi = Retrofit.Builder()
            .baseUrl("https://gerbang.bph.gov.my/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}
