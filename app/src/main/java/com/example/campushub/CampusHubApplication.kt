package com.example.campushub

import android.app.Application
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.campushub.data.AppContainer
import com.example.campushub.data.worker.ActivityReminderWorker
import com.example.campushub.data.worker.ReminderTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class CampusHubApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        applicationScope.launch {
            container.activityRepository.loadMockDataIfEmpty()
            checkUpcomingActivities()
        }
        setupWorkManager()
    }

    private suspend fun checkUpcomingActivities() {
        val reminderTracker = ReminderTracker(this)
        val now = System.currentTimeMillis()
        val upcoming = container.activityRepository.getUpcomingActivities(now)
        val timeFormat = SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault())

        upcoming.forEach { activity ->
            if (reminderTracker.wasNotified(activity.id)) return@forEach

            val timeStr = if (activity.startTime > 0) {
                timeFormat.format(activity.startTime)
            } else {
                "${activity.date} ${activity.time.split("-")[0]}"
            }

            container.notificationRepository.sendActivityReminderNotification(
                activityTitle = activity.title,
                activityId = activity.id,
                time = timeStr
            )

            reminderTracker.markNotified(activity.id)
        }
    }

    private fun setupWorkManager() {
        val constraints = Constraints.Builder()
            .build()

        val reminderRequest = PeriodicWorkRequestBuilder<ActivityReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueue(reminderRequest)
    }
}
