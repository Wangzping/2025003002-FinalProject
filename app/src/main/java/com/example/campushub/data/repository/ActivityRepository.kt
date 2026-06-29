package com.example.campushub.data.repository

import com.example.campushub.data.dao.ActivityDao
import com.example.campushub.data.dao.CheckInDao
import com.example.campushub.data.dao.UserActivityDao
import com.example.campushub.data.entity.ActivityEntity
import com.example.campushub.data.entity.UserActivityEntity
import com.example.campushub.data.network.MockDataSource
import com.example.campushub.data.network.NetworkDataSource
import com.example.campushub.model.CampusActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ActivityRepository(
    private val activityDao: ActivityDao,
    private val userActivityDao: UserActivityDao,
    private val networkDataSource: NetworkDataSource,
    private val checkInDao: CheckInDao? = null
) {
    suspend fun loadMockDataIfEmpty() {
        if (activityDao.getCount().first() == 0) {
            // 通过 Retrofit + MockInterceptor 从"网络"获取数据，再缓存到 Room
            val result = networkDataSource.fetchActivities()
            result.onSuccess { activities ->
                activities.forEach { activityDao.insert(it) }
            }.onFailure {
                // 网络请求失败，回退到本地 MockDataSource
                MockDataSource.getActivities().forEach { activityDao.insert(it) }
            }
        }
    }

    fun getAllActivities(): Flow<List<CampusActivity>> {
        return activityDao.getAllActivities().map { entities ->
            entities.map { it.toCampusActivity() }
        }
    }

    fun getActivitiesByCategory(category: String): Flow<List<CampusActivity>> {
        return if (category == "all") getAllActivities()
        else activityDao.getActivitiesByCategory(category).map { entities ->
            entities.map { it.toCampusActivity() }
        }
    }

    fun searchActivities(query: String): Flow<List<CampusActivity>> {
        return activityDao.searchActivities(query).map { entities ->
            entities.map { it.toCampusActivity() }
        }
    }

    suspend fun getActivity(id: Int): CampusActivity? {
        val entity = activityDao.getActivityById(id) ?: return null
        val isRegistered = userActivityDao.countByActivityId(id, "registered") > 0
        val isFavorite = userActivityDao.countByActivityId(id, "favorite") > 0
        val isCheckedIn = checkInDao?.hasCheckedIn(id) == true
        return entity.toCampusActivity(isRegistered, isFavorite, isCheckedIn)
    }

    suspend fun createActivity(
        title: String, description: String, date: String, time: String,
        location: String, organizer: String, category: String, maxParticipants: Int,
        creditHours: Int = 2
    ): Int {
        val newId = MockDataSource.getNextId()
        val startTime = parseDateTime(date, time)
        val entity = ActivityEntity(
            id = newId, title = title, description = description,
            date = date, time = time, location = location,
            organizer = organizer, category = category,
            maxParticipants = maxParticipants, startTime = startTime,
            creditHours = creditHours
        )
        activityDao.insert(entity)
        return newId
    }

    suspend fun updateActivity(
        id: Int,
        title: String,
        description: String,
        date: String,
        time: String,
        location: String,
        category: String,
        maxParticipants: Int,
        creditHours: Int = 2
    ): String? {
        val oldEntity = activityDao.getActivityById(id) ?: return null

        val changes = mutableListOf<String>()
        if (oldEntity.title != title) changes.add("标题")
        if (oldEntity.date != date) changes.add("日期")
        if (oldEntity.time != time) changes.add("时间")
        if (oldEntity.location != location) changes.add("地点")
        if (oldEntity.description != description) changes.add("描述")
        if (oldEntity.category != category) changes.add("分类")
        if (oldEntity.maxParticipants != maxParticipants) changes.add("最大参与人数")
        if (oldEntity.creditHours != creditHours) changes.add("学时")

        if (changes.isEmpty()) return null

        val newEntity = ActivityEntity(
            id = id,
            title = title,
            description = description,
            date = date,
            time = time,
            location = location,
            organizer = oldEntity.organizer,
            category = category,
            maxParticipants = maxParticipants,
            currentParticipants = oldEntity.currentParticipants,
            imageUrl = oldEntity.imageUrl,
            startTime = parseDateTime(date, time),
            creditHours = creditHours,
            createdAt = oldEntity.createdAt
        )
        activityDao.update(newEntity)
        return changes.joinToString("、") + "已变更"
    }

    private fun parseDateTime(date: String, time: String): Long {
        return try {
            val dateTimeStr = "$date $time"
            val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            format.parse(dateTimeStr)?.time ?: 0
        } catch (e: Exception) {
            0
        }
    }

    suspend fun deleteActivity(id: Int) {
        activityDao.deleteById(id)
        userActivityDao.deleteByActivityId(id, "registered")
        userActivityDao.deleteByActivityId(id, "favorite")
    }

    suspend fun register(activityId: Int): Boolean {
        val activity = activityDao.getActivityById(activityId) ?: return false
        if (activity.maxParticipants > 0 && activity.currentParticipants >= activity.maxParticipants) return false
        if (userActivityDao.countByActivityId(activityId, "registered") > 0) return false
        userActivityDao.insert(UserActivityEntity(activityId = activityId, status = "registered"))
        activityDao.incrementParticipants(activityId)
        return true
    }

    suspend fun cancelRegistration(activityId: Int) {
        if (userActivityDao.countByActivityId(activityId, "registered") > 0) {
            userActivityDao.deleteByActivityId(activityId, "registered")
            activityDao.decrementParticipants(activityId)
        }
    }

    suspend fun isRegistered(activityId: Int): Boolean {
        return userActivityDao.countByActivityId(activityId, "registered") > 0
    }

    suspend fun toggleFavorite(activityId: Int): Boolean {
        return if (userActivityDao.countByActivityId(activityId, "favorite") > 0) {
            userActivityDao.deleteByActivityId(activityId, "favorite")
            false
        } else {
            userActivityDao.insert(UserActivityEntity(activityId = activityId, status = "favorite"))
            true
        }
    }

    suspend fun isFavorite(activityId: Int): Boolean {
        return userActivityDao.countByActivityId(activityId, "favorite") > 0
    }

    fun getRegisteredActivities(): Flow<List<CampusActivity>> {
        return userActivityDao.getUserActivitiesByStatus("registered").map { userActivities ->
            val activityIds = userActivities.map { it.activityId }
            val entities = activityDao.getAllActivities().first()
            entities.filter { it.id in activityIds }.map { it.toCampusActivity(isRegistered = true) }
        }
    }

    fun getFavoriteActivities(): Flow<List<CampusActivity>> {
        return userActivityDao.getUserActivitiesByStatus("favorite").map { userActivities ->
            val activityIds = userActivities.map { it.activityId }
            val entities = activityDao.getAllActivities().first()
            entities.filter { it.id in activityIds }.map { it.toCampusActivity(isFavorite = true) }
        }
    }

    private fun ActivityEntity.toCampusActivity(
        isRegistered: Boolean = false,
        isFavorite: Boolean = false,
        isCheckedIn: Boolean = false
    ): CampusActivity = CampusActivity(
        id = this.id, title = this.title, description = this.description,
        date = this.date, time = this.time, location = this.location,
        organizer = this.organizer, category = this.category,
        maxParticipants = this.maxParticipants, currentParticipants = this.currentParticipants,
        imageUrl = this.imageUrl, startTime = this.startTime,
        isRegistered = isRegistered, isFavorite = isFavorite, isCheckedIn = isCheckedIn,
        creditHours = this.creditHours,
        createdAt = this.createdAt
    )

    suspend fun getUpcomingActivities(now: Long): List<CampusActivity> {
        val registeredIds = userActivityDao.getUserActivitiesByStatus("registered").first()
            .map { it.activityId }.toSet()

        val upcoming = activityDao.getActivitiesStartingInRange(
            now, now + 24 * 60 * 60 * 1000
        )

        return upcoming.map { entity ->
            entity.toCampusActivity(isRegistered = entity.id in registeredIds)
        }
    }
}
