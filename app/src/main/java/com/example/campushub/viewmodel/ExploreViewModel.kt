package com.example.campushub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.campushub.data.repository.ActivityRepository
import com.example.campushub.datastore.UserPreferencesRepository
import com.example.campushub.model.CampusActivity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ExploreUiState {
    data object Loading : ExploreUiState
    data class Success(val activities: List<CampusActivity>, val isEmpty: Boolean = false) : ExploreUiState
}

class ExploreViewModel(
    private val activityRepository: ActivityRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    val categories = listOf("all", "文艺活动", "学术竞赛", "体育赛事", "学术讲座", "公益活动")

    init { loadActivities() }

    fun loadActivities() {
        viewModelScope.launch {
            val filter = userPreferencesRepository.filterCategory.first()
            val flow = if (filter == "all") activityRepository.getAllActivities()
            else activityRepository.getActivitiesByCategory(filter)
            flow.collect { list ->
                _uiState.value = ExploreUiState.Success(list, list.isEmpty())
            }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            _isSearching.value = true
            if (query.isBlank()) { loadActivities(); _isSearching.value = false; return@launch }
            activityRepository.searchActivities(query).collect { list ->
                _uiState.value = ExploreUiState.Success(list, list.isEmpty())
            }
            _isSearching.value = false
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        loadActivities()
    }

    fun setFilter(category: String) {
        viewModelScope.launch {
            userPreferencesRepository.setFilterCategory(category)
        }
        loadActivities()
    }
}

class ExploreViewModelFactory(
    private val activityRepository: ActivityRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ExploreViewModel(activityRepository, userPreferencesRepository) as T
    }
}
