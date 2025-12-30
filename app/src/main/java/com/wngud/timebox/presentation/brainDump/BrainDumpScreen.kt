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
    var uiState by remember { mutableStateOf(BrainDumpUiState()) }

    BrainDumpContent(
        uiState = uiState,
        onBack = onBack,
        onInputChange = { newText ->
            uiState = uiState.copy(inputText = newText)
        },
        onSendClick = {
            if (uiState.inputText.isNotBlank()) {
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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color(0xFF111111)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF8F9FB)
                )
            )
        },
        containerColor = Color(0xFFF8F9FB)
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 20.dp,
                    bottom = 80.dp // InputArea의 높이를 고려한 패딩
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                items(uiState.items) { item ->
                    BrainDumpItemCard(text = item)
                }
            }

            BrainDumpInputArea(
                text = uiState.inputText,
                onValueChange = onInputChange,
                onSendClick = onSendClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
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
        shape = RoundedCornerShape(50),
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
            Text(
                text = "•",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
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
    Box(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 16.dp) // 화면 가장자리로부터의 패딩
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp), // 내부 콘텐츠 패딩
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                IconButton(
                    onClick = onSendClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF186EF2))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "전송",
                        tint = Color.White,
                        modifier = Modifier
                            .size(18.dp)
                            .offset(x = (-1).dp)
                    )
                }
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