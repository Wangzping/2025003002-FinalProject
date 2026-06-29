package com.example.campushub.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_activities",
    indices = [Index(value = ["activity_id"], unique = true)]
)
data class UserActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "activity_id")
    val activityId: Int,
    val status: String = "registered",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
