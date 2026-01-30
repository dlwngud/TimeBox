package com.wngud.timebox.presentation.onBoarding

import androidx.activity.compose.BackHandler
import android.content.ClipData
import android.content.ClipDescription
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.wngud.timebox.ui.theme.*
import kotlinx.coroutines.launch


/**
 * [Stateful] 온보딩 화면의 Route 컴포저블.
 * ViewModel과의 의존성을 가지고 상태를 UI 전용 컴포저블에 전달합니다.
 */
@Composable
fun OnBoardingRoute(
    onComplete: () -> Unit,
    viewModel: OnBoardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    OnBoardingScreen(
        uiState = uiState,
        onIntent = { viewModel.processIntent(it) },
        getPhase1Items = { viewModel.getPhase1Items() },
        getCombinedItemsForPhase2 = { viewModel.getCombinedItemsForPhase2() },
        onComplete = onComplete
    )
}

/**
 * [Stateless] 온보딩 UI 렌더링을 담당하는 컴포저블.
 * ViewModel 의존성 없이 상태(State)와 이벤트(Intent)만 전달받아 프리뷰와 테스트가 용이합니다.
 */
@Composable
fun OnBoardingScreen(
    uiState: OnBoardingUiState,
    onIntent: (OnBoardingIntent) -> Unit,
    getPhase1Items: () -> List<OnBoardingItem>,
    getCombinedItemsForPhase2: () -> List<OnBoardingItem>,
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })

    // ViewModel의 currentPage 변경을 감지하여 pagerState 동기화
    LaunchedEffect(uiState.currentPage) {
        if (pagerState.currentPage != uiState.currentPage) {
            pagerState.animateScrollToPage(uiState.currentPage)
        }
    }

    // 시스템 뒤로가기 처리 - ViewModel에 Intent 발행
    BackHandler(enabled = uiState.currentPage > 0) {
        onIntent(OnBoardingIntent.NavigateToPreviousPage)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> Phase1DumpScreen(
                    userInputItems = getPhase1Items(),
                    sampleItems = uiState.sampleItems,
                    inputText = uiState.userInputText,
                    canProceed = uiState.canProceedToPhase2,
                    onInputChange = { onIntent(OnBoardingIntent.UpdateInputText(it)) },
                    onAddItem = { onIntent(OnBoardingIntent.AddUserItem) },
                    onNext = { onIntent(OnBoardingIntent.NavigateToNextPage) }
                )
                1 -> Phase2SelectScreen(
                    items = getCombinedItemsForPhase2(),
                    selectedItemIds = uiState.selectedBigThree,
                    onToggleSelection = { onIntent(OnBoardingIntent.ToggleBigThree(it)) },
                    onNext = { onIntent(OnBoardingIntent.NavigateToNextPage) },
                    onBack = { onIntent(OnBoardingIntent.NavigateToPreviousPage) }
                )
                2 -> Phase3BoxScreen(
                    selectedItems = getCombinedItemsForPhase2()
                        .filter { uiState.selectedBigThree.contains(it.id) },
                    onComplete = onComplete,
                    onBack = { onIntent(OnBoardingIntent.NavigateToPreviousPage) }
                )
            }
        }

        // 페이지 인디케이터
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { index ->
                val width by animateDpAsState(
                    targetValue = if (index == pagerState.currentPage) 32.dp else 8.dp,
                    animationSpec = tween(300)
                )
                Box(
                    modifier = Modifier
                        .width(width)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (index == pagerState.currentPage) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                )
            }
        }
    }
}

