package com.example.campushub.data.worker

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderTracker(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("reminder_tracker", Context.MODE_PRIVATE)

    fun wasNotified(activityId: Int): Boolean {
        val key = getTodayKey(activityId)
        return prefs.getStringSet(KEY_NOTIFIED, emptySet())?.contains(key) ?: false
    }

    fun markNotified(activityId: Int) {
        val key = getTodayKey(activityId)
        val set = prefs.getStringSet(KEY_NOTIFIED, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.add(key)
        prefs.edit().putStringSet(KEY_NOTIFIED, set).apply()
    }

    private fun getTodayKey(activityId: Int): String {
        val dateKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        return "$activityId:$dateKey"
    }

    companion object {
        private const val KEY_NOTIFIED = "notified_set"
    }
}
