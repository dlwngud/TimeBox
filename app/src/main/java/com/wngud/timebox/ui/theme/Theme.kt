package com.wngud.timebox.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.wngud.timebox.presentation.ThemeViewModel

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    secondary = DarkTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    background = BackgroundGray,
    onBackground = TextBlack,
    surface = CardWhite,
    onSurface = TextBlack,
    secondary = TextGray
)

@Composable
fun TimeBoxTheme(
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val currentThemeMode by themeViewModel.themeMode.collectAsState(initial = "시스템")

    val darkTheme = when (currentThemeMode) {
        "다크" -> true
        "라이트" -> false
        "시스템" -> isSystemInDarkTheme()
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    // 상태바 아이콘 색상 설정
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                // 라이트 모드: 어두운 아이콘 (true)
                // 다크 모드: 밝은 아이콘 (false)
                isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
