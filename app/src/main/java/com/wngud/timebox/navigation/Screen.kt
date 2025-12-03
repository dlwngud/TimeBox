package com.wngud.timebox.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen: NavKey {

    @Serializable
    data object OnBoarding : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data object Stats : Screen

    @Serializable
    data object BrainDump : Screen

    @Serializable
    data object Setting : Screen
}