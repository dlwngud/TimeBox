package com.wngud.timebox.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wngud.timebox.data.modal.DailyStats
import com.wngud.timebox.data.modal.EventColorType
import com.wngud.timebox.data.modal.ScheduleEvent
import com.wngud.timebox.data.modal.Task
import com.wngud.timebox.presentation.components.TimeBoxerTopBar
import com.wngud.timebox.ui.theme.TimeBoxTheme
import java.time.LocalTime

val LocalDragTargetInfo = compositionLocalOf { DragAndDropState() }

class DragAndDropState {
    var isDragging by mutableStateOf(false)
    var draggedEvent by mutableStateOf<ScheduleEvent?>(null)
    var dragOffset by mutableStateOf(Offset.Zero)
    var dragStartingPoint by mutableStateOf(Offset.Zero)
    var currentCardCoordinates by mutableStateOf<LayoutCoordinates?>(null)
    var draggingEventId by mutableStateOf<String?>(null)

    fun startDrag(event: ScheduleEvent, startOffset: Offset, cardCoords: LayoutCoordinates) {
        isDragging = true
        draggedEvent = event
        dragStartingPoint = startOffset
        currentCardCoordinates = cardCoords
        draggingEventId = event.id
    }

    fun stopDrag() {
        isDragging = false
        draggedEvent = null
        dragOffset = Offset.Zero
        dragStartingPoint = Offset.Zero
        currentCardCoordinates = null
        draggingEventId = null
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
                "빈 시간",
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                EventColorType.GREEN
            ),
            ScheduleEvent(
                "3",
                "팀 회의",
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                EventColorType.GREEN
            ),
            ScheduleEvent(
                "4",
                "점심 시간",
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                EventColorType.GREEN
            )
        )
    }
    val fakeStats = DailyStats("3.5h", 5, 8, 85, 12)
    val fakeTasks = listOf(
        Task("1", "기획안 초안 작성 완료하기", false),
        Task("2", "팀 회의록 정리", false),
        Task("3", "디자인 시스템 검토", false)
    )

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
                stats = fakeStats,
                tasks = fakeTasks,
                events = fakeEvents,
                onNavigateToStats = onNavigateToStats,
                userName = "사용자"
            )
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
    userName: String
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
        TimelineSectionNew(events)

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
fun TimelineSectionNew(events: List<ScheduleEvent>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        events.forEach { event ->
            TimelineEventItem(event)
        }
    }
}

@Composable
fun TimelineEventItem(event: ScheduleEvent) {
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${String.format("%02d", event.startTime.hour)}:${String.format("%02d", event.startTime.minute)} ${if (event.startTime.hour < 12) "AM" else "PM"}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF9E9E9E),
            modifier = Modifier.width(90.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            border = if (borderColor != Color.Transparent) {
                androidx.compose.foundation.BorderStroke(2.dp, borderColor)
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
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    TimeBoxTheme {
        HomeScreen({}, {}, {})
    }
}