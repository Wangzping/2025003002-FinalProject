package com.example.campushub.model

data class CampusActivity(
    val id: Int,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val location: String,
    val organizer: String,
    val category: String,
    val maxParticipants: Int,
    val currentParticipants: Int = 0,
    val imageUrl: String? = null,
    val startTime: Long = 0,
    val isRegistered: Boolean = false,
    val isFavorite: Boolean = false,
    val isCheckedIn: Boolean = false,
    val creditHours: Int = 2,
    val createdAt: Long = System.currentTimeMillis()
)
