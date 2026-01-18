package com.example.diary.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.diary.ui.screens.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Calendar : Screen("calendar")
    object Search : Screen("search")
    object Settings : Screen("settings")
    object Tags : Screen("tags")
    object DiaryDetail : Screen("diary/{diaryId}") {
        fun createRoute(diaryId: Long) = "diary/$diaryId"
    }
    object EditDiary : Screen("edit/{diaryId}") {
        fun createRoute(diaryId: Long = 0L) = "edit/$diaryId"
    }
}

@Composable
fun DiaryNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDetail = { diaryId ->
                    navController.navigate(Screen.DiaryDetail.createRoute(diaryId))
                },
                onNavigateToEdit = { diaryId ->
                    navController.navigate(Screen.EditDiary.createRoute(diaryId))
                },
                onNavigateToCalendar = {
                    navController.navigate(Screen.Calendar.route)
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { diaryId ->
                    navController.navigate(Screen.DiaryDetail.createRoute(diaryId))
                },
                onNavigateToEdit = { diaryId ->
                    navController.navigate(Screen.EditDiary.createRoute(diaryId))
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { diaryId ->
                    navController.navigate(Screen.DiaryDetail.createRoute(diaryId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTags = {
                    navController.navigate(Screen.Tags.route)
                }
            )
        }

        composable(Screen.Tags.route) {
            TagManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DiaryDetail.route,
            arguments = listOf(navArgument("diaryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val diaryId = backStackEntry.arguments?.getLong("diaryId") ?: 0L
            DiaryDetailScreen(
                diaryId = diaryId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = {
                    navController.navigate(Screen.EditDiary.createRoute(diaryId))
                }
            )
        }

        composable(
            route = Screen.EditDiary.route,
            arguments = listOf(navArgument("diaryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val diaryId = backStackEntry.arguments?.getLong("diaryId") ?: 0L
            EditDiaryScreen(
                diaryId = diaryId,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { savedDiaryId ->
                    navController.popBackStack()
                    if (diaryId == 0L) {
                        navController.navigate(Screen.DiaryDetail.createRoute(savedDiaryId))
                    }
                }
            )
        }
    }
}
