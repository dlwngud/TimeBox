package com.wngud.timebox.presentation

import androidx.lifecycle.ViewModel
import com.wngud.timebox.manager.AppThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val appThemeManager: AppThemeManager
) : ViewModel() {
    
    val themeMode: Flow<String> = appThemeManager.themeMode
}
