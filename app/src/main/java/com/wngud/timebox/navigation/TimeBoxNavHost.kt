package com.wngud.timebox.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wngud.timebox.presentation.brainDump.BrainDumpScreen
import com.wngud.timebox.presentation.home.HomeScreen
import com.wngud.timebox.presentation.onBoarding.OnBoardingScreen
import com.wngud.timebox.presentation.setting.SettingScreen
import com.wngud.timebox.presentation.stats.StatsScreen

@Composable
fun TimeBoxNavHost(
    navController: NavHostController,
    startDestination: String = Screen.OnBoarding.route
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // 온보딩 → 완료 시 Home으로 이동
        composable(Screen.OnBoarding.route) {
            OnBoardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.OnBoarding.route) { inclusive = true }
                    }
                }
            )
        }

        // 홈
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToBrainDump = { navController.navigate(Screen.BrainDump.route) },
                onNavigateToSetting = { navController.navigate(Screen.Setting.route) }
            )
        }
//
        // 통계
        composable(Screen.Stats.route) {
            StatsScreen(onBack = { navController.popBackStack() })
        }

        // 브레인덤프
        composable(Screen.BrainDump.route) {
            BrainDumpScreen(onBack = { navController.popBackStack() })
        }

        // 설정
        composable(Screen.Setting.route) {
            SettingScreen(onBack = { navController.popBackStack() })
        }
    }
}