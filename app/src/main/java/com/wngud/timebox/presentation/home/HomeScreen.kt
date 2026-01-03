package com.wngud.timebox.presentation.home

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import com.wngud.timebox.data.modal.ScheduleEvent
import com.wngud.timebox.data.modal.Task
import com.wngud.timebox.presentation.components.TimeBoxerTopBar
import com.wngud.timebox.ui.theme.TimeBoxTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.Duration // Duration import 추가
import kotlin.math.roundToInt

val LocalDragTargetInfo = compositionLocalOf { DragAndDropState() }

class DragAndDropState {
    var isDragging by mutableStateOf(false)
    var draggedEvent by mutableStateOf<ScheduleEvent?>(null)
    var dragOffset by mutableStateOf(Offset.Zero)
    var dragStartingPoint by mutableStateOf(Offset.Zero)
    var currentCardCoordinates by mutableStateOf<LayoutCoordinates?>(null)
    var draggingEventId by mutableStateOf<String?>(null)
    var currentDropTargetTime by mutableStateOf<LocalTime?>(null)

    fun startDrag(event: ScheduleEvent, startOffset: Offset, cardCoords: LayoutCoordinates) {
        isDragging = true
        draggedEvent = event
        dragStartingPoint = startOffset
        currentCardCoordinates = cardCoords
        draggingEventId = event.id
    }

    fun stopDrag() {
        isDragging = false
        // draggedEvent, dragOffset 등은 onEventMove에서 처리 후 null로 만드는 것이 좋음
        // (드롭 애니메이션 등을 위해 잠시 값을 유지할 수 있음)
        // draggingEventId = null
        // currentDropTargetTime = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToStats: () -> Unit,
    onNavigateToBrainDump: () -> Unit,
    onNavigateToSetting: () -> Unit
) {
    val dragAndDropState = remember { DragAndDropState() }

    // Fake Data
    val fakeEvents = remember {
        mutableStateListOf(
            ScheduleEvent(
                "1",
                "기획안 초안 작성",
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                EventColorType.BLUE
            ),
            ScheduleEvent(
                "2",
                "점심 시간",
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                EventColorType.GREEN
            ),
            ScheduleEvent(
                "3",
                "팀 회의",
                LocalTime.of(15, 30),
                LocalTime.of(16, 0),
                EventColorType.GREEN
            ),
            ScheduleEvent(
                "4",
                "개인 업무",
                LocalTime.of(18, 0),
                LocalTime.of(18, 30),
                EventColorType.GRAY
            )
        )
    }

    val onEventMove: (ScheduleEvent, LocalTime) -> Unit = { event, newTime ->
        val index = fakeEvents.indexOfFirst { it.id == event.id }
        if (index != -1) {
            val durationMinutes = Duration.between(event.startTime, event.endTime).toMinutes()
            val updatedEvent = event.copy(
                startTime = newTime,
                endTime = newTime.plusMinutes(durationMinutes)
            )
            fakeEvents[index] = updatedEvent
        }
        // 드롭 완료 후 상태 초기화
        dragAndDropState.draggedEvent = null
        dragAndDropState.dragOffset = Offset.Zero
        dragAndDropState.dragStartingPoint = Offset.Zero
        dragAndDropState.currentCardCoordinates = null
        dragAndDropState.draggingEventId = null
        dragAndDropState.currentDropTargetTime = null
    }

    CompositionLocalProvider(LocalDragTargetInfo provides dragAndDropState) {
        Scaffold(
            containerColor = Color(0xFFF5F7FA),
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
                tasks = listOf(
                    Task("1", "기획안 초안 작성 완료하기", false),
                    Task("2", "팀 회의록 정리", false),
                    Task("3", "디자인 시스템 검토", false)
                ), // Fake Data
                events = fakeEvents,
                onNavigateToStats = onNavigateToStats,
                userName = "사용자",
                dragAndDropState = dragAndDropState,
                onEventMove = onEventMove
            )

            // Floating dragged item visual - 여기로 이동하여 모든 UI 위에 렌더링되도록 함
            if (dragAndDropState.isDragging && dragAndDropState.draggedEvent != null && dragAndDropState.currentCardCoordinates != null) {
                val draggedEvent = dragAndDropState.draggedEvent!!
                val initialCardOffset = dragAndDropState.currentCardCoordinates!!.positionInRoot()
                val density = LocalDensity.current

                // Calculate offset relative to root for the floating card
                val floatingOffset = initialCardOffset + dragAndDropState.dragOffset - dragAndDropState.dragStartingPoint

                Box(modifier = Modifier
                    .offset { IntOffset(floatingOffset.x.roundToInt(), floatingOffset.y.roundToInt()) }
                    .width(with(density) { dragAndDropState.currentCardCoordinates!!.size.width.toDp() })
                    .zIndex(1f) // 가장 위에 렌더링되도록 Z-Index 설정
                ) {
                    TimelineEventCard(draggedEvent, dragAndDropState, Modifier)
                }
            }
        }
    }
}

