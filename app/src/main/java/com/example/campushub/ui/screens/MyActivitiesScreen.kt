package com.example.campushub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushub.CampusHubApplication
import com.example.campushub.ui.components.*
import com.example.campushub.viewmodel.MyActivitiesUiState
import com.example.campushub.viewmodel.MyActivitiesViewModel
import com.example.campushub.viewmodel.MyActivitiesViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyActivitiesScreen(onActivityClick: (Int) -> Unit, onNavigateToExplore: () -> Unit = {}) {
    val app = LocalContext.current.applicationContext as CampusHubApplication
    val container = app.container
    val vm: MyActivitiesViewModel = viewModel(factory = MyActivitiesViewModelFactory(container.activityRepository, container.notificationRepository))

    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val selectedTab by vm.selectedTab.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的活动") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Tab row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { vm.selectTab(0) }, text = { Text("已报名") })
                Tab(selected = selectedTab == 1, onClick = { vm.selectTab(1) }, text = { Text("收藏") })
            }

            when (val state = uiState) {
                is MyActivitiesUiState.Loading -> LoadingState()
                is MyActivitiesUiState.Success -> {
                    val activities = if (selectedTab == 0) state.registered else state.favorites
                    if (activities.isEmpty()) {
                        EmptyState(
                            message = if (selectedTab == 0) "你还没有报名任何活动" else "暂无收藏活动",
                            actionText = "去浏览活动",
                            onAction = onNavigateToExplore
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(activities, key = { it.id }) { activity ->
                                Card(
                                    onClick = { onActivityClick(activity.id) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(activity.title, style = MaterialTheme.typography.titleMedium)
                                        Spacer(Modifier.height(4.dp))
                                        Text("📅 ${activity.date}  📍 ${activity.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (selectedTab == 0) {
                                            Spacer(Modifier.height(8.dp))
                                            OutlinedButton(
                                                onClick = { vm.cancelRegistration(activity.id) },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) { Text("取消报名") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
