package com.wngud.timebox.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import java.time.LocalDate
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.luminance
import com.wngud.timebox.ui.theme.*
import com.wngud.timebox.util.toKoreanDateString
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 드래그앤드롭 상태 관리 클래스
 * @Stable 어노테이션으로 Compose 재구성 최적화
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
    
    /**
     * 드래그 시작
     */
    fun startDrag(slot: ScheduleSlot, coords: LayoutCoordinates) {
        isDragging = true
        draggedSlot = slot
        draggedSlotOriginalCoords = coords
        dragOffsetY = 0f
        currentTargetTime = slot.startTime
    }
    
    /**
     * 드래그 오프셋 업데이트 (Y축만)
     */
    fun updateDragOffset(offsetY: Float) {
        dragOffsetY = offsetY
    }
    
    /**
     * 타겟 시간 업데이트
     */
    fun updateTargetTime(time: LocalTime) {
        currentTargetTime = time
    }
    
    /**
     * 드래그 종료 및 상태 초기화
     */
    fun stopDrag() {
        isDragging = false
        draggedSlot = null
        dragOffsetY = 0f
        currentTargetTime = null
        draggedSlotOriginalCoords = null
    }
}

/**
 * CompositionLocal로 DragAndDropState 공유
 */
val LocalDragAndDropState = compositionLocalOf { DragAndDropState() }

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
    
    // 드래그앤드롭 상태
    val dragState = remember { DragAndDropState() }

    val onTaskCheckChanged: (Task, Boolean) -> Unit = { task, _ ->
        viewModel.toggleTaskCompletion(task.id)
    }
    
    // 드래그 종료 시 ViewModel 호출
    val onDragEnd: (ScheduleSlot?, LocalTime?) -> Unit = { slot, targetTime ->
        if (slot != null && targetTime != null && slot.startTime != targetTime) {
            viewModel.moveScheduleSlot(slot, targetTime)
        }
    }

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
                    onTimeSlotClick = viewModel::onTimeSlotClick,
                    onSlotLongClick = viewModel::removeScheduleSlot,
                    onDragEnd = onDragEnd
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
            onSlotLongClick = onSlotLongClick,
            onDragEnd = onDragEnd
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun DailySummaryCardNew(
    stats: DailyStats,
    onNavigateToStats: () -> Unit
) {
    val currentDate = remember { LocalDate.now() }
    val dateText = remember(currentDate) { currentDate.toKoreanDateString() }
    
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
                    text = dateText,
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
    onSlotLongClick: (String) -> Unit = {},
    onDragEnd: (ScheduleSlot?, LocalTime?) -> Unit = { _, _ -> }
) {
    val dragState = LocalDragAndDropState.current
    var cardCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    
    val isBeingDragged = dragState.isDragging && dragState.draggedSlot?.id == slot.id
    
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isBeingDragged) 0.3f else 1f)
            .onGloballyPositioned { cardCoords = it }
            .pointerInput(slot.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { 
                        cardCoords?.let { coords ->
                            dragState.startDrag(slot, coords)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        // Y축만 적용 (상하 드래그만)
                        dragState.updateDragOffset(
                            dragState.dragOffsetY + dragAmount.y
                        )
                        change.consume()
                    },
                    onDragEnd = {
                        onDragEnd(dragState.draggedSlot, dragState.currentTargetTime)
                        dragState.stopDrag()
                    },
                    onDragCancel = {
                        dragState.stopDrag()
                    }
                )
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
}

@Composable
fun HourTimelineRow(
    hour: Int,
    slotsForThisHour: List<ScheduleSlot>,
    onTimeSlotClick: (LocalTime) -> Unit,
    onSlotLongClick: (String) -> Unit,
    onDragEnd: (ScheduleSlot?, LocalTime?) -> Unit = { _, _ -> }
) {
    val dragState = LocalDragAndDropState.current
    val targetTime = dragState.currentTargetTime
    
    // 이 시간이 드래그 타겟인지 확인
    val isTargetHour = dragState.isDragging && targetTime?.hour == hour
    val isTargetAt00 = isTargetHour && targetTime?.minute == 0
    
    // 부드러운 색상 전환 애니메이션
    val textColor by animateColorAsState(
        targetValue = if (isTargetAt00) {
            MaterialTheme.colorScheme.primary
        } else {
            DisabledGray
        },
        animationSpec = tween(durationMillis = 200),
        label = "textColor"
    )
    
    // 부드러운 투명도 전환
    val textAlpha by animateFloatAsState(
        targetValue = if (isTargetAt00) 1f else 0.6f,
        animationSpec = tween(durationMillis = 200),
        label = "textAlpha"
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Time label with highlighting
        val formattedTime = when (hour) {
            0 -> "오전 12시"
            in 1..11 -> "오전 ${hour}시"
            12 -> "오후 12시"
            in 13..23 -> "오후 ${hour - 12}시"
            else -> ""
        }
        
        // Box로 감싸서 레이아웃 고정
        Box(
            modifier = Modifier
                .width(90.dp)
                .wrapContentHeight(),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold // 항상 SemiBold로 고정하여 레이아웃 변화 방지
                ),
                color = textColor,
                modifier = Modifier.alpha(textAlpha)
            )
        }

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
                onSlotLongClick = onSlotLongClick,
                onDragEnd = onDragEnd
            )

            // 30분 슬롯
            TimeSlotRow(
                hour = hour,
                minute = 30,
                slots = slotsForThisHour.filter { it.startTime.minute == 30 },
                onTimeSlotClick = onTimeSlotClick,
                onSlotLongClick = onSlotLongClick,
                onDragEnd = onDragEnd
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
    onSlotLongClick: (String) -> Unit,
    onDragEnd: (ScheduleSlot?, LocalTime?) -> Unit = { _, _ -> }
) {
    if (slots.isNotEmpty()) {
        // 일정이 있는 경우
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slots.sortedBy { it.startTime }.forEach { slot ->
                TimelineSlotCard(
                    slot = slot,
                    modifier = Modifier,
                    onSlotLongClick = onSlotLongClick,
                    onDragEnd = onDragEnd
                )
            }
        }
    } else {
        // 빈 슬롯 - 클릭 가능한 영역 (카드 높이와 동일하게)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // 카드 내용 높이와 동일하게 (padding 16dp * 2 + 텍스트 약 24dp)
                .clickable {
                    onTimeSlotClick(LocalTime.of(hour, minute))
                }
                .background(Color.Transparent)
        )
    }
}

