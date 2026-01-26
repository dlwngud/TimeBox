package com.wngud.timebox.presentation.onBoarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch

// 색상 정의 (업로드된 디자인 기반)
private val DarkBackground = Color(0xFF1A1F2E)
private val DeepFocusIndigo = Color(0xFF4F46E5)
private val ElectricCyan = Color(0xFF22D3EE)
private val CardBackground = Color(0xFF252B3A)
private val TextSecondary = Color(0xFF9CA3AF)

data class BrainDumpItem(
    val id: Int,
    val title: String,
    val category: String,
    val duration: String,
    var isChecked: Boolean = false,
    var isAiRecommended: Boolean = false
)

@Composable
fun OnBoardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    
    // 샘플 데이터
    val sampleItems = remember {
        mutableStateListOf(
            BrainDumpItem(1, "이번 주 주간 보고서 초안 작성하기", "업무", "1분 전", false, false),
            BrainDumpItem(2, "세탁소 들러서 거울 코트 찾아오기", "개인", "3분 전", false, false),
            BrainDumpItem(3, "컴퓨터에 인부 전화 드리기", "가족", "15분 전", false, false),
            BrainDumpItem(4, "어제 회의록 정리", "인프라", "1시간 전", false, false)
        )
    }
    
    val selectedItems = remember { mutableStateListOf<BrainDumpItem>() }
    
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
                    items = sampleItems,
                    onNext = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )
                1 -> Phase2SelectScreen(
                    items = sampleItems,
                    selectedItems = selectedItems,
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
                    selectedItems = selectedItems,
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
    items: List<BrainDumpItem>,
    onNext: () -> Unit
) {
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
                modifier = Modifier
                    .size(48.dp)
                    .background(DeepFocusIndigo.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "다음",
                    tint = Color.White,
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
            items(items) { item ->
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
            Text(
                text = "할 일, 걱정, 아이디어...",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(40.dp)
                    .background(DeepFocusIndigo, CircleShape)
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
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
fun DumpItemCard(item: BrainDumpItem) {
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
    items: List<BrainDumpItem>,
    selectedItems: MutableList<BrainDumpItem>,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    // AI 추천 시뮬레이션
    LaunchedEffect(Unit) {
        items.take(5).forEach { it.isAiRecommended = true }
    }
    
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
                text = "Big Three를 선택하세요 (${selectedItems.size}/3)",
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
                    isSelected = selectedItems.contains(item),
                    onToggle = {
                        if (selectedItems.contains(item)) {
                            selectedItems.remove(item)
                        } else if (selectedItems.size < 3) {
                            selectedItems.add(item)
                        }
                    }
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
            enabled = selectedItems.size == 3
        ) {
            Text(
                text = "몰입 시작",
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
    item: BrainDumpItem,
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
    selectedItems: List<BrainDumpItem>,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
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
                
                selectedItems.forEach { item ->
                    TodoCard(item)
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            // 오른쪽: 타임라인
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
            ) {
                TimelineView()
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
            shape = RoundedCornerShape(16.dp)
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
fun TodoCard(item: BrainDumpItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
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
                text = item.title.take(15) + "...",
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
}

@Composable
fun TimelineView() {
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
        items(timeSlots) { slot ->
            TimeSlotCard(slot)
        }
    }
}

data class TimeSlot(val time: String, val label: String, val isAiRecommended: Boolean)

@Composable
fun TimeSlotCard(slot: TimeSlot) {
    val backgroundColor = if (slot.isAiRecommended) {
        ElectricCyan.copy(alpha = 0.2f)
    } else {
        CardBackground.copy(alpha = 0.5f)
    }
    
    val borderColor = if (slot.isAiRecommended) {
        ElectricCyan
    } else {
        Color.Gray.copy(alpha = 0.3f)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (slot.isAiRecommended) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = slot.time,
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.width(50.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = slot.label,
            color = if (slot.isAiRecommended) ElectricCyan else Color.White,
            fontSize = 13.sp,
            fontWeight = if (slot.isAiRecommended) FontWeight.Bold else FontWeight.Normal
        )
    }
}