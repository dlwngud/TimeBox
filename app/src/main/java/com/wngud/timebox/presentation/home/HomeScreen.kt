package com.wngud.timebox.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import java.time.Duration // Duration import 추가
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.Check // 체크 아이콘을 위해 추가
import com.wngud.timebox.ui.theme.BorderGray
import com.wngud.timebox.ui.theme.DisabledGray
import com.wngud.timebox.ui.theme.EventBlueBg
import com.wngud.timebox.ui.theme.EventBlueBorder
import com.wngud.timebox.ui.theme.SuccessGreen
import com.wngud.timebox.ui.theme.TextSecondary
import androidx.hilt.navigation.compose.hiltViewModel
import com.wngud.timebox.ui.theme.SwitchBlue

val LocalDragTargetInfo = compositionLocalOf { DragAndDropState() }

class DragAndDropState {
    var isDragging by mutableStateOf(false)
    var draggedSlot by mutableStateOf<ScheduleSlot?>(null)
    var dragOffset by mutableStateOf(Offset.Zero)
    var dragStartingPoint by mutableStateOf(Offset.Zero)
    var currentCardCoordinates by mutableStateOf<LayoutCoordinates?>(null)
    var draggingSlotId by mutableStateOf<String?>(null)
    var currentDropTargetTime by mutableStateOf<LocalTime?>(null)

    fun startDrag(slot: ScheduleSlot, startOffset: Offset, cardCoords: LayoutCoordinates) {
        isDragging = true
        draggedSlot = slot
        dragStartingPoint = startOffset
        currentCardCoordinates = cardCoords
        draggingSlotId = slot.id
    }

