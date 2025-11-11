package com.example.promisclamping.data.remote.interceptor

// AuthInterceptor.kt  (replace entire class)

import android.util.Log
import com.example.promisclamping.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val repo: AuthRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // 1) Try to attach token; if it fails, continue without it
        var bearer: String? = null
        try {
            bearer = runBlocking { repo.getBearer() }
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "getBearer() failed, proceeding without token", e)
        }

        var req = if (bearer != null) {
            chain.request().newBuilder().header("Authorization", bearer).build()
        } else chain.request()

        var res = chain.proceed(req)

        // 2) If 401, attempt one refresh; if refresh fails, return original 401
        if (res.code == 401) {
            res.close()
            try {
                val newBearer = runBlocking { repo.refresh() }
                req = chain.request().newBuilder().header("Authorization", newBearer).build()
                res = chain.proceed(req)
            } catch (e: Exception) {
                Log.e("AuthInterceptor", "refresh() failed after 401, returning 401", e)
                // fall through with the 401 from the first attempt (already closed), so re-proceed once without auth:
                res = chain.proceed(chain.request())
            }
        }
        return res
    }
}
