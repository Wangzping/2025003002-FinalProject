package com.example.campushub.data.network

import com.example.campushub.data.network.dto.ActivityDetailResponse
import com.example.campushub.data.network.dto.ActivityListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/activities")
    suspend fun getActivities(
        @Query("category") category: String? = null,
        @Query("query") query: String? = null
    ): ActivityListResponse

    @GET("api/activities/{id}")
    suspend fun getActivityDetail(
        @Path("id") id: Int
    ): ActivityDetailResponse
}