@Composable
fun TimeBoxerContent(
    modifier: Modifier = Modifier,
    stats: DailyStats,
    tasks: List<Task>,
    events: List<ScheduleEvent>,
    onNavigateToStats: () -> Unit,
    userName: String,
    dragAndDropState: DragAndDropState,
    onEventMove: (ScheduleEvent, LocalTime) -> Unit
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
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "AI가 추천하는 오늘의 Big Three입니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF9E9E9E)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Big Three Tasks
        BigThreeSectionNew(tasks)

        Spacer(modifier = Modifier.height(28.dp))

        // Timeline Header
        Text(
            text = "오늘의 타임라인",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Timeline
        TimelineSectionNew(
            events = events,
            dragAndDropState = dragAndDropState,
            onEventMove = onEventMove
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
            containerColor = Color.White
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
                    color = Color(0xFF1A1A1A)
                )

                Text(
                    text = "자세히 보기 >",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
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
                .background(Color(0xFFE8F3FF)),
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
                color = Color(0xFF9E9E9E)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF1A1A1A)
            )
        }
    }
}

@Composable
fun BigThreeSectionNew(tasks: List<Task>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        tasks.forEach { task ->
            BigThreeTaskItem(task)
        }
    }
}

@Composable
fun BigThreeTaskItem(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
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
                        if (task.isCompleted) Color(0xFF4CAF50)
                        else Color(0xFFE0E0E0)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "AI 추천",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2196F3),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE3F2FD))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun TimelineEventCard(
    event: ScheduleEvent,
    dragAndDropState: DragAndDropState,
    modifier: Modifier = Modifier
) {
    val isEmptySlot = event.title == "빈 시간"
    val backgroundColor = when {
        isEmptySlot -> Color(0xFFFAFAFA)
        event.title.contains("기획") -> Color(0xFFE3F2FD)
        else -> Color.White
    }

    val borderColor = when {
        event.title.contains("기획") -> Color(0xFF2196F3)
        else -> Color.Transparent
    }

    // State to hold the LayoutCoordinates of this Card
    var cardLayoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (dragAndDropState.isDragging && dragAndDropState.draggingEventId == event.id) 0f else 1f)
            .onGloballyPositioned { coordinates -> // Capture the LayoutCoordinates of the Card
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
                .pointerInput(event.id) { // pointerInput은 Card 내부에 그대로 유지
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            cardLayoutCoordinates?.let { coords -> // 저장된 LayoutCoordinates 사용
                                dragAndDropState.startDrag(event, offset, coords)
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
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isEmptySlot) FontWeight.Normal else FontWeight.SemiBold
                    ),
                    color = if (isEmptySlot) Color(0xFF9E9E9E) else Color(0xFF1A1A1A)
                )

                if (!isEmptySlot && event.title.contains("기획")) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "핵심 기능 정의 및 와이어프레임",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                } else if (!isEmptySlot && event.title.contains("회의")) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "주간 진행 상황 공유",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            if (event.title.contains("기획")) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = "Favorite",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun HourTimelineRow(hour: Int, eventsStartingThisHour: List<ScheduleEvent>, dragAndDropState: DragAndDropState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time label (e.g., 오전 9시, 오후 5시)
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
            color = Color(0xFF9E9E9E),
            modifier = Modifier.width(90.dp) // Consistent width for time column
        )

        Spacer(modifier = Modifier.width(12.dp)) // Space between time and card

        // Event cards or empty placeholder. Use Column to stack multiple events for the same hour.
        if (eventsStartingThisHour.isNotEmpty()) {
            Column(
                modifier = Modifier.weight(1f), // Take remaining width
                verticalArrangement = Arrangement.spacedBy(8.dp) // Space between multiple events if any
            ) {
                // Sort events within the same hour by their start time (minute)
                eventsStartingThisHour
                    .sortedBy { it.startTime }
                    .forEach { event ->
                        TimelineEventCard(
                            event = event,
                            dragAndDropState = dragAndDropState,
                            modifier = Modifier
                        )
                    }
            }
        } else {
            // 일정이 없는 경우, 오른쪽 공간을 비움
            // 드래그앤드롭 계산의 일관성을 위해 빈 공간도 높이를 차지하도록 Spacer를 둡니다.
            Spacer(modifier = Modifier.weight(1f).height(56.dp))
        }
    }
}

