package com.example.promisclamping.data.remote.interceptor

// data/remote/interceptor/AuthInterceptor.kt
import com.example.promisclamping.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val repo: AuthRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Get (or fetch) token synchronously for this thread
        val bearer = runBlocking { repo.getBearer() }

        var req = chain.request().newBuilder()
            .header("Authorization", bearer)
            .build()

        var res = chain.proceed(req)

        if (res.code == 401) {
            // try refresh once
            res.close()
            val newBearer = runBlocking { repo.refresh() }
            req = chain.request().newBuilder()
                .header("Authorization", newBearer)
                .build()
            res = chain.proceed(req)
        }
        return res
    }
}
