package com.example.promisclamping.data.remote.model

import com.google.gson.annotations.SerializedName

data class AuthRequest(
    @SerializedName("login") val clientId: String,
    @SerializedName("password") val clientSecret: String
)
