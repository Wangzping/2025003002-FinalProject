package com.example.campushub.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "check_in_records",
    indices = [Index(value = ["activityId"])]
)
data class CheckInRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val activityId: Int,
    val checkInTime: Long = System.currentTimeMillis(),
    val duration: Int = 0
)
