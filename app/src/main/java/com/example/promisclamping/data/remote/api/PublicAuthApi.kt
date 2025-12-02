package com.example.promisclamping.data.remote.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

interface PublicAuthApi {
    @POST("public/authenticate")
    suspend fun getSessionToken(@Body body: PublicAuthRequest): PublicAuthResponse
}

data class PublicAuthRequest(
    @SerializedName("client_id") val clientId: String,
    @SerializedName("client_secret") val clientSecret: String
)

data class PublicAuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: Long
)
