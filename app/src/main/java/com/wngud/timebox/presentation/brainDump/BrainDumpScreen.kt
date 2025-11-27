package com.wngud.timebox.presentation.brainDump

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ------------------------------------------------------------------------
// 1. State Definition (상태 정의)
// ------------------------------------------------------------------------
data class BrainDumpUiState(
    val items: List<String> = listOf(
        "장보기 목록 정리",
        "새로운 프로젝트 아이디어 스케치",
        "주말 여행 계획 세우기"
    ),
    val inputText: String = ""
)

// ------------------------------------------------------------------------
// 2. Stateful Composable (상태 관리 & 로직 처리)
// ------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainDumpScreen(
    onBack: () -> Unit
) {
    // ViewModel의 StateFlow를 collectAsState()로 받는 것을 가정
    var uiState by remember { mutableStateOf(BrainDumpUiState()) }

    BrainDumpContent(
        uiState = uiState,
        onBack = onBack,
        onInputChange = { newText ->
            uiState = uiState.copy(inputText = newText)
        },
        onSendClick = {
            if (uiState.inputText.isNotBlank()) {
                // 기존 리스트에 새 항목 추가 후 입력창 초기화
                uiState = uiState.copy(
                    items = uiState.items + uiState.inputText,
                    inputText = ""
                )
            }
        }
    )
}

// ------------------------------------------------------------------------
// 3. Stateless Composable (UI 렌더링)
// ------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainDumpContent(
    uiState: BrainDumpUiState,
    onBack: () -> Unit,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "브레인 덤프",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF111111)
                        )
                    )
                },
                navigationIcon = { /* ... */ },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF8F9FB)
                )
            )
        },
        containerColor = Color(0xFFF8F9FB) // 전체 배경색
    ) { paddingValues ->

        // 1. Box를 사용하여 LazyColumn과 입력창을 겹치게 배치합니다. (레이아웃 구조: LazyColumn + Box(TextField))
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()) // TopBar 높이만큼만 상단 패딩 적용
        ) {
            // 2. 키보드 높이 계산 (베스트 프랙티스: WindowInsets.ime.asPaddingValues() 사용)
            val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

            // 3. 메인 컨텐츠 영역 (LazyColumn)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // LazyColumn의 contentPadding에 키보드 높이와 BottomBar 높이(혹은 입력창 높이)를 모두 반영합니다.
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 20.dp,
                    // Bottom Padding 계산:
                    // 현재 입력창이 차지하는 공간(56dp+16dp*2 = 약 88dp) + 키보드 높이
                    // imeBottomPadding을 사용하여, 키보드가 나타나면 이 값이 0보다 커져 리스트가 밀려 올라갑니다.
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 상단 설명 텍스트
                item {
                    Text(
                        text = "오늘 하고 싶은 일, 걱정, 아이디어… 자유롭게 적어보세요!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF8D94A0),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp, start = 20.dp, end = 20.dp)
                    )
                }
                // 리스트 아이템들
                items(uiState.items) { item ->
                    BrainDumpItemCard(text = item)
                }
            }

            // 4. 하단 입력창 영역 (Alignment.BottomCenter에 고정)
            BrainDumpInputArea(
                text = uiState.inputText,
                onValueChange = onInputChange,
                onSendClick = onSendClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter) // 하단 중앙에 고정
            )
        }
    }
}

// ------------------------------------------------------------------------
// 4. Sub Components (하위 컴포넌트)
// ------------------------------------------------------------------------

@Composable
fun BrainDumpItemCard(text: String) {
    Card(
        shape = RoundedCornerShape(50), // 캡슐 형태 (완전 둥근 모서리)
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bullet Point
            Text(
                text = "•",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
            // Content
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    color = Color(0xFF111111)
                )
            )
        }
    }
}

@Composable
fun BrainDumpInputArea(
    text: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 하단 영역 컨테이너
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 텍스트 필드 배경 (흰색 둥근 박스)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(Color(0xFFF8F9FB)) // 스캐폴드 배경색과 맞춥니다.
                .padding(horizontal = 16.dp, vertical = 8.dp), // 상하좌우 패딩 적용
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 입력 필드 (BasicTextField로 커스텀 스타일링)
            Box(modifier = Modifier.weight(1f)) {
                if (text.isEmpty()) {
                    Text(
                        text = "여기에 입력하세요...",
                        style = TextStyle(
                            color = Color(0xFFB0B0B0),
                            fontSize = 15.sp
                        )
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 15.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(Color(0xFF186EF2)),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 전송 버튼
            IconButton(
                onClick = onSendClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF186EF2)) // 파란색 배경
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "전송",
                    tint = Color.White,
                    modifier = Modifier
                        .size(18.dp)
                        .offset(x = (-1).dp) // 아이콘 시각적 중심 보정
                )
            }
        }
    }
}

// ------------------------------------------------------------------------
// Preview
// ------------------------------------------------------------------------
@Preview(showBackground = true)
@Composable
fun BrainDumpScreenPreview() {
    MaterialTheme {
        BrainDumpScreen(onBack = {})
    }
}