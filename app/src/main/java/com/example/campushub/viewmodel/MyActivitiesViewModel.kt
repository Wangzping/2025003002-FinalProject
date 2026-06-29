package com.example.campushub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campushub.data.repository.ActivityRepository
import com.example.campushub.data.repository.NotificationRepository
import com.example.campushub.model.CampusActivity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface MyActivitiesUiState {
    data object Loading : MyActivitiesUiState
    data class Success(
        val registered: List<CampusActivity>,
        val favorites: List<CampusActivity>,
        val isEmpty: Boolean = true
    ) : MyActivitiesUiState
}

class MyActivitiesViewModel(
    private val activityRepository: ActivityRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyActivitiesUiState>(MyActivitiesUiState.Loading)
    val uiState: StateFlow<MyActivitiesUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = MyActivitiesUiState.Loading
            combine(
                activityRepository.getRegisteredActivities(),
                activityRepository.getFavoriteActivities()
            ) { registered, favorites ->
                MyActivitiesUiState.Success(
                    registered = registered,
                    favorites = favorites,
                    isEmpty = registered.isEmpty() && favorites.isEmpty()
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun selectTab(tab: Int) { _selectedTab.value = tab }

    fun cancelRegistration(activityId: Int) {
        viewModelScope.launch {
            val activity = activityRepository.getActivity(activityId)
            activityRepository.cancelRegistration(activityId)
            activity?.let { notificationRepository.sendCancelSuccessNotification(it.title) }
        }
    }
}

class MyActivitiesViewModelFactory(
    private val activityRepository: ActivityRepository,
    private val notificationRepository: NotificationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MyActivitiesViewModel(activityRepository, notificationRepository) as T
    }
}
