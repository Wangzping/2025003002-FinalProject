package com.example.campushub.data.network

import com.example.campushub.data.entity.ActivityEntity
import com.example.campushub.data.network.dto.ActivityDto

class NetworkDataSource(private val apiService: ApiService) {

    suspend fun fetchActivities(category: String? = null, query: String? = null): Result<List<ActivityEntity>> {
        return try {
            val response = apiService.getActivities(category, query)
            if (response.code == 200) {
                Result.success(response.data.map { it.toEntity() })
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchActivityDetail(id: Int): Result<ActivityEntity> {
        return try {
            val response = apiService.getActivityDetail(id)
            if (response.code == 200) {
                Result.success(response.data.toEntity())
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ActivityDto.toEntity() = ActivityEntity(
        id = id, title = title, description = description, date = date, time = time,
        location = location, organizer = organizer, category = category,
        maxParticipants = maxParticipants, currentParticipants = currentParticipants,
        imageUrl = imageUrl, createdAt = createdAt
    )
}
