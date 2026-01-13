package com.wngud.timebox.manager

import com.wngud.timebox.data.datastore.ThemeDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppThemeManager @Inject constructor(
    private val themeDataStore: ThemeDataStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * 현재 테마 모드를 Flow로 반환합니다.
     * "라이트" 또는 "다크"
     */
    val themeMode: Flow<String> = themeDataStore.getThemeMode()

    /**
     * 테마 모드를 설정하고 DataStore에 저장합니다.
     * @param mode "라이트" 또는 "다크"
     */
    fun setThemeMode(mode: String) {
        scope.launch {
            themeDataStore.saveThemeMode(mode)
        }
    }
}
