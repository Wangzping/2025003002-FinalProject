package com.example.campushub.data.dao

import androidx.room.*
import com.example.campushub.data.entity.CheckInCodeEntity
import com.example.campushub.data.entity.CheckInRecordEntity

@Dao
interface CheckInDao {

    //签到码
    @Insert
    suspend fun insertCode(code: CheckInCodeEntity)

    @Query("SELECT * FROM check_in_codes WHERE activityId = :activityId AND isActive = 1 AND expiresAt > :now ORDER BY id DESC LIMIT 1")
    suspend fun getActiveCode(activityId: Int, now: Long): CheckInCodeEntity?

    @Query("UPDATE check_in_codes SET isActive = 0 WHERE id = :id")
    suspend fun deactivateCode(id: Int)

    @Query("UPDATE check_in_codes SET isActive = 0 WHERE expiresAt <= :now")
    suspend fun deactivateExpiredCodes(now: Long)

    //签到记录
    @Insert
    suspend fun insertRecord(record: CheckInRecordEntity): Long

    @Query("SELECT * FROM check_in_records WHERE activityId = :activityId ORDER BY id DESC LIMIT 1")
    suspend fun getRecord(activityId: Int): CheckInRecordEntity?

    @Query("SELECT COUNT(*) FROM check_in_records WHERE activityId = :activityId")
    suspend fun hasCheckedIn(activityId: Int): Boolean

    @Query("SELECT SUM(duration) FROM check_in_records")
    suspend fun getTotalDuration(): Int

    @Query("SELECT COUNT(*) FROM check_in_records")
    suspend fun getTotalCheckIns(): Int

    @Query("SELECT cr.activityId FROM check_in_records cr WHERE cr.activityId = :activityId LIMIT 1")
    suspend fun getCheckedInActivityId(activityId: Int): Int?
}
