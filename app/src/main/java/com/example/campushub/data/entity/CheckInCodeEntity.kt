package com.example.campushub.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_in_codes")
data class CheckInCodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val activityId: Int,
    val code: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long,
    val isActive: Boolean = true
)