    fun stopDrag() {
        isDragging = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToStats: () -> Unit,
    onNavigateToBrainDump: () -> Unit,
    onNavigateToSetting: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val dragAndDropState = remember { DragAndDropState() }
    val bigThreeTasks by viewModel.bigThreeTasks.collectAsState()
    val scheduleSlots by viewModel.scheduleSlots.collectAsState()
    val showBrainDumpSelector by viewModel.showBrainDumpSelector.collectAsState()
    val selectedTimeSlot by viewModel.selectedTimeSlot.collectAsState()
    val availableBrainDumpItems by viewModel.availableBrainDumpItems.collectAsState()

    val onSlotMove: (ScheduleSlot, LocalTime) -> Unit = { slot, newTime ->
        viewModel.moveScheduleSlot(slot, newTime)
        // 드롭 완료 후 상태 초기화
        dragAndDropState.draggedSlot = null
        dragAndDropState.dragOffset = Offset.Zero
        dragAndDropState.dragStartingPoint = Offset.Zero
        dragAndDropState.currentCardCoordinates = null
        dragAndDropState.draggingSlotId = null
        dragAndDropState.currentDropTargetTime = null
    }

    val onTaskCheckChanged: (Task, Boolean) -> Unit = { task, _ ->
        viewModel.toggleTaskCompletion(task.id)
    }

    CompositionLocalProvider(LocalDragTargetInfo provides dragAndDropState) {
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
                stats = DailyStats("3.5h", 5, 8, 85, 12), // Fake Data
                tasks = bigThreeTasks,
                scheduleSlots = scheduleSlots,
                onNavigateToStats = onNavigateToStats,
                userName = "사용자",
                dragAndDropState = dragAndDropState,
                onSlotMove = onSlotMove,
                onTaskCheckChanged = onTaskCheckChanged,
                onTimeSlotClick = viewModel::onTimeSlotClick
            )

            // Floating dragged item visual
            if (dragAndDropState.isDragging && dragAndDropState.draggedSlot != null && dragAndDropState.currentCardCoordinates != null) {
                val draggedSlot = dragAndDropState.draggedSlot!!
                val initialCardOffset = dragAndDropState.currentCardCoordinates!!.positionInRoot()
                val density = LocalDensity.current

                val floatingOffset = initialCardOffset + dragAndDropState.dragOffset - dragAndDropState.dragStartingPoint

                Box(modifier = Modifier
                    .offset { IntOffset(floatingOffset.x.roundToInt(), floatingOffset.y.roundToInt()) }
                    .width(with(density) { dragAndDropState.currentCardCoordinates!!.size.width.toDp() })
                    .zIndex(1f)
                ) {
                    TimelineSlotCard(draggedSlot, dragAndDropState, Modifier)
                }
            }
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
}

@Composable
fun TimeBoxerContent(
    modifier: Modifier = Modifier,
    stats: DailyStats,
    tasks: List<Task>,
    scheduleSlots: List<ScheduleSlot>,
    onNavigateToStats: () -> Unit,
    userName: String,
    dragAndDropState: DragAndDropState,
    onSlotMove: (ScheduleSlot, LocalTime) -> Unit,
    onTaskCheckChanged: (Task, Boolean) -> Unit,
    onTimeSlotClick: (LocalTime) -> Unit
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
            dragAndDropState = dragAndDropState,
            onSlotMove = onSlotMove,
            onTimeSlotClick = onTimeSlotClick
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
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(EventBlueBg),
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
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (task.isCompleted) SwitchBlue // 체크 시 파랑
                        else BorderGray // 미체크 시 회색
                    )
                    .clickable { onToggleComplete(task, !task.isCompleted) }, // 클릭 시 상태 토글
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
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

@Composable
fun TimelineSlotCard(
    slot: ScheduleSlot,
    dragAndDropState: DragAndDropState,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (slot.colorType) {
        EventColorType.BLUE -> EventBlueBg
        EventColorType.GREEN -> Color(0xFFE8F5E9)
        EventColorType.GRAY -> Color(0xFFFAFAFA)
    }

    val borderColor = when (slot.colorType) {
        EventColorType.BLUE -> EventBlueBorder
        else -> Color.Transparent
    }

    var cardLayoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (dragAndDropState.isDragging && dragAndDropState.draggingSlotId == slot.id) 0f else 1f)
            .onGloballyPositioned { coordinates ->
                cardLayoutCoordinates = coordinates
            },
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
                .padding(16.dp)
                .pointerInput(slot.id) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            cardLayoutCoordinates?.let { coords ->
                                dragAndDropState.startDrag(slot, offset, coords)
                            }
                        },
                        onDragEnd = {
                            dragAndDropState.stopDrag()
                        },
                        onDragCancel = {
                            dragAndDropState.stopDrag()
                        },
                        onDrag = { change, dragAmount ->
                            dragAndDropState.dragOffset += dragAmount
                        }
                    )
                },
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
}

