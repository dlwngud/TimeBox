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
    startDestination: Screen = Screen.OnBoarding
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // 온보딩 → 완료 시 Home으로 이동
        composable<Screen.OnBoarding> {
            OnBoardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.OnBoarding) { inclusive = true }
                    }
                }
            )
        }

        // 홈
        composable<Screen.Home> {
            HomeScreen(
                onNavigateToStats = { navController.navigate(Screen.Stats) },
                onNavigateToBrainDump = { navController.navigate(Screen.BrainDump) },
                onNavigateToSetting = { navController.navigate(Screen.Setting) }
            )
        }
//
        // 통계
        composable<Screen.Stats> {
            StatsScreen(onBack = { navController.popBackStack() })
        }

        // 브레인덤프
        composable<Screen.BrainDump> {
            BrainDumpScreen(onBack = { navController.popBackStack() })
        }

        // 설정
        composable<Screen.Setting> {
            SettingScreen(onBack = { navController.popBackStack() })
        }
    }
}