package com.voicevault.recorder.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.voicevault.recorder.ui.api.ApiSettingsScreen
import com.voicevault.recorder.ui.categories.ManageCategoriesScreen
import com.voicevault.recorder.ui.chat.ChatScreen
import com.voicevault.recorder.ui.home.HomeScreen
import com.voicevault.recorder.ui.list.ListScreen
import com.voicevault.recorder.ui.player.PlayerScreen
import com.voicevault.recorder.ui.recyclebin.RecycleBinScreen
import com.voicevault.recorder.ui.search.SearchScreen
import com.voicevault.recorder.ui.settings.SettingsScreen

// navigation graph defining all app routes - ALL FIXED
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // home screen
        composable("home") {
            HomeScreen(
                onNavigateToList = { navController.navigate("list") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        // list screen - FIXED: all navigation working
        composable("list") {
            ListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { recordingId ->
                    navController.navigate("player/$recordingId")
                },
                onNavigateToSearch = { navController.navigate("search") },
                onNavigateToRecycleBin = { navController.navigate("recycle_bin") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToManageCategories = { navController.navigate("manage_categories") }
            )
        }

        // FIXED: search screen
        composable("search") {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { recordingId ->
                    navController.navigate("player/$recordingId")
                }
            )
        }

        // FIXED: recycle bin screen
        composable("recycle_bin") {
            RecycleBinScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // FIXED: manage categories screen
        composable("manage_categories") {
            ManageCategoriesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // player screen
        composable(
            route = "player/{recordingId}",
            arguments = listOf(navArgument("recordingId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recordingId = backStackEntry.arguments?.getLong("recordingId") ?: 0L
            PlayerScreen(
                recordingId = recordingId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { id ->
                    navController.navigate("chat/$id")
                }
            )
        }

        // chat screen
        composable(
            route = "chat/{recordingId}",
            arguments = listOf(navArgument("recordingId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recordingId = backStackEntry.arguments?.getLong("recordingId") ?: 0L
            ChatScreen(
                recordingId = recordingId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // settings screen
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToApiSettings = { navController.navigate("api_settings") }
            )
        }

        // api settings screen
        composable("api_settings") {
            ApiSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}