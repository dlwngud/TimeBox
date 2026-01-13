package com.wngud.timebox.presentation.setting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wngud.timebox.ui.theme.BorderGray
import com.wngud.timebox.ui.theme.IconBlue
import com.wngud.timebox.ui.theme.IconBlueBg
import com.wngud.timebox.ui.theme.SubtitleGray
import com.wngud.timebox.ui.theme.SwitchBlue

// ------------------------------------------------------------------------
// 2. Stateful Composable (상태를 관리하는 최상위 컴포저블)
// ------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBack: () -> Unit,
    viewModel: SettingViewModel = hiltViewModel()
) {
    // ViewModel에서 UI 상태를 수집
    val uiState by viewModel.uiState.collectAsState()

    SettingContent(
        uiState = uiState,
        onBack = onBack,
        onNotificationToggle = { viewModel.processIntent(SettingIntent.ToggleNotification(it)) },
        onVibrationToggle = { viewModel.processIntent(SettingIntent.ToggleVibration(it)) },
        onTimeClick = { viewModel.processIntent(SettingIntent.OnTimeClick) },
        onThemeClick = { viewModel.processIntent(SettingIntent.OnThemeClick) },
        onDismissThemeDialog = { viewModel.processIntent(SettingIntent.DismissThemeDialog) },
        onThemeSelected = { viewModel.processIntent(SettingIntent.SetThemeMode(it)) }
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
    onThemeClick: () -> Unit,
    onDismissThemeDialog: () -> Unit,
    onThemeSelected: (String) -> Unit
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
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                iconColor = IconBlue,
                iconBgColor = IconBlueBg,
                title = "알림 받기",
                control = {
                    Switch(
                        checked = uiState.isNotificationEnabled,
                        onCheckedChange = onNotificationToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SwitchBlue,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = BorderGray,
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
                            color = SwitchBlue,
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
                            checkedTrackColor = SwitchBlue,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = BorderGray,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            )

            // 4. 테마 (Text)
            SettingItemCard(
                icon = if (uiState.themeMode == "라이트") "☀️" else "🌙",
                iconColor = Color(0xFFFF9800),
                iconBgColor = Color(0xFFFFF3E0),
                title = "테마",
                onClick = onThemeClick,
                control = {
                    Text(
                        text = uiState.themeMode,
                        color = SwitchBlue,
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
    
    // 테마 선택 다이얼로그
    if (uiState.showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.themeMode,
            onDismiss = onDismissThemeDialog,
            onThemeSelected = onThemeSelected
        )
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
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (onClick != null && enabled) Modifier.clickable { onClick() } else Modifier
            ),
        shape = RoundedCornerShape(20.dp), // 둥근 모서리
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                            color = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray
                        )
                    )
                    if (subTitle != null) {
                        Text(
                            text = subTitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SubtitleGray,
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

/**
 * 토스 스타일의 테마 선택 다이얼로그
 */
@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onDismiss: () -> Unit,
    onThemeSelected: (String) -> Unit
) {
    var selectedTheme by remember { mutableStateOf(currentTheme) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "테마 선택",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 라이트 모드 옵션
                ThemeOptionItem(
                    icon = "☀️",
                    title = "라이트",
                    isSelected = selectedTheme == "라이트",
                    onClick = { selectedTheme = "라이트" }
                )
                
                // 다크 모드 옵션
                ThemeOptionItem(
                    icon = "🌙",
                    title = "다크",
                    isSelected = selectedTheme == "다크",
                    onClick = { selectedTheme = "다크" }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onThemeSelected(selectedTheme)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SwitchBlue
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "확인",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )
            }
        },
        dismissButton = null
    )
}

/**
 * 테마 선택 옵션 아이템 (토스 스타일)
 */
@Composable
fun ThemeOptionItem(
    icon: String,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) IconBlueBg else MaterialTheme.colorScheme.background
        ),
        border = if (isSelected) BorderStroke(2.dp, SwitchBlue) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 아이콘
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) SwitchBlue.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = icon,
                        fontSize = 20.sp
                    )
                }
                
                // 제목
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) SwitchBlue else MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            
            // 선택 표시
            if (isSelected) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(SwitchBlue)
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ThemeSelectionDialogPreview() {
    MaterialTheme {
        ThemeSelectionDialog("라이트", {}, {})
    }
}

@Preview(showBackground = true)
@Composable
fun ThemeOptionItemPreview() {
    MaterialTheme {
        ThemeOptionItem("☀️", "라이트", true, {})
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