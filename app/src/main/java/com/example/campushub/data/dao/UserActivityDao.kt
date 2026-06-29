package com.example.campushub.data.dao

import androidx.room.*
import com.example.campushub.data.entity.UserActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserActivityDao {
    @Query("SELECT * FROM user_activities ORDER BY created_at DESC")
    fun getAllUserActivities(): Flow<List<UserActivityEntity>>

    @Query("SELECT * FROM user_activities WHERE status = :status ORDER BY created_at DESC")
    fun getUserActivitiesByStatus(status: String): Flow<List<UserActivityEntity>>

    @Query("SELECT * FROM user_activities WHERE activity_id = :activityId AND status = :status")
    suspend fun getUserActivity(activityId: Int, status: String): UserActivityEntity?

    @Query("SELECT COUNT(*) FROM user_activities WHERE activity_id = :activityId AND status = :status")
    suspend fun countByActivityId(activityId: Int, status: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userActivity: UserActivityEntity): Long

    @Delete
    suspend fun delete(userActivity: UserActivityEntity)

    @Query("DELETE FROM user_activities WHERE activity_id = :activityId AND status = :status")
    suspend fun deleteByActivityId(activityId: Int, status: String)

    @Query("SELECT COUNT(*) FROM user_activities WHERE status = 'registered'")
    fun getRegistrationCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_activities WHERE status = 'favorite'")
    fun getFavoriteCount(): Flow<Int>
}
