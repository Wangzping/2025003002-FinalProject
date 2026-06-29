package com.example.campushub.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val FOLLOW_SYSTEM_KEY = booleanPreferencesKey("follow_system")
        private val SORT_BY_KEY = stringPreferencesKey("sort_by")
        private val FILTER_CATEGORY_KEY = stringPreferencesKey("filter_category")
    }

    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE_KEY] ?: false }
    val followSystem: Flow<Boolean> = context.dataStore.data.map { it[FOLLOW_SYSTEM_KEY] ?: true }
    val sortBy: Flow<String> = context.dataStore.data.map { it[SORT_BY_KEY] ?: "date" }
    val filterCategory: Flow<String> = context.dataStore.data.map { it[FILTER_CATEGORY_KEY] ?: "all" }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { it[DARK_MODE_KEY] = isDark }
    }

    suspend fun setFollowSystem(follow: Boolean) {
        context.dataStore.edit { it[FOLLOW_SYSTEM_KEY] = follow }
    }

    suspend fun setSortBy(sortBy: String) {
        context.dataStore.edit { it[SORT_BY_KEY] = sortBy }
    }

    suspend fun setFilterCategory(category: String) {
        context.dataStore.edit { it[FILTER_CATEGORY_KEY] = category }
    }
}
