package com.example.campushub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campushub.data.repository.CheckInRepository
import com.example.campushub.datastore.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val checkInRepository: CheckInRepository
) : ViewModel() {

    val darkMode: StateFlow<Boolean> = userPreferencesRepository.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val followSystem: StateFlow<Boolean> = userPreferencesRepository.followSystem
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val sortBy: StateFlow<String> = userPreferencesRepository.sortBy
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "date")

    private val _totalCheckIns = MutableStateFlow(0)
    val totalCheckIns: StateFlow<Int> = _totalCheckIns.asStateFlow()

    private val _totalCreditHours = MutableStateFlow(0)
    val totalCreditHours: StateFlow<Int> = _totalCreditHours.asStateFlow()

    init { loadStats() }

    private fun loadStats() {
        viewModelScope.launch {
            _totalCheckIns.value = checkInRepository.getTotalCheckIns()
            _totalCreditHours.value = checkInRepository.getTotalCreditHours()
        }
    }

    fun refreshStats() { loadStats() }

    fun setDarkMode(isDark: Boolean) { viewModelScope.launch { userPreferencesRepository.setDarkMode(isDark) } }
    fun setFollowSystem(follow: Boolean) { viewModelScope.launch { userPreferencesRepository.setFollowSystem(follow) } }
    fun setSortBy(sortBy: String) { viewModelScope.launch { userPreferencesRepository.setSortBy(sortBy) } }
}

class SettingsViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val checkInRepository: CheckInRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SettingsViewModel(userPreferencesRepository, checkInRepository) as T
    }
}
