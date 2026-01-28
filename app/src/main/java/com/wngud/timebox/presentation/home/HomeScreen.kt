package com.wngud.timebox.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
import java.time.LocalDate
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.luminance
import com.wngud.timebox.ui.theme.*
import com.wngud.timebox.util.toKoreanDateString
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 드래그앤드롭 상태 관리 클래스
 */
@Stable
class DragAndDropState {
    var isDragging by mutableStateOf(false)
        private set
    
    var draggedSlot by mutableStateOf<ScheduleSlot?>(null)
        private set
    
    var dragOffsetY by mutableFloatStateOf(0f)
        private set
    
    var currentTargetTime by mutableStateOf<LocalTime?>(null)
        private set
    
    var draggedSlotOriginalCoords by mutableStateOf<LayoutCoordinates?>(null)
        private set
    
    fun startDrag(slot: ScheduleSlot, coords: LayoutCoordinates) {
        isDragging = true
        draggedSlot = slot
        draggedSlotOriginalCoords = coords
        dragOffsetY = 0f
        currentTargetTime = slot.startTime
    }
    
    fun updateDragOffset(offsetY: Float) {
        dragOffsetY = offsetY
    }
    
    fun updateTargetTime(time: LocalTime) {
        currentTargetTime = time
    }
    
    fun stopDrag() {
        isDragging = false
        draggedSlot = null
        dragOffsetY = 0f
        currentTargetTime = null
        draggedSlotOriginalCoords = null
    }
}

val LocalDragAndDropState = compositionLocalOf { DragAndDropState() }

/**
 * [Stateful] 홈 화면의 Route 컴포저블
 */
@Composable
fun HomeRoute(
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

    HomeScreen(
        bigThreeTasks = bigThreeTasks,
        scheduleSlots = scheduleSlots,
        showBrainDumpSelector = showBrainDumpSelector,
        selectedTimeSlot = selectedTimeSlot,
        availableBrainDumpItems = availableBrainDumpItems,
        onNavigateToStats = onNavigateToStats,
        onNavigateToBrainDump = onNavigateToBrainDump,
        onNavigateToSetting = onNavigateToSetting,
        onTaskCheckChanged = { task, _ -> viewModel.toggleTaskCompletion(task.id) },
        onTimeSlotClick = viewModel::onTimeSlotClick,
        onSlotLongClick = viewModel::removeScheduleSlot,
        onMoveScheduleSlot = viewModel::moveScheduleSlot,
        onPlaceBrainDumpItem = viewModel::placeBrainDumpItem,
        onAddDirectScheduleSlot = viewModel::addDirectScheduleSlot,
        onDismissBrainDumpSelector = viewModel::dismissBrainDumpSelector
    )
}

