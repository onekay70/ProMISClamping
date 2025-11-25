package com.example.promisclamping.data.remote.model

import com.google.gson.annotations.SerializedName

data class UserLoginRequest(
    @SerializedName("login") val login: String,
    @SerializedName("password") val password: String
)
