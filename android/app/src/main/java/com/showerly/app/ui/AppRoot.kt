package com.showerly.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.showerly.app.di.AppContainer
import com.showerly.app.ui.home.HomeScreen
import com.showerly.app.ui.settings.SettingsScreen

@Composable
fun AppRoot(container: AppContainer) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                container = container,
                onOpenSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                container = container,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
