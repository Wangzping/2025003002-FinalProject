package com.example.campushub.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val location: String,
    val organizer: String,
    val category: String,
    @ColumnInfo(name = "max_participants")
    val maxParticipants: Int,
    @ColumnInfo(name = "current_participants")
    val currentParticipants: Int = 0,
    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null,
    @ColumnInfo(name = "start_time")
    val startTime: Long = 0,
    @ColumnInfo(name = "credit_hours")
    val creditHours: Int = 2,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
