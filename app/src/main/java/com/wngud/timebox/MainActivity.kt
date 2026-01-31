package com.wngud.timebox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.wngud.timebox.presentation.ThemeViewModel
import com.wngud.timebox.ui.theme.TimeBoxTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState(initial = "시스템")

            val darkTheme = when (themeMode) {
                "다크" -> true
                "라이트" -> false
                else -> isSystemInDarkTheme()
            }

            TimeBoxTheme(darkTheme = darkTheme) {
                TimeBoxApp()
            }
        }
    }
}