/**
 * [Stateless] 홈 화면 UI 컴포저블
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    bigThreeTasks: List<Task>,
    scheduleSlots: List<ScheduleSlot>,
    showBrainDumpSelector: Boolean,
    selectedTimeSlot: Pair<LocalTime, LocalTime>?,
    availableBrainDumpItems: List<com.wngud.timebox.presentation.brainDump.BrainDumpItem>,
    onNavigateToStats: () -> Unit,
    onNavigateToBrainDump: () -> Unit,
    onNavigateToSetting: () -> Unit,
    onTaskCheckChanged: (Task, Boolean) -> Unit,
    onTimeSlotClick: (LocalTime) -> Unit,
    onSlotLongClick: (String) -> Unit,
    onMoveScheduleSlot: (ScheduleSlot, LocalTime) -> Unit,
    onPlaceBrainDumpItem: (com.wngud.timebox.presentation.brainDump.BrainDumpItem, LocalTime) -> Unit,
    onAddDirectScheduleSlot: (String, LocalTime) -> Unit,
    onDismissBrainDumpSelector: () -> Unit
) {
    val dragState = remember { DragAndDropState() }

    CompositionLocalProvider(LocalDragAndDropState provides dragState) {
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
            Box {
                TimeBoxerContent(
                    modifier = Modifier.padding(innerPadding),
                    stats = DailyStats("3.5h", 5, 8, 85, 12),
                    tasks = bigThreeTasks,
                    scheduleSlots = scheduleSlots,
                    onNavigateToStats = onNavigateToStats,
                    userName = "사용자",
                    onTaskCheckChanged = onTaskCheckChanged,
                    onTimeSlotClick = onTimeSlotClick,
                    onSlotLongClick = onSlotLongClick,
                    onDragEnd = { slot, targetTime ->
                        if (slot != null && targetTime != null && slot.startTime != targetTime) {
                            onMoveScheduleSlot(slot, targetTime)
                        }
                    }
                )
                
                // 플로팅 드래그 카드
                if (dragState.isDragging && 
                    dragState.draggedSlot != null && 
                    dragState.draggedSlotOriginalCoords != null) {
                    
                    val slot = dragState.draggedSlot!!
                    val coords = dragState.draggedSlotOriginalCoords!!
                    val position = coords.positionInRoot()
                    
                    Box(
                        modifier = Modifier
                            .offset { 
                                IntOffset(
                                    position.x.roundToInt(), 
                                    (position.y + dragState.dragOffsetY).roundToInt()
                                ) 
                            }
                            .width(with(LocalDensity.current) { coords.size.width.toDp() })
                            .alpha(0.8f)
                            .zIndex(100f)
                    ) {
                        TimelineSlotCard(
                            slot = slot,
                            onSlotLongClick = {},
                            onDragEnd = { _, _ -> }
                        )
                    }
                }
            }
        }
        
        if (showBrainDumpSelector && selectedTimeSlot != null) {
            BrainDumpItemSelectorBottomSheet(
                items = availableBrainDumpItems,
                selectedTimeSlot = selectedTimeSlot,
                onItemSelected = { item ->
                    onPlaceBrainDumpItem(item, selectedTimeSlot.first)
                },
                onDirectEntry = { title ->
                    onAddDirectScheduleSlot(title, selectedTimeSlot.first)
                },
                onDismiss = onDismissBrainDumpSelector
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
    onTaskCheckChanged: (Task, Boolean) -> Unit,
    onTimeSlotClick: (LocalTime) -> Unit,
    onSlotLongClick: (String) -> Unit,
    onDragEnd: (ScheduleSlot?, LocalTime?) -> Unit = { _, _ -> }
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        DailySummaryCardNew(stats, onNavigateToStats)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "안녕하세요, $userName!",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "AI가 추천하는 오늘의 Big Three입니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = DisabledGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        BigThreeSectionNew(tasks, onTaskCheckChanged)
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "오늘의 타임라인",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        TimelineSectionNew(
            scheduleSlots = scheduleSlots,
            onTimeSlotClick = onTimeSlotClick,
            onSlotLongClick = onSlotLongClick,
            onDragEnd = onDragEnd
        )
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun DailySummaryCardNew(stats: DailyStats, onNavigateToStats: () -> Unit) {
    val currentDate = remember { LocalDate.now() }
    val dateText = remember(currentDate) { currentDate.toKoreanDateString() }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onNavigateToStats() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = dateText, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                Text(text = "자세히 보기 >", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatsItem(icon = "✓", label = "완료된 작업", value = "5/8")
                Spacer(modifier = Modifier.width(24.dp))
                StatsItem(icon = "⏳", label = "집중 시간", value = stats.focusTime)
            }
        }
    }
}

@Composable
fun StatsItem(icon: String, label: String, value: String) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(if (isDarkTheme) Color(0xFF1E3A5F) else EventBlueBg), contentAlignment = Alignment.Center) {
            Text(text = icon, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = DisabledGray)
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun BigThreeSectionNew(tasks: List<Task>, onTaskCheckChanged: (Task, Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        tasks.forEach { task -> BigThreeTaskItem(task, onTaskCheckChanged) }
    }
}

@Composable
fun BigThreeTaskItem(task: Task, onToggleComplete: (Task, Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
            Box(
                modifier = Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)).background(if (task.isCompleted) SwitchBlue else if (isDarkTheme) Color(0xFF424242) else BorderGray).clickable { onToggleComplete(task, !task.isCompleted) },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = if (task.isCompleted) Color.White else (if (isDarkTheme) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.4f)), modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = task.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineSlotCard(slot: ScheduleSlot, modifier: Modifier = Modifier, onSlotLongClick: (String) -> Unit = {}, onDragEnd: (ScheduleSlot?, LocalTime?) -> Unit = { _, _ -> }) {
    val dragState = LocalDragAndDropState.current
    var cardCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val isBeingDragged = dragState.isDragging && dragState.draggedSlot?.id == slot.id
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val backgroundColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else (when (slot.colorType) { EventColorType.BLUE -> SchedulePrimaryBg; EventColorType.GREEN -> ScheduleSecondaryBg; EventColorType.GRAY -> ScheduleTertiaryBg })
    val borderColor = when (slot.colorType) { EventColorType.BLUE -> if (isDarkTheme) SchedulePrimaryBorderDark else SchedulePrimaryBorder; EventColorType.GREEN -> if (isDarkTheme) ScheduleSecondaryBorderDark else ScheduleSecondaryBorder; else -> Color.Transparent }
    Card(
        modifier = modifier.fillMaxWidth().alpha(if (isBeingDragged) 0.3f else 1f).onGloballyPositioned { cardCoords = it }.pointerInput(slot.id) { detectDragGesturesAfterLongPress(onDragStart = { cardCoords?.let { coords -> dragState.startDrag(slot, coords) } }, onDrag = { change, dragAmount -> dragState.updateDragOffset(dragState.dragOffsetY + dragAmount.y); change.consume() }, onDragEnd = { onDragEnd(dragState.draggedSlot, dragState.currentTargetTime); dragState.stopDrag() }, onDragCancel = { dragState.stopDrag() }) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (borderColor != Color.Transparent) BorderStroke(2.dp, borderColor) else null
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = slot.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            if (slot.colorType == EventColorType.BLUE) Icon(imageVector = Icons.Outlined.Star, contentDescription = null, tint = EventBlueBorder, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun HourTimelineRow(hour: Int, slotsForThisHour: List<ScheduleSlot>, onTimeSlotClick: (LocalTime) -> Unit, onSlotLongClick: (String) -> Unit, onDragEnd: (ScheduleSlot?, LocalTime?) -> Unit = { _, _ -> }) {
    val dragState = LocalDragAndDropState.current
    val targetTime = dragState.currentTargetTime
    val isTargetHour = dragState.isDragging && targetTime?.hour == hour
    val isTargetAt00 = isTargetHour && targetTime?.minute == 0
    val textColor by animateColorAsState(targetValue = if (isTargetAt00) MaterialTheme.colorScheme.primary else DisabledGray, animationSpec = tween(200))
    val textAlpha by animateFloatAsState(targetValue = if (isTargetAt00) 1f else 0.6f, animationSpec = tween(200))
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        val formattedTime = when (hour) { 0 -> "오전 12시"; in 1..11 -> "오전 ${hour}시"; 12 -> "오후 12시"; in 13..23 -> "오후 ${hour - 12}시"; else -> "" }
        Box(modifier = Modifier.width(90.dp).wrapContentHeight(), contentAlignment = Alignment.CenterStart) {
            Text(text = formattedTime, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = textColor, modifier = Modifier.alpha(textAlpha))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TimeSlotRow(hour = hour, minute = 0, slots = slotsForThisHour.filter { it.startTime.minute == 0 }, onTimeSlotClick = onTimeSlotClick, onSlotLongClick = onSlotLongClick, onDragEnd = onDragEnd)
            TimeSlotRow(hour = hour, minute = 30, slots = slotsForThisHour.filter { it.startTime.minute == 30 }, onTimeSlotClick = onTimeSlotClick, onSlotLongClick = onSlotLongClick, onDragEnd = onDragEnd)
        }
    }
}

@Composable
fun TimeSlotRow(hour: Int, minute: Int, slots: List<ScheduleSlot>, onTimeSlotClick: (LocalTime) -> Unit, onSlotLongClick: (String) -> Unit, onDragEnd: (ScheduleSlot?, LocalTime?) -> Unit = { _, _ -> }) {
    if (slots.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slots.sortedBy { it.startTime }.forEach { slot -> TimelineSlotCard(slot = slot, onSlotLongClick = onSlotLongClick, onDragEnd = onDragEnd) }
        }
    } else {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp).clickable { onTimeSlotClick(LocalTime.of(hour, minute)) }.background(Color.Transparent))
    }
}

@Composable
fun TimelineSectionNew(scheduleSlots: List<ScheduleSlot>, onTimeSlotClick: (LocalTime) -> Unit, onSlotLongClick: (String) -> Unit, onDragEnd: (ScheduleSlot?, LocalTime?) -> Unit = { _, _ -> }) {
    val dragState = LocalDragAndDropState.current
    var timelineCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val currentTime by rememberUpdatedState(LocalTime.now())
    val slotsByHour = remember(scheduleSlots) { scheduleSlots.groupBy { it.startTime.hour } }
    Box(modifier = Modifier.fillMaxWidth().onGloballyPositioned { timelineCoords = it }.pointerInput(dragState.isDragging) { if (dragState.isDragging) awaitPointerEventScope { while (true) { val event = awaitPointerEvent(); timelineCoords?.let { coords -> val relativeY = event.changes.first().position.y; val minuteInDay = (relativeY / coords.size.height * 1440).roundToInt().coerceIn(0, 1439); val snappedMinutes = (minuteInDay / 30) * 30; dragState.updateTargetTime(LocalTime.of(snappedMinutes / 60, snappedMinutes % 60)) } } } }) {
        timelineCoords?.let { coords -> CurrentTimeIndicator(currentTime = currentTime, timelineHeight = coords.size.height) }
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) { for (hour in 0..23) HourTimelineRow(hour = hour, slotsForThisHour = slotsByHour[hour] ?: emptyList(), onTimeSlotClick = onTimeSlotClick, onSlotLongClick = onSlotLongClick, onDragEnd = onDragEnd) }
    }
}

@Composable
fun CurrentTimeIndicator(currentTime: LocalTime, timelineHeight: Int) {
    if (timelineHeight == 0) return
    val yPosition = remember(currentTime, timelineHeight) { ( (currentTime.hour * 60 + currentTime.minute).toFloat() / 1440 ) * timelineHeight }
    val indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    Row(modifier = Modifier.offset(y = with(LocalDensity.current) { yPosition.toDp() }).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(90.dp).background(color = indicatorColor, shape = RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 4.dp), contentAlignment = Alignment.Center) {
            Text(text = String.format("%d:%02d", currentTime.hour, currentTime.minute), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
        }
        Spacer(modifier = Modifier.width(12.dp)); Box(modifier = Modifier.weight(1f).height(2.dp).background(indicatorColor))
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    TimeBoxTheme {
        HomeScreen(
            bigThreeTasks = emptyList(),
            scheduleSlots = emptyList(),
            showBrainDumpSelector = false,
            selectedTimeSlot = null,
            availableBrainDumpItems = emptyList(),
            onNavigateToStats = {},
            onNavigateToBrainDump = {},
            onNavigateToSetting = {},
            onTaskCheckChanged = { _, _ -> },
            onTimeSlotClick = {},
            onSlotLongClick = {},
            onMoveScheduleSlot = { _, _ -> },
            onPlaceBrainDumpItem = { _, _ -> },
            onAddDirectScheduleSlot = { _, _ -> },
            onDismissBrainDumpSelector = {}
        )
    }
}
