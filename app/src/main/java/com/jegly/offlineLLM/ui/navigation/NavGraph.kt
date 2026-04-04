package com.jegly.offlineLLM.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jegly.offlineLLM.ui.screens.AboutScreen
import com.jegly.offlineLLM.ui.screens.ChatScreen
import com.jegly.offlineLLM.ui.screens.HelpScreen
import com.jegly.offlineLLM.ui.screens.OnboardingScreen
import com.jegly.offlineLLM.ui.screens.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Routes.Chat.route) {
                        popUpTo(Routes.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Chat.route) {
            ChatScreen(
                onNavigateToSettings = {
                    navController.navigate(Routes.Settings.route)
                }
            )
        }

        composable(Routes.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate(Routes.About.route) },
                onNavigateToHelp = { navController.navigate(Routes.Help.route) },
            )
        }

        composable(Routes.About.route) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.Help.route) {
            HelpScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
