package com.wngud.timebox.navigation

sealed class Screen(val route: String) {
    data object OnBoarding : Screen("onboarding")

    data object Home : Screen("home")

    data object Stats : Screen("stats")

    data object BrainDump : Screen("braindump")

    data object Setting : Screen("setting")
}