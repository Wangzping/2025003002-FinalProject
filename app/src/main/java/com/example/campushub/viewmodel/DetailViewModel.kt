package com.example.campushub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campushub.data.repository.ActivityRepository
import com.example.campushub.data.repository.CheckInRepository
import com.example.campushub.data.repository.NotificationRepository
import com.example.campushub.model.CampusActivity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val activity: CampusActivity) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class DetailViewModel(
    private val activityId: Int,
    private val activityRepository: ActivityRepository,
    private val notificationRepository: NotificationRepository,
    private val checkInRepository: CheckInRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            val activity = activityRepository.getActivity(activityId)
            if (activity != null) _uiState.value = DetailUiState.Success(activity)
            else _uiState.value = DetailUiState.Error("活动不存在")
        }
    }

    fun toggleRegistration() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current is DetailUiState.Success) {
                if (current.activity.isRegistered) {
                    activityRepository.cancelRegistration(activityId)
                    notificationRepository.sendCancelSuccessNotification(current.activity.title)
                } else {
                    val success = activityRepository.register(activityId)
                    if (success) {
                        notificationRepository.sendRegistrationSuccessNotification(current.activity.title, activityId)
                    } else {
                        notificationRepository.sendRegistrationFailedNotification(current.activity.title, "活动已满或已报名")
                    }
                }
                load()
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current is DetailUiState.Success) {
                val isFavorite = activityRepository.toggleFavorite(activityId)
                if (isFavorite) {
                    notificationRepository.sendFavoriteAddedNotification(current.activity.title, activityId)
                }
            }
        }
    }

    fun updateActivity(
        title: String,
        description: String,
        date: String,
        time: String,
        location: String,
        category: String,
        maxParticipants: Int,
        creditHours: Int = 2
    ) {
        viewModelScope.launch {
            try {
                val changes = activityRepository.updateActivity(
                    id = activityId,
                    title = title,
                    description = description,
                    date = date,
                    time = time,
                    location = location,
                    category = category,
                    maxParticipants = maxParticipants,
                    creditHours = creditHours
                )
                if (changes != null) {
                    val current = _uiState.value
                    val activityTitle = if (current is DetailUiState.Success) current.activity.title else title
                    notificationRepository.sendActivityUpdatedNotification(
                        activityTitle = activityTitle,
                        activityId = activityId,
                        changes = changes
                    )
                }
                load()
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.message ?: "更新失败")
            }
        }
    }

    fun generateCheckInCode() {
        viewModelScope.launch {
            try {
                val code = checkInRepository.generateCode(activityId)
                _uiState.value = DetailUiState.Success(
                    (uiState.value as DetailUiState.Success).activity.copy()
                )
                // code 通过返回值返回，UI 通过协程结果获取
            } catch (e: Exception) {
                // 忽略
            }
        }
    }

    suspend fun generateCheckInCodeSuspend(): String {
        return checkInRepository.generateCode(activityId)
    }

    suspend fun verifyAndCheckIn(code: String): CheckInResult {
        val current = _uiState.value
        if (current !is DetailUiState.Success) return CheckInResult.Error("页面状态异常")
        val activity = current.activity

        val valid = checkInRepository.verifyCode(activityId, code)
        if (!valid) return CheckInResult.Error("签到码无效或已过期")

        val success = checkInRepository.checkIn(activityId, activity.creditHours)
        if (!success) return CheckInResult.Error("您已签到过了")

        load()
        return CheckInResult.Success(activity.creditHours)
    }
}

sealed interface CheckInResult {
    data class Success(val creditHours: Int) : CheckInResult
    data class Error(val message: String) : CheckInResult
}

class DetailViewModelFactory(
    private val activityId: Int,
    private val activityRepository: ActivityRepository,
    private val notificationRepository: NotificationRepository,
    private val checkInRepository: CheckInRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DetailViewModel(activityId, activityRepository, notificationRepository, checkInRepository) as T
    }
}