// Updated TimelineSectionNew to display 00-23 hours and handle drops
@Composable
fun TimelineSectionNew(
    events: List<ScheduleEvent>,
    dragAndDropState: DragAndDropState,
    onEventMove: (ScheduleEvent, LocalTime) -> Unit
) {
    val eventsByHour = remember(events) { // remember the map to avoid re-computation on every recomposition
        events.groupBy { it.startTime.hour }
    }

    val density = LocalDensity.current

    val hourRowBaseHeightDp = 56.dp // TimelineEventCard의 기본 높이
    val hourRowVerticalSpacingDp = 12.dp // HourTimelineRow 간의 수직 간격
    // 한 시간 슬롯의 총 높이 (카드/스페이서 높이 + 다음 슬롯까지의 간격)
    val hourSlotTotalHeightPx = with(density) { (hourRowBaseHeightDp + hourRowVerticalSpacingDp).toPx() }

    var timelineRootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                timelineRootCoordinates = coordinates
            }
            .pointerInput(dragAndDropState.isDragging) { // 드래그 이벤트 감지 (드롭 타겟으로서)
                if (dragAndDropState.isDragging && dragAndDropState.draggedEvent != null) {
                    detectDragGestures(
                        onDragStart = { /* 드롭 타겟에서는 사용하지 않음 */ },
                        onDragEnd = {
                            val draggedEvent = dragAndDropState.draggedEvent
                            val targetTime = dragAndDropState.currentDropTargetTime
                            if (draggedEvent != null && targetTime != null) {
                                onEventMove(draggedEvent, targetTime)
                            }
                            dragAndDropState.stopDrag() // 드래그 상태 리셋
                        },
                        onDragCancel = {
                            dragAndDropState.stopDrag() // 드래그 상태 리셋
                        },
                        onDrag = { change, _ ->
                            timelineRootCoordinates?.let { rootCoords ->
                                val currentPointerYInTimeline = change.position.y - rootCoords.positionInRoot().y

                                // 포인터 Y 위치를 타임라인 전체 높이 비율로 변환하여 시간 계산
                                val totalTimelineMinutes = 24 * 60 // 00:00부터 23:30까지 가능하도록
                                val timelineVisualHeight = rootCoords.size.height.toFloat()
                                val relativeYFraction = (currentPointerYInTimeline / timelineVisualHeight).coerceIn(0f, 1f)

                                val rawMinuteInTimeline = (relativeYFraction * totalTimelineMinutes).roundToInt()

                                // 30분 단위로 스냅 및 시간 범위 클램프 (00:00 ~ 23:30)
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
        // 실제 타임라인 항목들이 렌더링되는 Column
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(hourRowVerticalSpacingDp)
        ) {
            for (hour in 0..23) { // 00시부터 23시까지 모든 시간 순회
                val eventsForThisHour = eventsByHour[hour] ?: emptyList()
                HourTimelineRow(hour = hour, eventsStartingThisHour = eventsForThisHour, dragAndDropState = dragAndDropState)
            }
        }

        // 드롭 타겟 시각적 표시 (오버레이)
        if (dragAndDropState.isDragging && dragAndDropState.currentDropTargetTime != null && timelineRootCoordinates != null) {
            val targetTime = dragAndDropState.currentDropTargetTime!!
            val targetHour = targetTime.hour
            val targetMinute = targetTime.minute

            // 드롭 타겟 인디케이터의 Y 위치 계산 (타임라인 시작점으로부터의 오프셋)
            // (시간 + 분/60.0) * 시간당 높이 (여기서는 HourTimelineRow의 총 높이를 기준으로 함)
            val yOffsetForIndicatorPx = (targetHour + targetMinute / 60f) * hourSlotTotalHeightPx

            Spacer(
                modifier = Modifier
                    .offset { // offset Modifier는 IntOffset 람다를 받습니다.
                        IntOffset(
                            x = with(density) { (90.dp + 12.dp).roundToPx() }, // 시간 라벨과 간격 너비만큼 X 오프셋
                            y = yOffsetForIndicatorPx.roundToInt() // 계산된 Y 오프셋 적용
                        )
                    }
                    .width(with(density) { (timelineRootCoordinates!!.size.width.toDp() - (90.dp + 12.dp)) }) // 카드와 비슷한 너비
                    .height(hourRowBaseHeightDp) // 30분 슬롯에 맞게 높이 조정 (현재는 카드 높이와 동일하게)
                    .alpha(0.5f)
                    .background(Color.Blue.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp)) // 반투명 파란색 배경
                    .zIndex(0.5f) // 타임라인 콘텐츠 위에, 드래그되는 플로팅 카드 아래에 렌더링
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