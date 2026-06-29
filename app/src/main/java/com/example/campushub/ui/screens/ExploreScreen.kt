package com.example.campushub.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushub.CampusHubApplication
import com.example.campushub.ui.components.*
import com.example.campushub.viewmodel.ExploreUiState
import com.example.campushub.viewmodel.ExploreViewModel
import com.example.campushub.viewmodel.ExploreViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onActivityClick: (Int) -> Unit,
    onNotificationClick: () -> Unit = {}
) {
    val app = LocalContext.current.applicationContext as CampusHubApplication
    val container = app.container
    val vm: ExploreViewModel = viewModel(factory = ExploreViewModelFactory(container.activityRepository, container.userPreferencesRepository))
    val notificationVm: com.example.campushub.viewmodel.NotificationViewModel = viewModel(
        factory = com.example.campushub.viewmodel.NotificationViewModelFactory(container.notificationRepository)
    )

    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val categories = vm.categories
    val unreadCount by notificationVm.unreadCount.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发现活动") },
                actions = {
                    IconButton(onClick = onNotificationClick) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge { Text(unreadCount.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "通知",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { vm.search(it) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索活动...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { vm.clearSearch() }) { Icon(Icons.Default.Clear, contentDescription = "清除") }
                    }
                },
                singleLine = true, shape = MaterialTheme.shapes.medium
            )

            // Category filter chips
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val selected = cat == (if (searchQuery.isBlank()) "all" else "all") // simplified
                    FilterChip(
                        selected = cat == "all" && searchQuery.isBlank(),
                        onClick = { vm.setFilter(cat) },
                        label = { Text(if (cat == "all") "全部" else cat) }
                    )
                }
            }

            when (val state = uiState) {
                is ExploreUiState.Loading -> LoadingState()
                is ExploreUiState.Success -> {
                    if (state.isEmpty) {
                        EmptyState(message = if (searchQuery.isBlank()) "暂无活动" else "没有找到匹配的活动")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.activities, key = { it.id }) { activity ->
                                ActivityCard(activity = activity, onClick = { onActivityClick(activity.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}
