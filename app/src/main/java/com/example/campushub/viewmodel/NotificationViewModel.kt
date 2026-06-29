package com.example.campushub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campushub.data.repository.NotificationRepository
import com.example.campushub.model.Notification
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface NotificationUiState {
    data object Loading : NotificationUiState
    data class Success(
        val notifications: List<Notification>,
        val isEmpty: Boolean = true
    ) : NotificationUiState
}

class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    val unreadCount: StateFlow<Int> = notificationRepository.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            notificationRepository.getAllNotifications().collect { notifications ->
                _uiState.value = NotificationUiState.Success(
                    notifications = notifications,
                    isEmpty = notifications.isEmpty()
                )
            }
        }
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch { notificationRepository.markAsRead(notificationId) }
    }

    fun markAllAsRead() {
        viewModelScope.launch { notificationRepository.markAllAsRead() }
    }

    fun deleteNotification(notificationId: Int) {
        viewModelScope.launch { notificationRepository.deleteNotification(notificationId) }
    }

    fun clearAll() {
        viewModelScope.launch { notificationRepository.deleteAll() }
    }
}

class NotificationViewModelFactory(
    private val notificationRepository: NotificationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return NotificationViewModel(notificationRepository) as T
    }
}
