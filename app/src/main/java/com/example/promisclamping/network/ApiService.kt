package com.example.promisclamping.network

import com.example.promisclamping.models.ClampingRequestForm
import com.example.promisclamping.models.ClampingResponseForm
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("clamping")
    suspend fun createClamping(
        @Body request: ClampingRequestForm
    ): retrofit2.Response<ClampingResponseForm>
}
