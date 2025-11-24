package com.wngud.timebox.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wngud.timebox.data.modal.DailyStats
import com.wngud.timebox.data.modal.EventColorType
import com.wngud.timebox.data.modal.ScheduleEvent
import com.wngud.timebox.data.modal.Task
import com.wngud.timebox.presentation.components.BigThreeSection
import com.wngud.timebox.presentation.components.DailySummaryCard
import com.wngud.timebox.presentation.components.TimeBoxerTopBar
import com.wngud.timebox.presentation.components.TimelineSection
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
    val fakeEvents = remember { mutableStateListOf(
        ScheduleEvent("1", "프로젝트 기획", LocalTime.of(9, 0), LocalTime.of(11, 0), EventColorType.GREEN),
        ScheduleEvent("2", "디자인 리뷰", LocalTime.of(11, 0), LocalTime.of(12, 0), EventColorType.BLUE),
        ScheduleEvent("3", "점심 식사", LocalTime.of(12, 0), LocalTime.of(13, 0), EventColorType.GREEN)
    )}
    val fakeStats = DailyStats("6h 42m", 3, 3, 85, 12)
    val fakeTasks = listOf(
        Task("1", "프로젝트 기획서 초안 완료", true),
        Task("2", "디자인 팀 피드백 회의", true),
        Task("3", "다음 주 개발 계획 수립", true)
    )

    CompositionLocalProvider(LocalDragTargetInfo provides dragAndDropState) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background, // Theme Color 사용
            topBar = { TimeBoxerTopBar() },
            floatingActionButton = {
                // FAB를 다크모드 토글 버튼으로 사용
                FloatingActionButton(
                    onClick = onNavigateToBrainDump,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    // 아이콘 변경
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Toggle Theme"
                    )
                }
            }
        ) { innerPadding ->
            TimeBoxerContent(
                modifier = Modifier.padding(innerPadding),
                stats = fakeStats,
                tasks = fakeTasks,
                events = fakeEvents,
                onEventDropped = { eventId, newStartTime, newEndTime ->
                    val index = fakeEvents.indexOfFirst { it.id == eventId }
                    if (index != -1) {
                        fakeEvents[index] = fakeEvents[index].copy(startTime = newStartTime, endTime = newEndTime)
                    }
                },
                userName = "이아하"
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
    onEventDropped: (String, LocalTime, LocalTime) -> Unit,
    userName: String
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        DailySummaryCard(stats, {})
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "$userName, TimeBoxer세요!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary // Theme Color
        )

        Spacer(modifier = Modifier.height(12.dp))
        BigThreeSection(tasks)
        Spacer(modifier = Modifier.height(24.dp))
        TimelineSection(events)
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    TimeBoxTheme {
        HomeScreen({},{},{})
    }
}