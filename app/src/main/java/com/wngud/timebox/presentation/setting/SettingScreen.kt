package com.wngud.timebox.presentation.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ------------------------------------------------------------------------
// 1. State Definition (상태 정의)
// 현업에서는 보통 ViewModel에서 관리하는 State data class입니다.
// ------------------------------------------------------------------------
data class SettingUiState(
    val isNotificationEnabled: Boolean = true,
    val notificationTime: String = "오전 9:00",
    val isVibrationEnabled: Boolean = false,
    val themeMode: String = "라이트",
    val isCalendarSyncEnabled: Boolean = false, // 비활성화 상태 표현을 위해 false
    val appVersion: String = "1.0.0"
)

// ------------------------------------------------------------------------
// 2. Stateful Composable (상태를 관리하는 최상위 컴포저블)
// ------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBack: () -> Unit
) {
    // 실제 앱에서는 ViewModel을 주입받아 collectAsState()를 사용합니다.
    // 여기서는 remember를 사용하여 상태를 관리합니다.
    var uiState by remember { mutableStateOf(SettingUiState()) }

    SettingContent(
        uiState = uiState,
        onBack = onBack,
        onNotificationToggle = { uiState = uiState.copy(isNotificationEnabled = it) },
        onVibrationToggle = { uiState = uiState.copy(isVibrationEnabled = it) },
        onTimeClick = { /* 시간 설정 다이얼로그 호출 */ },
        onThemeClick = { /* 테마 설정 바텀시트 호출 */ }
    )
}

// ------------------------------------------------------------------------
// 3. Stateless Composable (UI 렌더링 전담)
// ------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingContent(
    uiState: SettingUiState,
    onBack: () -> Unit,
    onNotificationToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onTimeClick: () -> Unit,
    onThemeClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "설정",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF8F9FB) // 배경색 일치
                )
            )
        },
        containerColor = Color(0xFFF8F9FB) // 전체 배경색
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. 알림 받기 (Switch)
            SettingItemCard(
                icon = "🔔",
                iconColor = Color(0xFF4A89F7),
                iconBgColor = Color(0xFFE3EDFB),
                title = "알림 받기",
                control = {
                    Switch(
                        checked = uiState.isNotificationEnabled,
                        onCheckedChange = onNotificationToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF186EF2),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFE0E0E0),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            )

            // 2. 알림 시간 (Dropdown/Text)
            SettingItemCard(
                icon = "⏰",
                iconColor = Color(0xFF9C27B0),
                iconBgColor = Color(0xFFF3E5F5),
                title = "알림 시간",
                onClick = onTimeClick,
                control = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiState.notificationTime,
                            color = Color(0xFF186EF2),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        // 드롭다운 화살표 아이콘이 필요하다면 추가 (이미지 상에는 텍스트 옆 쉐브론)
                        Spacer(modifier = Modifier.width(4.dp))
                        // Icon(Icons.Default.KeyboardArrowDown, ..., tint = Color(0xFF186EF2))
                    }
                }
            )

            // 3. 진동 (Switch)
            SettingItemCard(
                icon = "📳",
                iconColor = Color(0xFF4CAF50),
                iconBgColor = Color(0xFFE8F5E9),
                title = "진동",
                control = {
                    Switch(
                        checked = uiState.isVibrationEnabled,
                        onCheckedChange = onVibrationToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF186EF2),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFE0E0E0),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            )

            // 4. 테마 (Text)
            SettingItemCard(
                icon = "🎨",
                iconColor = Color(0xFFFF9800),
                iconBgColor = Color(0xFFFFF3E0),
                title = "테마",
                onClick = onThemeClick,
                control = {
                    Text(
                        text = uiState.themeMode,
                        color = Color(0xFF186EF2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            )

            // 5. 캘린더 연동 (Disabled, Subtext)
            SettingItemCard(
                icon = "📅",
                iconColor = Color(0xFF9E9E9E),
                iconBgColor = Color(0xFFEEEEEE),
                title = "캘린더 연동",
                subTitle = "곧 오픈돼요",
                enabled = false,
                control = {
                    Switch(
                        checked = false,
                        onCheckedChange = null,
                        enabled = false,
                        colors = SwitchDefaults.colors(
                            disabledCheckedTrackColor = Color(0xFFE0E0E0),
                            disabledUncheckedTrackColor = Color(0xFFEEEEEE),
                            disabledUncheckedThumbColor = Color.White
                        )
                    )
                }
            )

            // 6. 앱 버전 (Info)
            SettingItemCard(
                icon = "ℹ️",
                iconColor = Color(0xFF616161),
                iconBgColor = Color(0xFFECEFF1),
                title = "앱 버전",
                control = {
                    Text(
                        text = uiState.appVersion,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            )
        }
    }
}

// ------------------------------------------------------------------------
// 4. Reusable Components (재사용 가능한 컴포넌트)
// ------------------------------------------------------------------------

/**
 * 설정 화면의 각 항목을 표현하는 카드 컴포넌트
 */
@Composable
fun SettingItemCard(
    icon: String,
    iconColor: Color,
    iconBgColor: Color,
    title: String,
    subTitle: String? = null,
    control: @Composable () -> Unit,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .then(
                if (onClick != null && enabled) Modifier.clickable { onClick() } else Modifier
            ),
        shape = RoundedCornerShape(20.dp), // 둥근 모서리
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp) // 살짝 그림자
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 아이콘 박스
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconBgColor)
                ) {
                    Text(
                        text = icon,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 텍스트 영역
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            color = if (enabled) Color(0xFF111111) else Color.Gray
                        )
                    )
                    if (subTitle != null) {
                        Text(
                            text = subTitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFB0B0B0),
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            // 우측 컨트롤 (Switch or Text)
            control()
        }
    }
}

// ------------------------------------------------------------------------
// Preview
// ------------------------------------------------------------------------
@Preview(showBackground = true)
@Composable
fun SettingScreenPreview() {
    MaterialTheme {
        SettingScreen(onBack = {})
    }
}