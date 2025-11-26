package com.example.promisclamping.network

import com.example.promisclamping.models.ClampingRequestForm
import com.example.promisclamping.models.ClampingResponseForm
import com.example.promisclamping.models.KompaunListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("clamping")
    suspend fun createClamping(
        @Body request: ClampingRequestForm,
        @Query("authId") authId: String? = null
    ): retrofit2.Response<ClampingResponseForm>

    @GET("clamping")
    suspend fun getKompaunList(
        @Query("status") status: String,
        @Query("pageNo") pageNo: Int,
        @Query("pageSize") pageSize: Int,
        @Query("authId") authId: String
    ): Response<KompaunListResponse>

    @PUT("clamping/{id}/batal-kompaun")
    suspend fun batalKompaun(
        @Path("id") id: String,
        @Body body: ClampingRequestForm,
        @Query("authId") authId: String
    ): Response<ClampingResponseForm>

    @PUT("clamping/{id}/selesai")
    suspend fun selesaiKompaun(
        @Path("id") id: String,
        @Body body: ClampingRequestForm,
        @Query("authId") authId: String
    ): Response<ClampingResponseForm>

}
