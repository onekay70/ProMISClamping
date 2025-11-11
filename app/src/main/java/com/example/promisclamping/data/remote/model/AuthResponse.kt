package com.example.promisclamping.data.remote.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("securityToken") val securityToken: String,
    @SerializedName("idPengguna") val idPengguna: String? = null,
    @SerializedName("namaPengguna") val namaPengguna: String? = null,
    @SerializedName("emel") val emel: String? = null,
    @SerializedName("flagPasswordExpired") val flagPasswordExpired: Boolean? = null,
    @SerializedName("flagTukarKatalaluan") val flagTukarKatalaluan: Boolean? = null,
    @SerializedName("skipOtp") val skipOtp: Boolean? = null,
    @SerializedName("idBadanBerkanun") val idBadanBerkanun: String? = null,
    @SerializedName("idSeksyen") val idSeksyen: String? = null
    // "senaraiPeranan" ignored unless you need it
)