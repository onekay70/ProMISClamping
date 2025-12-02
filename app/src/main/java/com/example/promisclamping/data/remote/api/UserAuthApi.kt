package com.example.promisclamping.data.remote.api

import com.example.promisclamping.data.remote.model.UserLoginRequest
import com.example.promisclamping.data.remote.model.UserLoginResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface UserAuthApi {

    @POST("mobile/clamping/authenticate")
    suspend fun login(
        @Header("Authorization") bearer: String,
        @Body body: UserLoginRequest
    ): UserLoginResponse
}
