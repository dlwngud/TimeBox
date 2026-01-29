package com.wngud.timebox.ui.theme

import androidx.compose.ui.graphics.Color

// ============ Material Design 3 Color System ============
// Execution Focus Mode 프로젝트 기반 (#4f47e6)

// Primary Colors (메인 브랜드 컬러)
val DeepFocusIndigo = Color(0xFF4F46E5)          // Primary - 메인 브랜드 컬러
val DeepFocusIndigoLight = Color(0xFF6B63FF)     // Primary Light (라이트 모드용)
val DeepFocusIndigoDark = Color(0xFF3730A3)      // Primary Dark
val PrimaryContainer = Color(0xFFE0DFFE)         // Primary Container (라이트)
val PrimaryContainerDark = Color(0xFF2E1F8A)     // Primary Container (다크)
val OnPrimary = Color.White                      // Primary 위의 텍스트
val OnPrimaryContainer = Color(0xFF1A0F5C)       // Primary Container 위의 텍스트 (라이트)
val OnPrimaryContainerDark = Color(0xFFE0DFFE)   // Primary Container 위의 텍스트 (다크)

// Secondary Colors (강조 컬러)
val ElectricCyan = Color(0xFF22D3EE)             // Secondary - 강조 컬러
val SecondaryContainer = Color(0xFFCFF6FF)       // Secondary Container (라이트)
val SecondaryContainerDark = Color(0xFF004D61)   // Secondary Container (다크)
val OnSecondary = Color(0xFF003544)              // Secondary 위의 텍스트
val OnSecondaryContainer = Color(0xFF001F29)     // Secondary Container 위의 텍스트 (라이트)
val OnSecondaryContainerDark = Color(0xFFCFF6FF) // Secondary Container 위의 텍스트 (다크)

// Tertiary Colors (보조 강조 컬러)
val TertiaryPurple = Color(0xFF9C27B0)           // Tertiary
val TertiaryContainer = Color(0xFFF3E5F5)        // Tertiary Container (라이트)
val TertiaryContainerDark = Color(0xFF4A148C)    // Tertiary Container (다크)
val OnTertiary = Color.White                     // Tertiary 위의 텍스트
val OnTertiaryContainer = Color(0xFF38006B)      // Tertiary Container 위의 텍스트

// Background Colors
val DarkBackground = Color(0xFF1A1F2E)           // Dark 배경
val LightBackground = Color(0xFFF5F7FA)          // Light 배경
val OnBackgroundDark = Color(0xFFE0E0E0)         // Dark 배경 위의 텍스트
val OnBackgroundLight = Color(0xFF1A1A1A)        // Light 배경 위의 텍스트

// Surface Colors (카드, 다이얼로그 등)
val DarkSurface = Color(0xFF252B3A)              // Dark 서피스
val LightSurface = Color.White                   // Light 서피스
val DarkSurfaceVariant = Color(0xFF2C2C2C)       // Dark 서피스 변형
val LightSurfaceVariant = Color(0xFFF5F5F5)      // Light 서피스 변형
val OnSurfaceDark = Color(0xFFE0E0E0)            // Dark 서피스 위의 텍스트
val OnSurfaceLight = Color(0xFF1A1A1A)           // Light 서피스 위의 텍스트
val OnSurfaceVariantDark = Color(0xFFB0B0B0)    // Dark 서피스 변형 위의 텍스트
val OnSurfaceVariantLight = Color(0xFF888888)   // Light 서피스 변형 위의 텍스트

// Error Colors (경고, 삭제 등)
val ErrorRed = Color(0xFFFF5252)                 // Error
val ErrorContainer = Color(0xFFFFDAD6)           // Error Container (라이트)
val ErrorContainerDark = Color(0xFF93000A)       // Error Container (다크)
val OnError = Color.White                        // Error 위의 텍스트
val OnErrorContainer = Color(0xFF410002)         // Error Container 위의 텍스트 (라이트)
val OnErrorContainerDark = Color(0xFFFFDAD6)     // Error Container 위의 텍스트 (다크)

// Outline Colors (테두리, 구분선)
val OutlineDark = Color(0xFF4A4A4A)              // Dark 모드 아웃라인
val OutlineLight = Color(0xFFE0E0E0)             // Light 모드 아웃라인
val OutlineVariantDark = Color(0xFF2C2C2C)       // Dark 모드 아웃라인 변형
val OutlineVariantLight = Color(0xFFF5F5F5)      // Light 모드 아웃라인 변형

// ============ Legacy Colors (기존 호환성 유지) ============
val PrimaryBlue = DeepFocusIndigo                // DeepFocusIndigo와 동일
val CardBackground = DarkSurface                 // DarkSurface와 동일
val BackgroundGray = LightBackground             // LightBackground와 동일
val CardWhite = LightSurface                     // LightSurface와 동일

// Text Colors
val TextBlack = OnBackgroundLight
val TextGray = OnSurfaceVariantLight
val TextSecondary = Color(0xFF9CA3AF)
val TextTertiary = Color(0xFF757575)
val TextDark = Color(0xFF424242)
val DarkTextPrimary = OnBackgroundDark
val DarkTextSecondary = OnSurfaceVariantDark

// Accent Colors
val SuccessGreen = Color(0xFF4CAF50)
val AccentRed = ErrorRed
val AccentOrange = Color(0xFFFF9800)
val AccentPurple = TertiaryPurple
val AccentDeepPurple = Color(0xFF673AB7)
val AccentAmber = Color(0xFFFFA000)

// Stats/Chart Colors
val StatsBlue = Color(0xFF1565C0)
val StatsBlueBg = Color(0xFFE3F2FD)
val StatsBlueLight = Color(0xFFBBDEFB)
val StatsPurpleBg = Color(0xFFEDE7F6)

// Event Colors
val EventGreenBg = Color(0xFFE8F5E9)
val EventGreenBorder = Color(0xFF2ECC71)
val EventBlueBg = Color(0xFFE3F2FD)
val EventBlueBorder = DeepFocusIndigo

// Schedule Slot Colors (Light Mode)
val SchedulePrimaryBg = Color(0xFFE3F2FD)
val SchedulePrimaryBorder = DeepFocusIndigo
val ScheduleSecondaryBg = Color(0xFFE8EAF6)
val ScheduleSecondaryBorder = Color(0xFF5C6BC0)
val ScheduleTertiaryBg = Color(0xFFF5F5F5)

// Schedule Slot Colors (Dark Mode)
val SchedulePrimaryBgDark = Color(0xFF1A237E)
val SchedulePrimaryBorderDark = Color(0xFF5C6BC0)
val ScheduleSecondaryBgDark = Color(0xFF283593)
val ScheduleSecondaryBorderDark = Color(0xFF7986CB)
val ScheduleTertiaryBgDark = Color(0xFF2C2C2C)

// UI Element Colors
val IconBlue = Color(0xFF4A89F7)
val IconBlueBg = Color(0xFFE3EDFB)
val SwitchBlue = Color(0xFF186EF2)
val BorderGray = OutlineLight
val DisabledGray = Color(0xFF9E9E9E)
val SubtitleGray = Color(0xFFB0B0B0)