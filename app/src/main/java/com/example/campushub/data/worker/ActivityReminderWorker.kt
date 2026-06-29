package com.example.campushub.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.campushub.CampusHubApplication
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ActivityReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val container = (appContext as CampusHubApplication).container
    private val reminderTracker = ReminderTracker(appContext)

    override suspend fun doWork(): Result {
        try {
            val now = Calendar.getInstance().timeInMillis
            val upcomingActivities = container.activityRepository.getUpcomingActivities(now)
            val timeFormat = SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault())

            upcomingActivities.forEach { activity ->
                if (reminderTracker.wasNotified(activity.id)) return@forEach

                val timeStr = if (activity.startTime > 0) {
                    timeFormat.format(activity.startTime)
                } else {
                    "${activity.date} ${activity.time}"
                }

                container.notificationRepository.sendActivityReminderNotification(
                    activityTitle = activity.title,
                    activityId = activity.id,
                    time = timeStr
                )

                reminderTracker.markNotified(activity.id)
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
