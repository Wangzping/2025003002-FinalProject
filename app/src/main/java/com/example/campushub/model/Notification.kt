package com.example.campushub.model

data class Notification(
    val id: Int = 0,
    val title: String,
    val content: String,
    val type: NotificationType = NotificationType.INFO,
    val activityId: Int? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class NotificationType(val value: String) {
    INFO("info"),
    SUCCESS("success"),
    WARNING("warning"),
    ERROR("error");

    companion object {
        fun fromValue(value: String): NotificationType {
            return entries.find { it.value == value } ?: INFO
        }
    }
}
