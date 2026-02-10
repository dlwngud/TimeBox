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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wngud.timebox.ui.theme.TimeBoxTheme

/**
 * [Stateful] 설정 화면의 Route 컴포저블
 */
@Composable
fun SettingRoute(
    onBack: () -> Unit,
    viewModel: SettingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingScreen(
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

/**
 * [Stateless] 설정 UI 컴포저블
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
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
                    Text(text = "설정", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기") }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingItemCard(
                icon = "🔔", 
                iconColor = MaterialTheme.colorScheme.primary, 
                iconBgColor = MaterialTheme.colorScheme.primaryContainer, 
                title = "알림 받기", 
                control = { 
                    Switch(
                        checked = uiState.isNotificationEnabled, 
                        onCheckedChange = onNotificationToggle, 
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary, 
                            checkedTrackColor = MaterialTheme.colorScheme.primary, 
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant, 
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant, 
                            uncheckedBorderColor = Color.Transparent
                        )
                    ) 
                }
            )
            SettingItemCard(
                icon = "⏰", 
                iconColor = MaterialTheme.colorScheme.secondary, 
                iconBgColor = MaterialTheme.colorScheme.secondaryContainer, 
                title = "알림 시간", 
                onClick = onTimeClick, 
                control = { 
                    Row(verticalAlignment = Alignment.CenterVertically) { 
                        Text(
                            text = uiState.notificationTime, 
                            color = MaterialTheme.colorScheme.primary, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp)) 
                    } 
                }
            )
            SettingItemCard(
                icon = "📳", 
                iconColor = MaterialTheme.colorScheme.tertiary, 
                iconBgColor = MaterialTheme.colorScheme.tertiaryContainer, 
                title = "진동", 
                control = { 
                    Switch(
                        checked = uiState.isVibrationEnabled, 
                        onCheckedChange = onVibrationToggle, 
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary, 
                            checkedTrackColor = MaterialTheme.colorScheme.primary, 
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant, 
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant, 
                            uncheckedBorderColor = Color.Transparent
                        )
                    ) 
                }
            )
            SettingItemCard(
                icon = if (uiState.themeMode == "라이트") "☀️" else "🌙", 
                iconColor = MaterialTheme.colorScheme.primary, 
                iconBgColor = MaterialTheme.colorScheme.primaryContainer, 
                title = "테마", 
                onClick = onThemeClick, 
                control = { 
                    Text(
                        text = uiState.themeMode, 
                        color = MaterialTheme.colorScheme.primary, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 14.sp
                    ) 
                }
            )
            SettingItemCard(
                icon = "ℹ️", 
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant, 
                iconBgColor = MaterialTheme.colorScheme.surfaceVariant, 
                title = "앱 버전", 
                control = { 
                    Text(
                        text = uiState.appVersion, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        fontSize = 14.sp
                    ) 
                }
            )
        }
    }
    
    if (uiState.showThemeDialog) {
        ThemeSelectionDialog(currentTheme = uiState.themeMode, onDismiss = onDismissThemeDialog, onThemeSelected = onThemeSelected)
    }
}

@Composable
fun SettingItemCard(icon: String, iconColor: Color, iconBgColor: Color, title: String, subTitle: String? = null, control: @Composable () -> Unit, onClick: (() -> Unit)? = null, enabled: Boolean = true) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Card(
        modifier = Modifier.fillMaxWidth().height(72.dp).clip(RoundedCornerShape(20.dp)).then(if (onClick != null && enabled) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).clip(CircleShape).background(if (isDarkTheme) iconBgColor.copy(alpha = 0.3f) else iconBgColor)) { Text(text = icon, fontSize = 16.sp) }
                Spacer(modifier = Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, color = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray))
                    if (subTitle != null) Text(text = subTitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp))
                }
            }
            control()
        }
    }
}

@Composable
fun ThemeSelectionDialog(currentTheme: String, onDismiss: () -> Unit, onThemeSelected: (String) -> Unit) {
    var selectedTheme by remember { mutableStateOf(currentTheme) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        title = { Text(text = "테마 선택", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp), modifier = Modifier.padding(top = 8.dp)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ThemeOptionItem(icon = "⚙️", title = "시스템", description = "기기 설정을 따릅니다", isSelected = selectedTheme == "시스템", onClick = { selectedTheme = "시스템" })
                ThemeOptionItem(icon = "☀️", title = "라이트", description = "밝은 테마", isSelected = selectedTheme == "라이트", onClick = { selectedTheme = "라이트" })
                ThemeOptionItem(icon = "🌙", title = "다크", description = "어두운 테마", isSelected = selectedTheme == "다크", onClick = { selectedTheme = "다크" })
            }
        },
        confirmButton = { Button(onClick = { onThemeSelected(selectedTheme) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(text = "확인", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp), color = MaterialTheme.colorScheme.onPrimary) } }
    )
}

@Composable
fun ThemeOptionItem(icon: String, title: String, description: String? = null, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(if (description != null) 72.dp else 64.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).clip(CircleShape).background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)) { Text(text = icon, fontSize = 20.sp) }
                Column {
                    Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface))
                    if (description != null) Text(text = description, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
            if (isSelected) Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)) { Text(text = "✓", color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingScreenPreview() {
    TimeBoxTheme {
        SettingScreen(
            uiState = SettingUiState(themeMode = "시스템"),
            onBack = {},
            onNotificationToggle = {},
            onVibrationToggle = {},
            onTimeClick = {},
            onThemeClick = {},
            onDismissThemeDialog = {},
            onThemeSelected = {}
        )
    }
}