// ============ Phase 1: Dump ============
@Composable
fun Phase1DumpScreen(
    userInputItems: List<OnBoardingItem>,
    sampleItems: List<OnBoardingItem>,
    inputText: String,
    canProceed: Boolean,
    onInputChange: (String) -> Unit,
    onAddItem: () -> Unit,
    onNext: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val allItems = remember(userInputItems, sampleItems) {
        (userInputItems + sampleItems).sortedByDescending { it.id }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(40.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
                onClick = onNext,
                enabled = canProceed,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (canProceed) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "다음",
                    tint = if (canProceed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "지금 머릿속에 있는 생각을\n모두 쏟아보세요",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "판단하지 말고, 그저 기록하세요.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(32.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allItems) { item ->
                DumpItemCard(item)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
                decorationBox = { innerTextField ->
                    if (inputText.isEmpty()) {
                        Text(
                            text = "할 일, 걱정, 아이디어...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            )
            IconButton(
                onClick = {
                    onAddItem()
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
                enabled = inputText.isNotBlank(),
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "추가",
                    tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("#Work", "#Personal", "#Ideas").forEach { tag ->
                Text(
                    text = tag,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
fun DumpItemCard(item: OnBoardingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (item.isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${item.category} • ${item.duration}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

// ============ Phase 2: Select ============
@Composable
fun Phase2SelectScreen(
    items: List<OnBoardingItem>,
    selectedItemIds: Set<Int>,
    onToggleSelection: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(40.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopStart
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "뒤로",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer(rotationZ = 180f)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "TIMEBOX",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "오늘의 선택",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Big Three를 선택하세요 (${selectedItemIds.size}/3)",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(32.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                SelectableItemCard(
                    item = item,
                    isSelected = selectedItemIds.contains(item.id),
                    onToggle = { onToggleSelection(item.id) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = selectedItemIds.size == 3
        ) {
            Text(
                text = "일정 배치",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, null)
        }
    }
}

@Composable
fun SelectableItemCard(
    item: OnBoardingItem,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        item.isAiRecommended -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        item.isAiRecommended -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .clickable { onToggle() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (item.isAiRecommended) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Face,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "AI 추천",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            Text(
                text = item.title,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${item.duration} • ${item.category}",
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .border(
                    2.dp,
                    if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    CircleShape
                )
                .background(
                    if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ============ Phase 3: Box ============
@Composable
fun Phase3BoxScreen(
    selectedItems: List<OnBoardingItem>,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    var draggingItemId by remember { mutableStateOf<Int?>(null) }
    val placedItems = remember { mutableStateMapOf<Int, OnBoardingItem>() }

    val allPlaced = placedItems.size == selectedItems.size && selectedItems.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(40.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopStart
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "뒤로",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer(rotationZ = 180f)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Column {
            Text(
                text = "Big 3 업무를",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "AI 추천 슬롯",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "에 놓아보세요.",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "TO DO",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )

                selectedItems.forEachIndexed { index, item ->
                    val isPlaced = placedItems.values.contains(item)
                    if (!isPlaced) {
                        DraggableTodoCard(
                            item = item,
                            isDragging = draggingItemId == item.id,
                            onDragStart = { draggingItemId = item.id },
                            onDragEnd = { draggingItemId = null }
                        )
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
            ) {
                TimelineView(
                    placedItems = placedItems,
                    isDragging = draggingItemId != null,
                    onDrop = { slotIndex, itemId ->
                        selectedItems.find { it.id == itemId }?.let { item ->
                            placedItems[slotIndex] = item
                        }
                        draggingItemId = null
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = allPlaced
        ) {
            Text(
                text = "완료",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.Check, null)
        }
    }
}

@Composable
fun DraggableTodoCard(
    item: OnBoardingItem,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (isDragging) 0.5f else 1f,
        animationSpec = tween(200)
    )

    val scale by animateFloatAsState(
        targetValue = if (isDragging) 0.95f else 1f,
        animationSpec = tween(200)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .alpha(alpha)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onDragStart()
                    }
                )
            }
            .dragAndDropSource { offset ->
                DragAndDropTransferData(
                    clipData = ClipData.newPlainText(
                        "item_id",
                        item.id.toString()
                    )
                )
            }
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (item.title.length > 15) item.title.take(15) + "..." else item.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.duration,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }

    LaunchedEffect(isDragging) {
        if (!isDragging) {
            onDragEnd()
        }
    }
}

@Composable
fun TimelineView(
    placedItems: Map<Int, OnBoardingItem> = emptyMap(),
    isDragging: Boolean = false,
    onDrop: (Int, Int) -> Unit = { _, _ -> }
) {
    val timeSlots = listOf(
        TimeSlot("08:00", "이메일 확인", false),
        TimeSlot("09:00", "AI 추천 슬롯", true),
        TimeSlot("10:00", "보고서 작성", true),
        TimeSlot("11:30", "AI 추천 슬롯", true),
        TimeSlot("12:00", "점심 식사", false),
        TimeSlot("13:00", "AI 추천 슬롯", true)
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(timeSlots) { index, slot ->
            DropTargetTimeSlotCard(
                slot = slot,
                placedItem = placedItems[index],
                isDragging = isDragging,
                onDrop = { itemId -> onDrop(index, itemId) }
            )
        }
    }
}

data class TimeSlot(val time: String, val label: String, val isAiRecommended: Boolean)

@Composable
fun DropTargetTimeSlotCard(
    slot: TimeSlot,
    placedItem: OnBoardingItem? = null,
    isDragging: Boolean = false,
    onDrop: (Int) -> Unit = {}
) {
    val isPlaced = placedItem != null
    var isHovered by remember { mutableStateOf(false) }

    val backgroundColor = when {
        isPlaced -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        isHovered && isDragging && slot.isAiRecommended -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
        slot.isAiRecommended -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
    }

    val borderColor = when {
        isPlaced -> MaterialTheme.colorScheme.primary
        isHovered && isDragging && slot.isAiRecommended -> MaterialTheme.colorScheme.secondary
        slot.isAiRecommended -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    val dragAndDropTarget = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                if (!slot.isAiRecommended || isPlaced) return false

                val itemId = event.toAndroidDragEvent().clipData
                    ?.getItemAt(0)?.text?.toString()?.toIntOrNull()

                itemId?.let {
                    onDrop(it)
                    isHovered = false
                }
                return itemId != null
            }

            override fun onEntered(event: DragAndDropEvent) {
                if (slot.isAiRecommended && !isPlaced) {
                    isHovered = true
                }
            }

            override fun onExited(event: DragAndDropEvent) {
                isHovered = false
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event ->
                    event.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                },
                target = dragAndDropTarget
            )
            .border(
                width = if (isPlaced) 3.dp else if (slot.isAiRecommended) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = slot.time,
            color = if (isPlaced) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = if (isPlaced) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.width(50.dp)
        )
        Spacer(Modifier.width(8.dp))

        if (isPlaced && placedItem != null) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = placedItem.title,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        } else {
            Text(
                text = slot.label,
                color = if (slot.isAiRecommended) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = if (slot.isAiRecommended) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// ============ Previews ============

@Preview(name = "Phase 1: Brain Dump", showBackground = true)
@Composable
fun Phase1DumpScreenPreview() {
    TimeBoxTheme(darkTheme = true) {
        Phase1DumpScreen(
            userInputItems = listOf(
                OnBoardingItem(1002, "프로젝트 기획서 작성", "사용자 입력", "방금 전", isUserInput = true, isAiRecommended = true),
                OnBoardingItem(1001, "운동하기", "사용자 입력", "방금 전", isUserInput = true)
            ),
            sampleItems = listOf(
                OnBoardingItem(1, "이번 주 주간 보고서 초안 작성하기", "업무", "1분 전", isAiRecommended = true),
                OnBoardingItem(2, "세탁소 들러서 거울 코트 찾아오기", "개인", "3분 전"),
                OnBoardingItem(3, "안부 전화 드리기", "가족", "15분 전")
            ),
            inputText = "",
            canProceed = true,
            onInputChange = {},
            onAddItem = {},
            onNext = {}
        )
    }
}

@Preview(name = "Phase 2: Select Big Three", showBackground = true)
@Composable
fun Phase2SelectScreenPreview() {
    TimeBoxTheme(darkTheme = true) {
        Phase2SelectScreen(
            items = listOf(
                OnBoardingItem(1001, "프로젝트 기획서 작성", "사용자 입력", "방금 전", isAiRecommended = true),
                OnBoardingItem(1, "이번 주 주간 보고서 초안 작성하기", "업무", "1분 전", isAiRecommended = true),
                OnBoardingItem(2, "세탁소 들러서 거울 코트 찾아오기", "개인", "3분 전"),
                OnBoardingItem(3, "안부 전화 드리기", "가족", "15분 전"),
                OnBoardingItem(4, "어제 회의록 정리", "인프라", "1시간 전")
            ),
            selectedItemIds = setOf(1001, 1),
            onToggleSelection = {},
            onNext = {},
            onBack = {}
        )
    }
}

@Preview(name = "Phase 3: Timeline Placement", showBackground = true)
@Composable
fun Phase3BoxScreenPreview() {
    TimeBoxTheme(darkTheme = true) {
        Phase3BoxScreen(
            selectedItems = listOf(
                OnBoardingItem(1001, "프로젝트 기획서 작성", "사용자 입력", "1시간", isAiRecommended = true),
                OnBoardingItem(1, "이번 주 주간 보고서 초안", "업무", "1시간", isAiRecommended = true),
                OnBoardingItem(2, "운동하기", "건강", "30분", isAiRecommended = true)
            ),
            onComplete = {},
            onBack = {}
        )
    }
}

@Preview(name = "Full Onboarding Flow", showBackground = true)
@Composable
fun OnBoardingScreenPreview() {
    val previewUiState = OnBoardingUiState(
        userInputText = "",
        userInputItems = listOf(
            OnBoardingItem(1001, "프로젝트 기획서 작성", "사용자 입력", "방금 전", isUserInput = true, isAiRecommended = true)
        ),
        sampleItems = listOf(
            OnBoardingItem(1, "이번 주 주간 보고서 초안 작성하기", "업무", "1분 전", isAiRecommended = true),
            OnBoardingItem(2, "세탁소 들러서 거울 코트 찾아오기", "개인", "3분 전"),
            OnBoardingItem(3, "안부 전화 드리기", "가족", "15분 전")
        ),
        selectedBigThree = setOf(1001, 1),
        canProceedToPhase2 = true
    )
    
    TimeBoxTheme(darkTheme = true) {
        OnBoardingScreen(
            uiState = previewUiState,
            onIntent = {},
            getPhase1Items = { previewUiState.userInputItems },
            getCombinedItemsForPhase2 = { 
                val allItems = previewUiState.userInputItems + previewUiState.sampleItems
                val aiRecommended = allItems.filter { it.isAiRecommended }
                val others = allItems.filter { !it.isAiRecommended }
                aiRecommended + others
            },
            onComplete = {}
        )
    }
}
