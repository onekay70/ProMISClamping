package com.example.promisclamping.data.remote.model

import com.google.gson.annotations.SerializedName

data class UserLoginResponse(
    @SerializedName("securityToken") val securityToken: String,
    @SerializedName("idPengguna") val idPengguna: String? = null,
    @SerializedName("namaPengguna") val namaPengguna: String? = null,
    @SerializedName("emel") val emel: String? = null
    // add more fields if needed
)
