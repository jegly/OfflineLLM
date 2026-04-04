package com.jegly.offlineLLM.ui.navigation

sealed class Routes(val route: String) {
    data object Onboarding : Routes("onboarding")
    data object Chat : Routes("chat")
    data object Settings : Routes("settings")
    data object About : Routes("about")
    data object Help : Routes("help")
}
