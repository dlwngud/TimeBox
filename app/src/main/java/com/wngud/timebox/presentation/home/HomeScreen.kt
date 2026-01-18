package com.wngud.timebox.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.wngud.timebox.data.modal.DailyStats
import com.wngud.timebox.data.modal.EventColorType
import com.wngud.timebox.data.modal.ScheduleSlot
import com.wngud.timebox.data.modal.Task
import com.wngud.timebox.presentation.components.TimeBoxerTopBar
import com.wngud.timebox.ui.theme.TimeBoxTheme
import java.time.LocalTime
import java.time.Duration
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.luminance
import com.wngud.timebox.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToStats: () -> Unit,
    onNavigateToBrainDump: () -> Unit,
    onNavigateToSetting: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val bigThreeTasks by viewModel.bigThreeTasks.collectAsState()
    val scheduleSlots by viewModel.scheduleSlots.collectAsState()
    val showBrainDumpSelector by viewModel.showBrainDumpSelector.collectAsState()
    val selectedTimeSlot by viewModel.selectedTimeSlot.collectAsState()
    val availableBrainDumpItems by viewModel.availableBrainDumpItems.collectAsState()

    val onTaskCheckChanged: (Task, Boolean) -> Unit = { task, _ ->
        viewModel.toggleTaskCompletion(task.id)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TimeBoxerTopBar(onNavigateToSetting) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToBrainDump,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
        }
    ) { innerPadding ->
        TimeBoxerContent(
            modifier = Modifier.padding(innerPadding),
            stats = DailyStats("3.5h", 5, 8, 85, 12),
            tasks = bigThreeTasks,
            scheduleSlots = scheduleSlots,
            onNavigateToStats = onNavigateToStats,
            userName = "사용자",
            onTaskCheckChanged = onTaskCheckChanged,
            onTimeSlotClick = viewModel::onTimeSlotClick,
            onSlotLongClick = viewModel::removeScheduleSlot
        )
    }
    
    // BrainDump Item Selector BottomSheet
    if (showBrainDumpSelector && selectedTimeSlot != null) {
        BrainDumpItemSelectorBottomSheet(
            items = availableBrainDumpItems,
            selectedTimeSlot = selectedTimeSlot!!,
            onItemSelected = { item ->
                viewModel.placeBrainDumpItem(item, selectedTimeSlot!!.first)
            },
            onDismiss = viewModel::dismissBrainDumpSelector
        )
    }
}

@Composable
fun TimeBoxerContent(
    modifier: Modifier = Modifier,
    stats: DailyStats,
    tasks: List<Task>,
    scheduleSlots: List<ScheduleSlot>,
    onNavigateToStats: () -> Unit,
    userName: String,
    onTaskCheckChanged: (Task, Boolean) -> Unit,
    onTimeSlotClick: (LocalTime) -> Unit,
    onSlotLongClick: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Daily Summary Card
        DailySummaryCardNew(stats, onNavigateToStats)

        Spacer(modifier = Modifier.height(24.dp))

        // Greeting & Big Three Header
        Text(
            text = "안녕하세요, $userName!",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "AI가 추천하는 오늘의 Big Three입니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = DisabledGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Big Three Tasks
        BigThreeSectionNew(tasks, onTaskCheckChanged) // onTaskCheckChanged 전달

        Spacer(modifier = Modifier.height(28.dp))

        // Timeline Header
        Text(
            text = "오늘의 타임라인",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Timeline
        TimelineSectionNew(
            scheduleSlots = scheduleSlots,
            onTimeSlotClick = onTimeSlotClick,
            onSlotLongClick = onSlotLongClick
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun DailySummaryCardNew(
    stats: DailyStats,
    onNavigateToStats: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToStats() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "2025.11.18 (화) 오늘의 결과",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "자세히 보기 >",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatsItem(
                    icon = "✓",
                    label = "완료된 작업",
//                    value = "${stats.completedTasks}/${stats.totalTasks}"
                    value = "5/8"
                )

                Spacer(modifier = Modifier.width(24.dp))

                StatsItem(
                    icon = "⏳",
                    label = "집중 시간",
                    value = stats.focusTime
                )
            }
        }
    }
}

@Composable
fun StatsItem(icon: String, label: String, value: String) {
    // MaterialTheme.colorScheme를 사용하여 앱의 테마 설정 감지
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isDarkTheme) Color(0xFF1E3A5F)
                    else EventBlueBg
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = DisabledGray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun BigThreeSectionNew(tasks: List<Task>, onTaskCheckChanged: (Task, Boolean) -> Unit) { // onTaskCheckChanged 추가
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        tasks.forEach { task ->
            BigThreeTaskItem(task, onTaskCheckChanged) // onTaskCheckChanged 전달
        }
    }
}

