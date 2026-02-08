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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Settings
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
import com.wngud.timebox.data.modal.EventColorType
import com.wngud.timebox.data.modal.ScheduleSlot
import com.wngud.timebox.data.modal.Task
import com.wngud.timebox.ui.theme.TimeBoxTheme
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalTime
import kotlin.math.roundToInt

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
 * [Stateless] 홈 화면 UI 컴포저블 - Big Three Dashboard (Dark Mode)
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
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToBrainDump,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
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
                BigThreeDashboardContent(
                    modifier = Modifier.padding(innerPadding),
                    tasks = bigThreeTasks,
                    scheduleSlots = scheduleSlots,
                    onNavigateToSetting = onNavigateToSetting,
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
                        TaskCard(
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

/**
 * Big Three Dashboard 메인 콘텐츠
 */
@Composable
fun BigThreeDashboardContent(
    modifier: Modifier = Modifier,
    tasks: List<Task>,
    scheduleSlots: List<ScheduleSlot>,
    onNavigateToSetting: () -> Unit,
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
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // 헤더
        DashboardHeader(onNavigateToSetting)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Big Three Priority 섹션
        Text(
            text = "오늘 꼭 해야할 일",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PriorityTasksSection(tasks, onTaskCheckChanged)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 오늘의 작업 섹션
        Text(
            text = "오늘의 작업",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TodayTasksSection(
            scheduleSlots = scheduleSlots,
            onTimeSlotClick = onTimeSlotClick,
            onSlotLongClick = onSlotLongClick,
            onDragEnd = onDragEnd
        )
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

/**
 * 대시보드 헤더
 */
@Composable
fun DashboardHeader(onNavigateToSetting: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "오늘의 Big Three",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(onClick = onNavigateToSetting) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Priority 작업 섹션 (Big Three)
 */
@Composable
fun PriorityTasksSection(tasks: List<Task>, onTaskCheckChanged: (Task, Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        tasks.take(3).forEachIndexed { index, task ->
            PriorityTaskCard(
                priority = index + 1,
                task = task,
                onToggleComplete = onTaskCheckChanged
            )
        }
    }
}

/**
 * Priority 작업 카드
 */
@Composable
fun PriorityTaskCard(
    priority: Int,
    task: Task,
    onToggleComplete: (Task, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority 번호
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$priority",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 작업 제목
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 체크박스
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (task.isCompleted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onToggleComplete(task, !task.isCompleted) },
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * 오늘의 작업 섹션
 */
@Composable
fun TodayTasksSection(
    scheduleSlots: List<ScheduleSlot>,
    onTimeSlotClick: (LocalTime) -> Unit,
    onSlotLongClick: (String) -> Unit,
    onDragEnd: (ScheduleSlot?, LocalTime?) -> Unit = { _, _ -> }
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        scheduleSlots.forEach { slot ->
            TaskCard(
                slot = slot,
                onSlotLongClick = onSlotLongClick,
                onDragEnd = onDragEnd
            )
        }
        
        // 빈 슬롯 추가 버튼
        if (scheduleSlots.isEmpty()) {
            EmptyTasksPlaceholder(onTimeSlotClick)
        }
    }
}

/**
 * 작업 카드
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskCard(
    slot: ScheduleSlot,
    modifier: Modifier = Modifier,
    onSlotLongClick: (String) -> Unit = {},
    onDragEnd: (ScheduleSlot?, LocalTime?) -> Unit = { _, _ -> }
) {
    val dragState = LocalDragAndDropState.current
    var cardCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val isBeingDragged = dragState.isDragging && dragState.draggedSlot?.id == slot.id
    
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
                        dragState.updateDragOffset(dragState.dragOffsetY + dragAmount.y)
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
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = slot.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${slot.startTime.hour}:${String.format("%02d", slot.startTime.minute)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 색상 인디케이터
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when (slot.colorType) {
                            EventColorType.BLUE -> MaterialTheme.colorScheme.primary
                            EventColorType.GREEN -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
            )
        }
    }
}

/**
 * 빈 작업 플레이스홀더
 */
@Composable
fun EmptyTasksPlaceholder(onTimeSlotClick: (LocalTime) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTimeSlotClick(LocalTime.now()) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "작업을 추가하려면 탭하세요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    TimeBoxTheme(darkTheme = true) {
        HomeScreen(
            bigThreeTasks = listOf(
                Task(id = "1", title = "프로젝트 기획서 작성", isCompleted = false),
                Task(id = "2", title = "팀 미팅 준비", isCompleted = true),
                Task(id = "3", title = "코드 리뷰", isCompleted = false)
            ),
            scheduleSlots = listOf(
                ScheduleSlot(
                    id = "1",
                    brainDumpItemId = 1L,
                    title = "아침 운동",
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(10, 0),
                    colorType = EventColorType.BLUE
                ),
                ScheduleSlot(
                    id = "2",
                    brainDumpItemId = 2L,
                    title = "독서 시간",
                    startTime = LocalTime.of(10, 30),
                    endTime = LocalTime.of(11, 30),
                    colorType = EventColorType.GREEN
                )
            ),
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
