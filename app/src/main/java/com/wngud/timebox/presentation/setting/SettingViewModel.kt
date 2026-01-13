package com.wngud.timebox.presentation.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wngud.timebox.manager.AppThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ------------------------------------------------------------------------
// 1. State Definition (상태 정의) - ViewModel 내부
// ------------------------------------------------------------------------
data class SettingUiState(
    val isNotificationEnabled: Boolean = true,
    val notificationTime: String = "오전 9:00",
    val isVibrationEnabled: Boolean = false,
    val themeMode: String = "라이트", // DataStore에서 로드된 테마
    val showThemeDialog: Boolean = false, // 테마 선택 다이얼로그 표시 여부
    val isCalendarSyncEnabled: Boolean = false, 
    val appVersion: String = "1.0.0"
)

// ------------------------------------------------------------------------
// 2. Intent Definition (사용자 액션 정의)
// ------------------------------------------------------------------------
sealed class SettingIntent {
    data class ToggleNotification(val enabled: Boolean) : SettingIntent()
    data class ToggleVibration(val enabled: Boolean) : SettingIntent()
    data object OnTimeClick : SettingIntent()
    data object OnThemeClick : SettingIntent()
    data object DismissThemeDialog : SettingIntent()
    data class SetThemeMode(val mode: String) : SettingIntent()
}

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val appThemeManager: AppThemeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState: StateFlow<SettingUiState> = _uiState.asStateFlow()

    init {
        // DataStore에서 테마 모드를 수집하여 UI 상태에 반영
        viewModelScope.launch {
            appThemeManager.themeMode.collect { mode ->
                _uiState.update { currentState ->
                    currentState.copy(themeMode = mode)
                }
            }
        }
    }

    fun processIntent(intent: SettingIntent) {
        when (intent) {
            is SettingIntent.ToggleNotification -> {
                _uiState.update { it.copy(isNotificationEnabled = intent.enabled) }
            }
            is SettingIntent.ToggleVibration -> {
                _uiState.update { it.copy(isVibrationEnabled = intent.enabled) }
            }
            SettingIntent.OnTimeClick -> {
                // TODO: 시간 설정 다이얼로그 처리
            }
            SettingIntent.OnThemeClick -> {
                _uiState.update { it.copy(showThemeDialog = true) }
            }
            SettingIntent.DismissThemeDialog -> {
                _uiState.update { it.copy(showThemeDialog = false) }
            }
            is SettingIntent.SetThemeMode -> {
                appThemeManager.setThemeMode(intent.mode) // DataStore에 저장
                _uiState.update { it.copy(showThemeDialog = false) } // 다이얼로그 닫기
            }
        }
    }
}
