package com.example.campushub.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.campushub.data.dao.ActivityDao
import com.example.campushub.data.dao.CheckInDao
import com.example.campushub.data.dao.NotificationDao
import com.example.campushub.data.dao.UserActivityDao
import com.example.campushub.data.entity.ActivityEntity
import com.example.campushub.data.entity.CheckInCodeEntity
import com.example.campushub.data.entity.CheckInRecordEntity
import com.example.campushub.data.entity.NotificationEntity
import com.example.campushub.data.entity.UserActivityEntity

@Database(
    entities = [ActivityEntity::class, UserActivityEntity::class, NotificationEntity::class, CheckInCodeEntity::class, CheckInRecordEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun userActivityDao(): UserActivityDao
    abstract fun notificationDao(): NotificationDao
    abstract fun checkInDao(): CheckInDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "campushub_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