// Updated TimelineSectionNew to display 00-23 hours with drag support
@Composable
fun TimelineSectionNew(
    scheduleSlots: List<ScheduleSlot>,
    onTimeSlotClick: (LocalTime) -> Unit,
    onSlotLongClick: (String) -> Unit,
    onDragEnd: (ScheduleSlot?, LocalTime?) -> Unit = { _, _ -> }
) {
    val dragState = LocalDragAndDropState.current
    val density = LocalDensity.current
    var timelineCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val currentTime by rememberUpdatedState(LocalTime.now())
    
    val slotsByHour = remember(scheduleSlots) {
        scheduleSlots.groupBy { it.startTime.hour }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { timelineCoords = it }
            .pointerInput(dragState.isDragging) {
                if (dragState.isDragging) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val position = event.changes.first().position
                            
                            timelineCoords?.let { coords ->
                                val relativeY = position.y
                                val totalHeight = coords.size.height.toFloat()
                                
                                // Y 좌표를 시간으로 변환
                                // 24시간 * 60분 = 1440분
                                val totalMinutes = 24 * 60
                                val minuteInDay = (relativeY / totalHeight * totalMinutes)
                                    .roundToInt()
                                    .coerceIn(0, 24 * 60 - 1)
                                
                                // 30분 단위로 스냅
                                val snappedMinutes = (minuteInDay / 30) * 30
                                val hour = snappedMinutes / 60
                                val minute = snappedMinutes % 60
                                
                                dragState.updateTargetTime(LocalTime.of(hour, minute))
                            }
                        }
                    }
                }
            }
    ) {
        // 현재 시간 인디케이터 (먼저 그려서 뒤에 배치)
        timelineCoords?.let { coords ->
            CurrentTimeIndicator(
                currentTime = currentTime,
                timelineHeight = coords.size.height
            )
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
                    onSlotLongClick = onSlotLongClick,
                    onDragEnd = onDragEnd
                )
            }
        }
        
        // 30분 레이블 오버레이 (해당 시간대 정가운데 고정)
        if (dragState.isDragging && dragState.currentTargetTime != null) {
            val targetTime = dragState.currentTargetTime!!
            
            if (targetTime.minute == 30) {
                // 부드러운 페이드 인 애니메이션
                val labelAlpha by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 200),
                    label = "labelAlpha"
                )
                
                // 해당 시간대의 정가운데 위치 계산
                val hour = targetTime.hour
                // 각 HourTimelineRow의 대략적인 높이와 간격
                val rowHeight = 152.dp // HourTimelineRow 높이 (64dp * 2 슬롯 + 8dp 간격 + 12dp 행간격)
                val yPosition = (hour * rowHeight.value + rowHeight.value / 2).dp
                
                Box(
                    modifier = Modifier
                        .offset(y = yPosition)
                        .width(90.dp)
                        .padding(start = 8.dp)
                        .alpha(labelAlpha)
                ) {
                    Text(
                        text = "${targetTime.hour}:30",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * 현재 시간을 나타내는 인디케이터
 * 빨간색 가로선과 시간 레이블로 구성
 */
@Composable
fun CurrentTimeIndicator(
    currentTime: LocalTime,
    timelineHeight: Int
) {
    if (timelineHeight == 0) return
    
    // 현재 시간을 Y 위치로 변환 (성능 최적화: remember 사용)
    val yPosition = remember(currentTime, timelineHeight) {
        val totalMinutes = 24 * 60
        val currentMinutes = currentTime.hour * 60 + currentTime.minute
        (currentMinutes.toFloat() / totalMinutes) * timelineHeight
    }
    
    // 테마에 맞는 색상 사용 (라이트/다크 모드 대응)
    val indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    
    Row(
        modifier = Modifier
            .offset(y = with(LocalDensity.current) { yPosition.toDp() })
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 시간 레이블 (Primary 색상 배경)
        Box(
            modifier = Modifier
                .width(90.dp)
                .background(
                    color = indicatorColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%d:%02d", currentTime.hour, currentTime.minute),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Primary 색상 가로선
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(indicatorColor)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    TimeBoxTheme {
        HomeScreen({}, {}, {})
    }
}