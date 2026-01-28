package com.wngud.timebox.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.wngud.timebox.presentation.brainDump.BrainDumpRoute
import com.wngud.timebox.presentation.home.HomeRoute
import com.wngud.timebox.presentation.onBoarding.OnBoardingRoute
import com.wngud.timebox.presentation.setting.SettingRoute
import com.wngud.timebox.presentation.stats.StatsRoute

@Composable
fun TimeBoxNavGraph(
    startDestination: Screen = Screen.OnBoarding
) {
    // 🔑 1. Back Stack 상태 생성 및 유지 (NavController 대체)
    val backStack = rememberNavBackStack(startDestination)

    // 🔑 2. Navigation Actions 정의 (NavController.navigate() 대체)
    val navigate: (Screen) -> Unit = { destination ->
        backStack.add(destination)
    }

    // 🔑 3. Back Action 정의 (NavController.popBackStack() 대체)
    val onBack: () -> Unit = {
        backStack.removeLastOrNull()
    }

    // 🔑 4. NavDisplay를 사용하여 UI 렌더링
    NavDisplay(
        backStack = backStack,
        onBack = onBack, // 시스템 Back 버튼 처리
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),

        // entryProvider: Route와 Composable을 연결 (NavHost.composable 대체)
        entryProvider = entryProvider {

            // 온보딩 → 완료 시 Home으로 이동 (popUpTo 로직)
            entry<Screen.OnBoarding> {
                OnBoardingRoute(
                    onComplete = {
                        // 백 스택을 Home만 남기고 OnBoarding 제거
                        backStack.clear()
                        backStack.add(Screen.Home)
                    }
                )
            }

            // 홈
            entry<Screen.Home> {
                HomeRoute(
                    onNavigateToStats = { navigate(Screen.Stats) },
                    onNavigateToBrainDump = { navigate(Screen.BrainDump) },
                    onNavigateToSetting = { navigate(Screen.Setting) }
                )
            }

            entry<Screen.Stats> {
                StatsRoute(onBack = onBack)
            }

            // 브레인덤프
            entry<Screen.BrainDump> {
                BrainDumpRoute(onBack = onBack)
            }

            // 설정
            entry<Screen.Setting> {
                SettingRoute(onBack = onBack)
            }
        }
    )
}
