package com.example.campushub.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.campushub.ui.screens.*

data class BottomNavItem(val label: String, val route: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector)

object Routes {
    const val EXPLORE = "explore"
    const val MY_ACTIVITIES = "my_activities"
    const val PROFILE = "profile"
    const val NOTIFICATION = "notification"
    const val DETAIL = "detail/{activityId}"
    const val CREATE = "create"
    fun detail(activityId: Int) = "detail/$activityId"
}

val bottomNavItems = listOf(
    BottomNavItem("发现", Routes.EXPLORE, Icons.Filled.Explore, Icons.Outlined.Explore),
    BottomNavItem("我的", Routes.MY_ACTIVITIES, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    BottomNavItem("设置", Routes.PROFILE, Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = { Icon(if (selected) item.selectedIcon else item.unselectedIcon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true; restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentDestination?.route == Routes.EXPLORE) {
                FloatingActionButton(onClick = { navController.navigate(Routes.CREATE) }) {
                    Icon(Icons.Default.Add, contentDescription = "发布活动")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.EXPLORE,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            composable(Routes.EXPLORE) {
                ExploreScreen(
                    onActivityClick = { navController.navigate(Routes.detail(it)) },
                    onNotificationClick = { navController.navigate(Routes.NOTIFICATION) }
                )
            }
            composable(Routes.MY_ACTIVITIES) {
                MyActivitiesScreen(
                    onActivityClick = { navController.navigate(Routes.detail(it)) },
                    onNavigateToExplore = {
                        navController.navigate(Routes.EXPLORE) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true; restoreState = true
                        }
                    }
                )
            }
            composable(Routes.PROFILE) {
                SettingsScreen()
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("activityId") { type = NavType.IntType })
            ) { entry ->
                val id = entry.arguments?.getInt("activityId") ?: return@composable
                DetailScreen(activityId = id, onNavigateBack = { navController.popBackStack() })
            }
            composable(Routes.CREATE) {
                CreateActivityScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Routes.NOTIFICATION) {
                NotificationScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onActivityClick = { navController.navigate(Routes.detail(it)) }
                )
            }
        }
    }
}
