package com.example.campushub.data.dao

import androidx.room.*
import com.example.campushub.data.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY date ASC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE category = :category ORDER BY date ASC")
    fun getActivitiesByCategory(category: String): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY date ASC")
    fun searchActivities(query: String): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityById(id: Int): ActivityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activities: List<ActivityEntity>)

    @Update
    suspend fun update(activity: ActivityEntity)

    @Delete
    suspend fun delete(activity: ActivityEntity)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE activities SET current_participants = current_participants + 1 WHERE id = :id")
    suspend fun incrementParticipants(id: Int)

    @Query("UPDATE activities SET current_participants = current_participants - 1 WHERE id = :id")
    suspend fun decrementParticipants(id: Int)

    @Query("SELECT COUNT(*) FROM activities")
    fun getCount(): Flow<Int>

    @Query("SELECT * FROM activities WHERE start_time BETWEEN :start AND :end ORDER BY start_time ASC")
    suspend fun getActivitiesStartingInRange(start: Long, end: Long): List<ActivityEntity>
}
