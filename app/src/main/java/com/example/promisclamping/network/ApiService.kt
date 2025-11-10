package com.example.promisclamping.network

import com.example.promisclamping.models.ClampingRequestForm
import com.example.promisclamping.models.ClampingResponseForm
import retrofit2.http.Body
import retrofit2.http.POST

data class SaveKompaunRequest(
    val noKenderaan: String,
    val jenisKenderaan: String,
    val blok: String,
    val tempatKompaun: String,
    val gambarId: String?
)

data class SaveKompaunResponse(
    val success: Boolean,
    val id: String?,
    val message: String?
)

interface ApiService {
    @POST("clamping")
    suspend fun createClamping(
        @Body request: ClampingRequestForm
    ): retrofit2.Response<ClampingResponseForm>
}
