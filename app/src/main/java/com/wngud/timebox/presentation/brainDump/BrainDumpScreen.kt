package com.wngud.timebox.presentation.brainDump

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.wngud.timebox.ui.theme.DisabledGray
import com.wngud.timebox.ui.theme.SubtitleGray
import com.wngud.timebox.ui.theme.SwitchBlue
import kotlinx.coroutines.launch // launch import 추가

// ------------------------------------------------------------------------
// 2. Stateful Composable (상태 관리 & 로직 처리)
// ------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainDumpScreen(
    onBack: () -> Unit,
    viewModel: BrainDumpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() } // SnackbarHostState 생성
    val scope = rememberCoroutineScope() // CoroutineScope 생성

    // 스낵바 이벤트 구독 및 표시 로직
    LaunchedEffect(key1 = Unit) {
        viewModel.snackbarEvent.collect { event ->
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = event.message,
                    actionLabel = event.actionLabel,
                    withDismissAction = true // 닫기 액션 추가
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        // "복구" 버튼이 눌렸을 때
                        if (event.actionLabel == "복구" && event.deletedItem != null) {
                            viewModel.processIntent(BrainDumpIntent.UndoDeleteItem)
                        }
                    }
                    SnackbarResult.Dismissed -> {
                        // 스낵바가 닫혔을 때 (다른 이유로)
                    }
                }
            }
        }
    }

    BrainDumpContent(
        uiState = uiState,
        onBack = onBack,
        onIntent = viewModel::processIntent,
        snackbarHostState = snackbarHostState // SnackbarHostState 전달
    )

    // 수정 다이얼로그
    if (uiState.editingItemId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.processIntent(BrainDumpIntent.CancelEditItem) },
            title = { Text("아이템 수정") },
            text = {
                BasicTextField(
                    value = uiState.editingInputText,
                    onValueChange = { newText -> viewModel.processIntent(BrainDumpIntent.EditInputTextChanged(newText)) },
                    textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth(),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.processIntent(BrainDumpIntent.SaveEditedItem) }) {
                    Text("저장")
                }
            },
            dismissButton = { 
                TextButton(onClick = { viewModel.processIntent(BrainDumpIntent.CancelEditItem) }) {
                    Text("취소")
                }
            }
        )
    }
}

// ------------------------------------------------------------------------
// 3. Stateless Composable (UI 렌더링)
// ------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainDumpContent(
    uiState: BrainDumpUiState,
    onBack: () -> Unit,
    onIntent: (BrainDumpIntent) -> Unit,
    snackbarHostState: SnackbarHostState // SnackbarHostState를 파라미터로 받도록 변경
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
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) } // SnackbarHost 연결
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
                        text = "오늘 하고 싶은 일, 걱정, 아이디어…\n자유롭게 적어보세요!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DisabledGray,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp, start = 20.dp, end = 20.dp)
                    )
                }
                items(
                    items = uiState.items,
                    key = { item -> item.id } // item.id를 고유 키로 사용
                ) { item ->
                    BrainDumpItemCard(
                        item = item,
                        onDeleteClick = { id -> onIntent(BrainDumpIntent.DeleteItem(id)) },
                        onItemClick = { clickedItem -> onIntent(BrainDumpIntent.StartEditItem(clickedItem)) }, // 아이템 클릭 시 수정 시작
                        onToggleBigThree = { id -> onIntent(BrainDumpIntent.ToggleBigThree(id)) } // Big Three 토글
                    )
                }
            }

            BrainDumpInputArea(
                text = uiState.inputText,
                onValueChange = { newText -> onIntent(BrainDumpIntent.InputTextChanged(newText)) },
                onSendClick = { onIntent(BrainDumpIntent.SendClick) },
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
fun BrainDumpItemCard(item: BrainDumpItem, onDeleteClick: (Long) -> Unit, onItemClick: (BrainDumpItem) -> Unit, onToggleBigThree: (Long) -> Unit) {
    Card(
        shape = RoundedCornerShape(20),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp) // 높이 조정
            .clip(RoundedCornerShape(20.dp)) // 클릭 가능한 영역을 위해 clip 추가
            .clickable { onItemClick(item) } // 아이템 클릭 이벤트 추가
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Big Three 별 아이콘
                IconButton(
                    onClick = { onToggleBigThree(item.id) },
                    modifier = Modifier.size(24.dp) // 아이콘 크기 조정
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = if (item.isBigThree) "Big Three 선택됨" else "Big Three 선택 안됨",
                        tint = if (item.isBigThree) Color(0xFFFFC107) else DisabledGray // 노란색 또는 회색
                    )
                }
                Spacer(modifier = Modifier.width(12.dp)) // 아이콘과 텍스트 사이 간격

                Column {
                    Text(
                        text = item.content,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (item.formattedTimestamp.isNotBlank()) {
                        Text(
                            text = item.formattedTimestamp,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = DisabledGray
                            )
                        )
                    }
                }
            }
            IconButton(onClick = { onDeleteClick(item.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Item",
                    tint = DisabledGray
                )
            }
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
            color = MaterialTheme.colorScheme.surface,
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
                                color = SubtitleGray,
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
                        cursorBrush = SolidColor(SwitchBlue),
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
