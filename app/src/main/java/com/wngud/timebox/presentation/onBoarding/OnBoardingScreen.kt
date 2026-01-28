package com.wngud.timebox.presentation.onBoarding

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
import com.wngud.timebox.ui.theme.TimeBoxTheme
import kotlinx.coroutines.launch

// 색상 정의 (업로드된 디자인 기반)
private val DarkBackground = Color(0xFF1A1F2E)
private val DeepFocusIndigo = Color(0xFF4F46E5)
private val ElectricCyan = Color(0xFF22D3EE)
private val CardBackground = Color(0xFF252B3A)
private val TextSecondary = Color(0xFF9CA3AF)


@Composable
fun OnBoardingScreen(
    onComplete: () -> Unit,
    viewModel: OnBoardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> Phase1DumpScreen(
                    userInputItems = viewModel.getPhase1Items(),
                    sampleItems = uiState.sampleItems,
                    inputText = uiState.userInputText,
                    canProceed = uiState.canProceedToPhase2,
                    onInputChange = { viewModel.processIntent(OnBoardingIntent.UpdateInputText(it)) },
                    onAddItem = { viewModel.processIntent(OnBoardingIntent.AddUserItem) },
                    onNext = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )
                1 -> Phase2SelectScreen(
                    items = viewModel.getCombinedItemsForPhase2(),
                    selectedItemIds = uiState.selectedBigThree,
                    onToggleSelection = { viewModel.processIntent(OnBoardingIntent.ToggleBigThree(it)) },
                    onNext = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    },
                    onBack = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                )
                2 -> Phase3BoxScreen(
                    selectedItems = viewModel.getCombinedItemsForPhase2()
                        .filter { uiState.selectedBigThree.contains(it.id) },
                    onComplete = onComplete,
                    onBack = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
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
                            if (index == pagerState.currentPage) DeepFocusIndigo
                            else Color.Gray.copy(alpha = 0.3f)
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

    // 사용자 입력 항목 + 예시 항목을 합쳐서 최신 항목이 위로 오도록 정렬
    val allItems = remember(userInputItems, sampleItems) {
        (userInputItems + sampleItems).sortedByDescending { it.id }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(40.dp))

        // 헤더 - 다음 버튼만 표시
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
                onClick = onNext,
                enabled = canProceed, // 최소 1개 입력 시에만 활성화
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (canProceed) DeepFocusIndigo.copy(alpha = 0.3f)
                        else Color.Gray.copy(alpha = 0.2f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "다음",
                    tint = if (canProceed) Color.White else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        // 중앙 아이콘과 메시지
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f),
            contentAlignment = Alignment.Center
        ) {
            // 글로우 효과
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ElectricCyan.copy(alpha = 0.3f),
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
                tint = ElectricCyan
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "지금 머릿속에 있는 생각을\n모두 쏟아보세요",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "판단하지 말고, 그저 기록하세요.",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(32.dp))

        // 덤프된 아이템 리스트
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allItems) { item ->
                DumpItemCard(item)
            }
        }

        Spacer(Modifier.height(16.dp))

        // 입력창
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(ElectricCyan),
                decorationBox = { innerTextField ->
                    if (inputText.isEmpty()) {
                        Text(
                            text = "할 일, 걱정, 아이디어...",
                            color = TextSecondary,
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
                        if (inputText.isNotBlank()) DeepFocusIndigo else Color.Gray.copy(alpha = 0.3f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "추가",
                    tint = if (inputText.isNotBlank()) Color.White else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 태그
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("#Work", "#Personal", "#Ideas").forEach { tag ->
                Text(
                    text = tag,
                    color = TextSecondary,
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
                color = ElectricCyan.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(CardBackground.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (item.isChecked) ElectricCyan else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${item.category} • ${item.duration}",
                color = TextSecondary,
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

        // 뒤로가기 버튼
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopStart
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .background(CardBackground, CircleShape)
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "뒤로",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer(rotationZ = 180f)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // 타이틀
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
                    tint = DeepFocusIndigo,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "TIMEBOX",
                    color = Color.White,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "오늘의 선택",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Big Three를 선택하세요 (${selectedItemIds.size}/3)",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(32.dp))

        // 선택 가능한 아이템 리스트
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

        // 다음 버튼
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = DeepFocusIndigo
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
        isSelected -> DeepFocusIndigo
        item.isAiRecommended -> ElectricCyan.copy(alpha = 0.1f)
        else -> CardBackground
    }

    val borderColor = when {
        isSelected -> DeepFocusIndigo
        item.isAiRecommended -> ElectricCyan
        else -> Color.Gray.copy(alpha = 0.3f)
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
                        tint = ElectricCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "AI 추천",
                        color = ElectricCyan,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            Text(
                text = item.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${item.duration} • ${item.category}",
                color = if (isSelected) Color.White.copy(alpha = 0.7f) else TextSecondary,
                fontSize = 12.sp
            )
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .border(
                    2.dp,
                    if (isSelected) Color.White else Color.Gray,
                    CircleShape
                )
                .background(
                    if (isSelected) Color.White else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = DeepFocusIndigo,
                    modifier = Modifier.size(16.dp)
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
    // 드래그 중인 카드와 배치 상태 관리
    var draggingItemId by remember { mutableStateOf<Int?>(null) }
    val placedItems = remember { mutableStateMapOf<Int, OnBoardingItem>() } // slotIndex to item

    // 모든 아이템이 배치되었는지 확인
    val allPlaced = placedItems.size == selectedItems.size && selectedItems.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(40.dp))

        // 헤더 - 뒤로가기 버튼만 표시
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopStart
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .background(CardBackground, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "뒤로",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer(rotationZ = 180f)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // 안내 메시지
        Column {
            Text(
                text = "Big 3 업무를",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "AI 추천 슬롯",
                    color = DeepFocusIndigo,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "에 놓아보세요.",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.weight(1f)) {
            // 왼쪽: TO DO 리스트
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "TO DO",
                    color = TextSecondary,
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

            // 오른쪽: 타임라인
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

        // 완료 버튼
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepFocusIndigo
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

// 드래그 가능한 TodoCard
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
                        CardBackground,
                        CardBackground.copy(alpha = 0.8f)
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
                tint = DeepFocusIndigo,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (item.title.length > 15) item.title.take(15) + "..." else item.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.duration,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }

    // 드래그 종료 감지
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

// 드롭 타겟 TimeSlotCard
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
        isPlaced -> DeepFocusIndigo.copy(alpha = 0.6f)
        isHovered && isDragging && slot.isAiRecommended -> ElectricCyan.copy(alpha = 0.4f)
        slot.isAiRecommended -> ElectricCyan.copy(alpha = 0.2f)
        else -> CardBackground.copy(alpha = 0.5f)
    }

    val borderColor = when {
        isPlaced -> DeepFocusIndigo
        isHovered && isDragging && slot.isAiRecommended -> ElectricCyan
        slot.isAiRecommended -> ElectricCyan
        else -> Color.Gray.copy(alpha = 0.3f)
    }

    val scale by animateDpAsState(
        targetValue = if (isPlaced) 2.dp else if (isHovered && isDragging) 4.dp else 0.dp,
        animationSpec = tween(400)
    )

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
            .graphicsLayer {
                shadowElevation = scale.toPx()
            }
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
            color = if (isPlaced) Color.White else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isPlaced) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.width(50.dp)
        )
        Spacer(Modifier.width(8.dp))

        if (isPlaced && placedItem != null) {
            // 배치된 아이템 표시
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = placedItem.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        } else {
            // 빈 슬롯 표시
            Text(
                text = slot.label,
                color = if (slot.isAiRecommended) ElectricCyan else Color.White,
                fontSize = 13.sp,
                fontWeight = if (slot.isAiRecommended) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// ============ Previews ============
@Preview(showBackground = true)
@Composable
fun Phase1DumpScreenPreview() {
    TimeBoxTheme {
        Phase1DumpScreen(
            userInputItems = listOf(
                OnBoardingItem(
                    id = 2,
                    title = "프로젝트 기획서 작성",
                    category = "업무",
                    duration = "1시간",
                    isAiRecommended = false,
                    isChecked = false
                ),
                OnBoardingItem(
                    id = 1,
                    title = "운동하기",
                    category = "건강",
                    duration = "30분",
                    isAiRecommended = false,
                    isChecked = false
                )
            ),
            sampleItems = listOf(
                OnBoardingItem(
                    id = 0,
                    title = "이메일 확인",
                    category = "업무",
                    duration = "15분",
                    isAiRecommended = false,
                    isChecked = false
                )
            ),
            inputText = "",
            canProceed = false,
            onInputChange = {},
            onAddItem = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E27)
@Composable
fun Phase2SelectScreenPreview() {
    TimeBoxTheme {
        Phase2SelectScreen(
            items = listOf(
                OnBoardingItem(
                    id = 2,
                    title = "프로젝트 기획서 작성",
                    category = "업무",
                    duration = "1시간",
                    isAiRecommended = true,
                    isChecked = false
                ),
                OnBoardingItem(
                    id = 1,
                    title = "운동하기",
                    category = "건강",
                    duration = "30분",
                    isAiRecommended = true,
                    isChecked = false
                ),
                OnBoardingItem(
                    id = 0,
                    title = "이메일 확인",
                    category = "업무",
                    duration = "15분",
                    isAiRecommended = false,
                    isChecked = false
                )
            ),
            selectedItemIds = setOf(2, 1),
            onToggleSelection = {},
            onNext = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E27)
@Composable
fun Phase3BoxScreenPreview() {
    TimeBoxTheme {
        Phase3BoxScreen(
            selectedItems = listOf(
                OnBoardingItem(
                    id = 2,
                    title = "프로젝트 기획서 작성",
                    category = "업무",
                    duration = "1시간",
                    isAiRecommended = true,
                    isChecked = false
                ),
                OnBoardingItem(
                    id = 1,
                    title = "운동하기",
                    category = "건강",
                    duration = "30분",
                    isAiRecommended = true,
                    isChecked = false
                ),
                OnBoardingItem(
                    id = 0,
                    title = "독서",
                    category = "자기계발",
                    duration = "45분",
                    isAiRecommended = true,
                    isChecked = false
                )
            ),
            onComplete = {},
            onBack = {}
        )
    }
}