@Composable
fun HourTimelineRow(
    hour: Int,
    slotsForThisHour: List<ScheduleSlot>,
    dragAndDropState: DragAndDropState,
    onTimeSlotClick: (LocalTime) -> Unit
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
                dragAndDropState = dragAndDropState
            )

            // 30분 슬롯
            TimeSlotRow(
                hour = hour,
                minute = 30,
                slots = slotsForThisHour.filter { it.startTime.minute == 30 },
                onTimeSlotClick = onTimeSlotClick,
                dragAndDropState = dragAndDropState
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
    dragAndDropState: DragAndDropState
) {
    if (slots.isNotEmpty()) {
        // 일정이 있는 경우
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slots.sortedBy { it.startTime }.forEach { slot ->
                TimelineSlotCard(
                    slot = slot,
                    dragAndDropState = dragAndDropState,
                    modifier = Modifier
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

// Updated TimelineSectionNew to display 00-23 hours and handle drops
@Composable
fun TimelineSectionNew(
    scheduleSlots: List<ScheduleSlot>,
    dragAndDropState: DragAndDropState,
    onSlotMove: (ScheduleSlot, LocalTime) -> Unit,
    onTimeSlotClick: (LocalTime) -> Unit
) {
    val slotsByHour = remember(scheduleSlots) {
        scheduleSlots.groupBy { it.startTime.hour }
    }

    val density = LocalDensity.current

    val hourRowBaseHeightDp = 28.dp // 30분 슬롯의 기본 높이
    val hourRowVerticalSpacingDp = 8.dp // 슬롯 간 간격
    val hourSlotTotalHeightPx = with(density) { ((hourRowBaseHeightDp * 2) + (hourRowVerticalSpacingDp * 2) + 12.dp).toPx() }

    var timelineRootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                timelineRootCoordinates = coordinates
            }
            .pointerInput(dragAndDropState.isDragging) {
                if (dragAndDropState.isDragging && dragAndDropState.draggedSlot != null) {
                    detectDragGestures(
                        onDragStart = { },
                        onDragEnd = {
                            val draggedSlot = dragAndDropState.draggedSlot
                            val targetTime = dragAndDropState.currentDropTargetTime
                            if (draggedSlot != null && targetTime != null) {
                                onSlotMove(draggedSlot, targetTime)
                            }
                            dragAndDropState.draggedSlot = null
                            dragAndDropState.dragOffset = Offset.Zero
                            dragAndDropState.dragStartingPoint = Offset.Zero
                            dragAndDropState.currentCardCoordinates = null
                            dragAndDropState.draggingSlotId = null
                            dragAndDropState.currentDropTargetTime = null
                            dragAndDropState.stopDrag()
                        },
                        onDragCancel = {
                            dragAndDropState.draggedSlot = null
                            dragAndDropState.dragOffset = Offset.Zero
                            dragAndDropState.dragStartingPoint = Offset.Zero
                            dragAndDropState.currentCardCoordinates = null
                            dragAndDropState.draggingSlotId = null
                            dragAndDropState.currentDropTargetTime = null
                            dragAndDropState.stopDrag()
                        },
                        onDrag = { change, _ ->
                            timelineRootCoordinates?.let { rootCoords ->
                                val currentPointerYInTimeline = change.position.y - rootCoords.positionInRoot().y

                                val totalTimelineMinutes = 24 * 60
                                val timelineVisualHeight = rootCoords.size.height.toFloat()
                                val relativeYFraction = (currentPointerYInTimeline / timelineVisualHeight).coerceIn(0f, 1f)

                                val rawMinuteInTimeline = (relativeYFraction * totalTimelineMinutes).roundToInt()
                                val snappedMinutes = ((rawMinuteInTimeline / 30) * 30).coerceIn(0, 23 * 60 + 30)

                                val newHour = snappedMinutes / 60
                                val newMinute = snappedMinutes % 60
                                dragAndDropState.currentDropTargetTime = LocalTime.of(newHour, newMinute)
                            }
                        }
                    )
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (hour in 0..23) {
                val slotsForThisHour = slotsByHour[hour] ?: emptyList()
                HourTimelineRow(
                    hour = hour,
                    slotsForThisHour = slotsForThisHour,
                    dragAndDropState = dragAndDropState,
                    onTimeSlotClick = onTimeSlotClick
                )
            }
        }

        // Drop target visual indicator
        if (dragAndDropState.isDragging && dragAndDropState.currentDropTargetTime != null && timelineRootCoordinates != null) {
            val targetTime = dragAndDropState.currentDropTargetTime!!
            val targetHour = targetTime.hour
            val targetMinute = targetTime.minute

            val yOffsetForIndicatorPx = (targetHour + targetMinute / 60f) * hourSlotTotalHeightPx

            Spacer(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = with(density) { (90.dp + 12.dp).roundToPx() },
                            y = yOffsetForIndicatorPx.roundToInt()
                        )
                    }
                    .width(with(density) { (timelineRootCoordinates!!.size.width.toDp() - (90.dp + 12.dp)) })
                    .height(28.dp)
                    .alpha(0.5f)
                    .background(Color.Blue.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
                    .zIndex(0.5f)
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