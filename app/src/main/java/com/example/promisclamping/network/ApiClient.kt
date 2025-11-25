package com.example.promisclamping.network

import UploadService
import android.content.Context
import com.example.promisclamping.Config
import com.example.promisclamping.Config.SEC_TOKEN_LOGIN
import com.example.promisclamping.Config.SEC_TOKEN_PASSWORD
import com.example.promisclamping.data.local.TokenStore
import com.example.promisclamping.data.remote.api.AuthApi
import com.example.promisclamping.data.remote.interceptor.AuthInterceptor
import com.example.promisclamping.data.repository.AuthRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // create Retrofit instance for the authentication endpoint
    private fun authRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://gerbang.bph.gov.my/api/")   // must end with /
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // lazy single instance of the AuthApi interface
    private val authApi: AuthApi by lazy {
        authRetrofit().create(AuthApi::class.java)
    }

    // Somewhere central (e.g., in an object Network or inside your Application class)
    fun makeAuthRepo(context: Context): AuthRepository {
        val store = TokenStore(context.applicationContext)
        return AuthRepository(
            api = authApi,
            store = store,
            clientId = SEC_TOKEN_LOGIN,      // put these in BuildConfig, not hard-coded
            clientSecret = SEC_TOKEN_PASSWORD
        )
    }

    private fun httpClient(context: Context): OkHttpClient {
        val repo = makeAuthRepo(context)
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(repo)) // ⬅️ our upgraded auth
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    // Use the same client for all services that require the token
    fun kompaun(context: Context): ApiService =
        Retrofit.Builder()
            .baseUrl(Config.KOMPAUN_BASE_URL)
            .client(httpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

    fun upload(context: Context): UploadService =
        Retrofit.Builder()
            .baseUrl(Config.UPLOAD_BASE_URL)
            .client(httpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UploadService::class.java)
}
