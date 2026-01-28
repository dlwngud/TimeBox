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
import com.wngud.timebox.ui.theme.TimeBoxTheme
import kotlinx.coroutines.launch
import java.util.Date

/**
 * [Stateful] 브레인덤프 화면의 Route 컴포저블
 */
@Composable
fun BrainDumpRoute(
    onBack: () -> Unit,
    viewModel: BrainDumpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        viewModel.snackbarEvent.collect { event ->
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = event.message,
                    actionLabel = event.actionLabel,
                    withDismissAction = true
                )
                if (result == SnackbarResult.ActionPerformed && event.actionLabel == "복구") {
                    viewModel.processIntent(BrainDumpIntent.UndoDeleteItem)
                }
            }
        }
    }

    BrainDumpScreen(
        uiState = uiState,
        onBack = onBack,
        onIntent = viewModel::processIntent,
        snackbarHostState = snackbarHostState
    )
}

/**
 * [Stateless] 브레인덤프 UI 컴포저블
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainDumpScreen(
    uiState: BrainDumpUiState,
    onBack: () -> Unit,
    onIntent: (BrainDumpIntent) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "브레인 덤프",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "오늘 하고 싶은 일, 걱정, 아이디어…\n자유롭게 적어보세요!",
                        style = MaterialTheme.typography.bodyMedium.copy(color = DisabledGray, fontSize = 14.sp, lineHeight = 20.sp),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp, start = 20.dp, end = 20.dp)
                    )
                }
                items(items = uiState.items, key = { it.id }) { item ->
                    BrainDumpItemCard(
                        item = item,
                        onDeleteClick = { onIntent(BrainDumpIntent.DeleteItem(it)) },
                        onItemClick = { onIntent(BrainDumpIntent.StartEditItem(it)) },
                        onToggleBigThree = { onIntent(BrainDumpIntent.ToggleBigThree(it)) }
                    )
                }
            }
            BrainDumpInputArea(
                text = uiState.inputText,
                onValueChange = { onIntent(BrainDumpIntent.InputTextChanged(it)) },
                onSendClick = { onIntent(BrainDumpIntent.SendClick) },
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            )
        }
    }

    if (uiState.editingItemId != null) {
        AlertDialog(
            onDismissRequest = { onIntent(BrainDumpIntent.CancelEditItem) },
            title = { Text("아이템 수정") },
            text = {
                BasicTextField(
                    value = uiState.editingInputText,
                    onValueChange = { onIntent(BrainDumpIntent.EditInputTextChanged(it)) },
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth(),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
            },
            confirmButton = { TextButton(onClick = { onIntent(BrainDumpIntent.SaveEditedItem) }) { Text("저장") } },
            dismissButton = { TextButton(onClick = { onIntent(BrainDumpIntent.CancelEditItem) }) { Text("취소") } }
        )
    }
}

@Composable
fun BrainDumpItemCard(item: BrainDumpItem, onDeleteClick: (Long) -> Unit, onItemClick: (BrainDumpItem) -> Unit, onToggleBigThree: (Long) -> Unit) {
    Card(
        shape = RoundedCornerShape(20),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(20.dp)).clickable { onItemClick(item) }
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                IconButton(onClick = { onToggleBigThree(item.id) }, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = if (item.isBigThree) Color(0xFFFFC107) else DisabledGray)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = item.content, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface))
                    if (item.formattedTimestamp.isNotBlank()) Text(text = item.formattedTimestamp, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = DisabledGray))
                }
            }
            IconButton(onClick = { onDeleteClick(item.id) }) { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = DisabledGray) }
        }
    }
}

@Composable
fun BrainDumpInputArea(text: String, onValueChange: (String) -> Unit, onSendClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f)) {
                    if (text.isEmpty()) Text(text = "여기에 입력하세요...", style = TextStyle(color = SubtitleGray, fontSize = 15.sp))
                    BasicTextField(value = text, onValueChange = onValueChange, textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp), singleLine = true, cursorBrush = SolidColor(SwitchBlue), modifier = Modifier.fillMaxWidth())
                }
                IconButton(onClick = onSendClick, modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF186EF2))) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "전송", tint = Color.White, modifier = Modifier.size(18.dp).offset(x = (-1).dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BrainDumpScreenPreview() {
    TimeBoxTheme {
        BrainDumpScreen(
            uiState = BrainDumpUiState(
                items = listOf(BrainDumpItem(1, "테스트 아이템", "10:00", false, Date()))
            ),
            onBack = {},
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
