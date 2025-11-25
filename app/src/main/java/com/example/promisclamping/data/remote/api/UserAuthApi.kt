package com.example.promisclamping.data.remote.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface UserAuthApi {
    @POST("pentadbiran/v1/authenticate")
    suspend fun login(
        @Header("Authorization") bearer: String,
        @Body body: UserLoginRequest
    ): UserLoginResponse
}

data class UserLoginRequest(
    @SerializedName("login") val login: String,
    @SerializedName("password") val password: String
)

data class UserLoginResponse(
    @SerializedName("securityToken") val securityToken: String,
    @SerializedName("namaPengguna") val namaPengguna: String?,
    @SerializedName("idPengguna") val idPengguna: Int?,
    @SerializedName("emel") val emel: String?
)
