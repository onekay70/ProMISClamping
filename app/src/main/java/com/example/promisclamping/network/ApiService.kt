package com.example.promisclamping.network

import com.example.promisclamping.models.ClampingRequestForm
import com.example.promisclamping.models.ClampingResponseForm
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("clamping")
    suspend fun createClamping(
        @Body request: ClampingRequestForm,
        @Query("authId") authId: String? = null
    ): retrofit2.Response<ClampingResponseForm>
}
