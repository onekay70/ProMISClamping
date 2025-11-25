package com.example.promisclamping.network

import com.example.promisclamping.domain.auth.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val authRepository: AuthRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath

        // Don't attach token for /authenticate
        if (path.endsWith("/authenticate")) {
            return chain.proceed(original)
        }

        val token = runBlocking { authRepository.getValidTokenOrNull() }

        val newReq = if (token != null) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        return chain.proceed(newReq)
    }
}
