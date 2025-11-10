package com.example.promisclamping.network

import UploadService
import android.content.Context
import com.example.promisclamping.Config
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // 🟢 Helper: reads token from Config or SharedPreferences
    private fun getAuthToken(context: Context?): String? {
        // for now just return Config.SECURITY_TOKEN
        return Config.SECURITY_TOKEN
        // OR, if dynamic:
        // val prefs = context?.getSharedPreferences("auth", Context.MODE_PRIVATE)
        // return prefs?.getString("token", null)
    }

    // 🧱 Interceptor that adds the Authorization header
    class AuthInterceptor(private val context: Context?) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val token = getAuthToken(context)
            val request = if (!token.isNullOrEmpty()) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else chain.request()
            return chain.proceed(request)
        }
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Add AuthInterceptor to every client
    private fun httpClient(context: Context?) = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(context))
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Two clients for two ports
    fun kompaun(context: Context?): ApiService =
        Retrofit.Builder()
            .baseUrl(Config.KOMPAUN_BASE_URL)
            .client(httpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

    // base URL must end with the resource path, e.g. ...:8093/upload/
    fun upload(context: Context?): UploadService =
        Retrofit.Builder()
            .baseUrl(Config.UPLOAD_BASE_URL)   // e.g. "http://192.168.0.25:8093/upload/"
            .client(httpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UploadService::class.java)
}
