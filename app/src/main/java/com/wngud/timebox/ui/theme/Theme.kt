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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Material Design 3 다크 모드 컬러 스킴
 * Execution Focus Mode 프로젝트 기반 (#4f47e6)
 */
private val DarkColorScheme = darkColorScheme(
    // Primary
    primary = DeepFocusIndigo,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    
    // Secondary
    secondary = ElectricCyan,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    
    // Tertiary
    tertiary = TertiaryPurple,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainer,
    
    // Error
    error = ErrorRed,
    onError = OnError,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    
    // Background
    background = DarkBackground,
    onBackground = OnBackgroundDark,
    
    // Surface
    surface = DarkSurface,
    onSurface = OnSurfaceDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnSurfaceVariantDark,
    
    // Outline
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

/**
 * Material Design 3 라이트 모드 컬러 스킴
 * Execution Focus Mode 프로젝트 기반 (#4f47e6)
 */
private val LightColorScheme = lightColorScheme(
    // Primary
    primary = DeepFocusIndigo,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    
    // Secondary
    secondary = ElectricCyan,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    
    // Tertiary
    tertiary = TertiaryPurple,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    
    // Error
    error = ErrorRed,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    
    // Background
    background = LightBackground,
    onBackground = OnBackgroundLight,
    
    // Surface
    surface = LightSurface,
    onSurface = OnSurfaceLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = OnSurfaceVariantLight,
    
    // Outline
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

/**
 * TimeBox 앱의 테마 컴포저블
 * 
 * Material Design 3 컬러 시스템을 완벽하게 구현했습니다.
 * Execution Focus Mode 프로젝트의 디자인 테마(#4f47e6)를 기반으로 합니다.
 * 
 * @param darkTheme 다크 테마 사용 여부. 기본값은 시스템 설정을 따름
 * @param dynamicColor Android 12+ 동적 색상 사용 여부
 * 
 * **현업 베스트 프랙티스:**
 * 테마 설정은 DataStore나 SharedPreferences로 관리하고,
 * CompositionLocal을 통해 전달하는 것이 일반적입니다.
 * ViewModel은 Theme.kt에서 직접 사용하지 않습니다.
 * 
 * **사용 예시:**
 * ```kotlin
 * // MainActivity 또는 최상위에서
 * val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
 * val isDarkTheme = when (themeMode) {
 *     ThemeMode.LIGHT -> false
 *     ThemeMode.DARK -> true
 *     ThemeMode.SYSTEM -> isSystemInDarkTheme()
 * }
 * TimeBoxTheme(darkTheme = isDarkTheme) { ... }
 * ```
 */
@Composable
fun TimeBoxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
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
