package com.example.campushub.data.repository

import com.example.campushub.data.dao.NotificationDao
import com.example.campushub.data.entity.NotificationEntity
import com.example.campushub.model.Notification
import com.example.campushub.model.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepository(private val notificationDao: NotificationDao) {

    fun getAllNotifications(): Flow<List<Notification>> {
        return notificationDao.getAllNotifications().map { entities ->
            entities.map { it.toNotification() }
        }
    }

    fun getUnreadNotifications(): Flow<List<Notification>> {
        return notificationDao.getUnreadNotifications().map { entities ->
            entities.map { it.toNotification() }
        }
    }

    fun getUnreadCount(): Flow<Int> {
        return notificationDao.getUnreadCount()
    }

    suspend fun getNotification(id: Int): Notification? {
        return notificationDao.getNotificationById(id)?.toNotification()
    }

    suspend fun sendNotification(
        title: String,
        content: String,
        type: NotificationType = NotificationType.INFO,
        activityId: Int? = null
    ): Long {
        val entity = NotificationEntity(
            title = title,
            content = content,
            type = type.value,
            activityId = activityId
        )
        return notificationDao.insert(entity)
    }

    suspend fun sendRegistrationSuccessNotification(activityTitle: String, activityId: Int) {
        sendNotification(
            title = "报名成功",
            content = "您已成功报名「$activityTitle」，记得准时参加哦！",
            type = NotificationType.SUCCESS,
            activityId = activityId
        )
    }

    suspend fun sendRegistrationFailedNotification(activityTitle: String, reason: String) {
        sendNotification(
            title = "报名失败",
            content = "很抱歉，「$activityTitle」报名失败：$reason",
            type = NotificationType.ERROR
        )
    }

    suspend fun sendCancelSuccessNotification(activityTitle: String) {
        sendNotification(
            title = "取消报名成功",
            content = "您已取消「$activityTitle」的报名",
            type = NotificationType.INFO
        )
    }

    suspend fun sendFavoriteAddedNotification(activityTitle: String, activityId: Int) {
        sendNotification(
            title = "收藏成功",
            content = "您已收藏「$activityTitle」",
            type = NotificationType.SUCCESS,
            activityId = activityId
        )
    }

    suspend fun sendActivityCreatedNotification(activityTitle: String, activityId: Int) {
        sendNotification(
            title = "发布成功",
            content = "您的活动「$activityTitle」已发布！",
            type = NotificationType.SUCCESS,
            activityId = activityId
        )
    }

    suspend fun sendActivityReminderNotification(activityTitle: String, activityId: Int, time: String) {
        sendNotification(
            title = "活动即将开始",
            content = "「$activityTitle」将于 $time 开始，请准时参加！",
            type = NotificationType.WARNING,
            activityId = activityId
        )
    }

    suspend fun sendActivityUpdatedNotification(activityTitle: String, activityId: Int, changes: String) {
        sendNotification(
            title = "活动已更新",
            content = "「$activityTitle」信息已更新：$changes",
            type = NotificationType.INFO,
            activityId = activityId
        )
    }

    suspend fun markAsRead(id: Int) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun deleteNotification(id: Int) {
        notificationDao.deleteById(id)
    }

    suspend fun deleteAll() {
        notificationDao.deleteAll()
    }

    private fun NotificationEntity.toNotification(): Notification = Notification(
        id = this.id,
        title = this.title,
        content = this.content,
        type = NotificationType.fromValue(this.type),
        activityId = this.activityId,
        isRead = this.isRead,
        createdAt = this.createdAt
    )
}
