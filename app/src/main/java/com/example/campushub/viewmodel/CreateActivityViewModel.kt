package com.example.campushub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campushub.data.repository.ActivityRepository
import com.example.campushub.data.repository.NotificationRepository
import com.example.campushub.model.CampusActivity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface CreateUiState {
    data object Idle : CreateUiState
    data class Success(val activityId: Int) : CreateUiState
    data class Error(val message: String) : CreateUiState
}

class CreateActivityViewModel(
    private val activityRepository: ActivityRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateUiState>(CreateUiState.Idle)
    val uiState: StateFlow<CreateUiState> = _uiState.asStateFlow()

    fun createActivity(
        title: String, description: String, date: String, time: String,
        location: String, organizer: String, category: String, maxParticipants: String,
        creditHours: String = "2"
    ) {
        // Input validation
        if (title.isBlank() || description.isBlank() || date.isBlank() || location.isBlank()) {
            _uiState.value = CreateUiState.Error("请填写所有必填项")
            return
        }
        val max = maxParticipants.toIntOrNull() ?: 0
        if (max < 0) {
            _uiState.value = CreateUiState.Error("参与人数不能为负数")
            return
        }
        val credit = creditHours.toIntOrNull() ?: 2
        if (credit < 0) {
            _uiState.value = CreateUiState.Error("学时不能为负数")
            return
        }

        viewModelScope.launch {
            try {
                val id = activityRepository.createActivity(
                    title = title, description = description, date = date,
                    time = time, location = location, organizer = organizer,
                    category = category, maxParticipants = max, creditHours = credit
                )
                notificationRepository.sendActivityCreatedNotification(title, id)
                _uiState.value = CreateUiState.Success(id)
            } catch (e: Exception) {
                _uiState.value = CreateUiState.Error(e.message ?: "创建失败")
            }
        }
    }

    fun reset() { _uiState.value = CreateUiState.Idle }
}

class CreateActivityViewModelFactory(
    private val activityRepository: ActivityRepository,
    private val notificationRepository: NotificationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CreateActivityViewModel(activityRepository, notificationRepository) as T
    }
}
