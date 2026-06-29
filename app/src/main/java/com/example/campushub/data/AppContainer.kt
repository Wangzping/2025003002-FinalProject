package com.example.campushub.data

import android.content.Context
import com.example.campushub.data.database.AppDatabase
import com.example.campushub.data.network.ApiService
import com.example.campushub.data.network.MockInterceptor
import com.example.campushub.data.network.NetworkDataSource
import com.example.campushub.data.repository.ActivityRepository
import com.example.campushub.data.repository.CheckInRepository
import com.example.campushub.data.repository.NotificationRepository
import com.example.campushub.datastore.UserPreferencesRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(private val context: Context) {

    companion object {
        private const val BASE_URL = "https://api.campushub.com/"
    }

    // OkHttp with mock interceptor
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(MockInterceptor())
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
    val networkDataSource = NetworkDataSource(apiService)

    // Room
    private val database = AppDatabase.getDatabase(context)
    private val activityDao = database.activityDao()
    private val userActivityDao = database.userActivityDao()
    private val checkInDao = database.checkInDao()

    val userPreferencesRepository = UserPreferencesRepository(context)
    val checkInRepository = CheckInRepository(checkInDao)
    val activityRepository = ActivityRepository(
        activityDao = activityDao,
        userActivityDao = userActivityDao,
        networkDataSource = networkDataSource,
        checkInDao = checkInDao
    )
    val notificationRepository = NotificationRepository(database.notificationDao())
}