@Composable
fun BigThreeTaskItem(task: Task, onToggleComplete: (Task, Boolean) -> Unit) { // onToggleComplete 추가
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // MaterialTheme.colorScheme를 사용하여 앱의 테마 설정 감지
            val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
            
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (task.isCompleted) SwitchBlue
                        else if (isDarkTheme) Color(0xFF424242)
                        else BorderGray
                    )
                    .clickable { onToggleComplete(task, !task.isCompleted) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = if (task.isCompleted) "Completed" else "Not Completed",
                    tint = if (task.isCompleted) {
                        Color.White
                    } else {
                        // 미체크 상태: 테마에 따라 반투명 체크 아이콘 표시
                        if (isDarkTheme) Color.White.copy(alpha = 0.3f)
                        else Color.Gray.copy(alpha = 0.4f)
                    },
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            // "AI 추천" 텍스트 제거
        }
    }
}
    @OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineSlotCard(
    slot: ScheduleSlot,
    modifier: Modifier = Modifier,
    onSlotLongClick: (String) -> Unit = {}
) {
    // MaterialTheme.colorScheme를 사용하여 앱의 테마 설정 감지
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    
    // 다크모드에서는 항상 surface 배경 사용
    val backgroundColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.surface
    } else {
        when (slot.colorType) {
            EventColorType.BLUE -> SchedulePrimaryBg
            EventColorType.GREEN -> ScheduleSecondaryBg
            EventColorType.GRAY -> ScheduleTertiaryBg
        }
    }

    val borderColor = when (slot.colorType) {
        EventColorType.BLUE -> if (isDarkTheme) SchedulePrimaryBorderDark else SchedulePrimaryBorder
        EventColorType.GREEN -> if (isDarkTheme) ScheduleSecondaryBorderDark else ScheduleSecondaryBorder
        else -> Color.Transparent
    }

    var showContextMenu by remember { mutableStateOf(false) }

    Box {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { },
                    onLongClick = { showContextMenu = true }
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            border = if (borderColor != Color.Transparent) {
                BorderStroke(2.dp, borderColor)
            } else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = slot.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (slot.colorType == EventColorType.BLUE) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = "Big Three",
                        tint = EventBlueBorder,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        // Context Menu (DropdownMenu)
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🗑️",
                            fontSize = 18.sp
                        )
                        Text(
                            text = "삭제",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Red
                        )
                    }
                },
                onClick = {
                    onSlotLongClick(slot.id)
                    showContextMenu = false
                }
            )
        }
    }
}

@Composable
fun HourTimelineRow(
    hour: Int,
    slotsForThisHour: List<ScheduleSlot>,
    onTimeSlotClick: (LocalTime) -> Unit,
    onSlotLongClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Time label
        val formattedTime = when (hour) {
            0 -> "오전 12시"
            in 1..11 -> "오전 ${hour}시"
            12 -> "오후 12시"
            in 13..23 -> "오후 ${hour - 12}시"
            else -> ""
        }
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.bodyMedium,
            color = DisabledGray,
            modifier = Modifier.width(90.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Right area: divided into 30-minute slots
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 00분 슬롯
            TimeSlotRow(
                hour = hour,
                minute = 0,
                slots = slotsForThisHour.filter { it.startTime.minute == 0 },
                onTimeSlotClick = onTimeSlotClick,
                onSlotLongClick = onSlotLongClick
            )

            // 30분 슬롯
            TimeSlotRow(
                hour = hour,
                minute = 30,
                slots = slotsForThisHour.filter { it.startTime.minute == 30 },
                onTimeSlotClick = onTimeSlotClick,
                onSlotLongClick = onSlotLongClick
            )
        }
    }
}

/**
 * 30분 단위 시간 슬롯 행
 */
@Composable
fun TimeSlotRow(
    hour: Int,
    minute: Int,
    slots: List<ScheduleSlot>,
    onTimeSlotClick: (LocalTime) -> Unit,
    onSlotLongClick: (String) -> Unit
) {
    if (slots.isNotEmpty()) {
        // 일정이 있는 경우
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slots.sortedBy { it.startTime }.forEach { slot ->
                TimelineSlotCard(
                    slot = slot,
                    modifier = Modifier,
                    onSlotLongClick = onSlotLongClick
                )
            }
        }
    } else {
        // 빈 슬롯 - 클릭 가능한 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clickable {
                    onTimeSlotClick(LocalTime.of(hour, minute))
                }
                .background(Color.Transparent)
        )
    }
}

// Updated TimelineSectionNew to display 00-23 hours
@Composable
fun TimelineSectionNew(
    scheduleSlots: List<ScheduleSlot>,
    onTimeSlotClick: (LocalTime) -> Unit,
    onSlotLongClick: (String) -> Unit
) {
    val slotsByHour = remember(scheduleSlots) {
        scheduleSlots.groupBy { it.startTime.hour }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (hour in 0..23) {
            val slotsForThisHour = slotsByHour[hour] ?: emptyList()
            HourTimelineRow(
                hour = hour,
                slotsForThisHour = slotsForThisHour,
                onTimeSlotClick = onTimeSlotClick,
                onSlotLongClick = onSlotLongClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    TimeBoxTheme {
        HomeScreen({}, {}, {})
    }
}