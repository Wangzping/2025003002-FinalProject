package com.example.campushub.data.network.dto

import com.google.gson.annotations.SerializedName

data class ActivityListResponse(
    val code: Int,
    val message: String,
    val data: List<ActivityDto>
)

data class ActivityDetailResponse(
    val code: Int,
    val message: String,
    val data: ActivityDto
)

data class ActivityDto(
    val id: Int,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val location: String,
    val organizer: String,
    val category: String,
    @SerializedName("max_participants")
    val maxParticipants: Int,
    @SerializedName("current_participants")
    val currentParticipants: Int = 0,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)
