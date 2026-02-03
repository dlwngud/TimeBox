package com.wngud.timebox.presentation.onBoarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        onIntent(OnBoardingIntent.GoBack)
    }

    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "[${uiState.currentPage + 1}/3]",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (uiState.currentPage < 2) {
                    TextButton(onClick = { onComplete() }) {
                        Text(
                            text = "건너뛰기",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> Phase1DumpScreen(
                        userInputItems = getPhase1Items(),
                        sampleItems = uiState.sampleItems,
                        inputText = uiState.inputText,
                        canProceed = getPhase1Items().size >= 3,
                        onInputChange = { onIntent(OnBoardingIntent.UpdateInputText(it)) },
                        onAddItem = { onIntent(OnBoardingIntent.AddItem) },
                        onNext = { onIntent(OnBoardingIntent.GoNext) }
                    )
                    1 -> Phase2SelectScreen(
                        items = getCombinedItemsForPhase2(),
                        selectedItemIds = uiState.selectedItemIds,
                        onToggleSelection = { onIntent(OnBoardingIntent.ToggleSelection(it)) },
                        onNext = { onIntent(OnBoardingIntent.GoNext) },
                        onBack = { onIntent(OnBoardingIntent.GoBack) }
                    )
                    2 -> Phase3ConfirmScreen(
                        selectedItems = getCombinedItemsForPhase2().filter { it.id in uiState.selectedItemIds },
                        onComplete = {
                            onIntent(OnBoardingIntent.Complete)
                            onComplete()
                        },
                        onBack = { onIntent(OnBoardingIntent.GoBack) }
                    )
                }
            }
        }
    }
}

// ============ Phase 1: Brain Dump ============
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
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Emoji Icon
        Text(
            text = "📝",
            fontSize = 64.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(24.dp))

        // Headline
        Text(
            text = "오늘 할 일을\n모두 적어보세요",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Subtitle
        Text(
            text = "생각나는 대로 편하게 적어주세요",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        // Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(12.dp)
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (inputText.isEmpty()) {
                        Text(
                            text = "할 일 입력...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Task List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allItems) { item ->
                DumpItemCard(item)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Hint Message
        Text(
            text = "💡 최소 3개 이상 입력해주세요 (${allItems.size}/3)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Next Button
        Button(
            onClick = {
                if (inputText.isNotBlank()) {
                    onAddItem()
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
                onNext()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = canProceed,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "다음 (${allItems.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun DumpItemCard(item: OnBoardingItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ============ Phase 2: AI Recommendation ============
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
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Emoji Icon
        Text(
            text = "🎯",
            fontSize = 64.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(24.dp))

        // Headline
        Text(
            text = "오늘의 Big Three를\n선택해주세요",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Subtitle
        Text(
            text = "AI가 중요한 순서대로 추천했어요",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        // Task Selection List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                SelectableItemCard(
                    item = item,
                    isSelected = selectedItemIds.contains(item.id),
                    isAiRecommended = items.indexOf(item) < 3,
                    onToggle = { onToggleSelection(item.id) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Hint Message
        Text(
            text = "💡 정확히 3개를 선택해주세요 (${selectedItemIds.size}/3)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Next Button
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = selectedItemIds.size == 3,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "다음 (${selectedItemIds.size}/3)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(8.dp))

        // Back Button
        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "← 뒤로",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun SelectableItemCard(
    item: OnBoardingItem,
    isSelected: Boolean,
    isAiRecommended: Boolean = false,
    onToggle: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(200),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else 
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Check,
                    contentDescription = null,
                    tint = if (isSelected) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (isAiRecommended) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "🤖 AI 추천",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ============ Phase 3: Confirmation ============
@Composable
fun Phase3ConfirmScreen(
    selectedItems: List<OnBoardingItem>,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Emoji Icon
        Text(
            text = "🎉",
            fontSize = 64.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(24.dp))

        // Headline
        Text(
            text = "오늘의 Big Three",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Subtitle
        Text(
            text = "이 3가지만 완료하면 성공!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        // Big Three Cards
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(selectedItems.take(3)) { item ->
                val index = selectedItems.indexOf(item)
                BigThreeCard(
                    item = item,
                    number = index + 1
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Encouragement Message
        Text(
            text = "💪 오늘도 화이팅!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        // Start Button
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "시작하기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(8.dp))

        // Back Button
        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "← 뒤로",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun BigThreeCard(
    item: OnBoardingItem,
    number: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val emoji = when (number) {
                1 -> "1️⃣"
                2 -> "2️⃣"
                3 -> "3️⃣"
                else -> "✓"
            }
            
            Text(
                text = emoji,
                fontSize = 28.sp
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
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
                OnBoardingItem(1, "주간 보고서 작성", isUserInput = true),
                OnBoardingItem(2, "코드 리뷰 3개", isUserInput = true)
            ),
            sampleItems = listOf(
                OnBoardingItem(3, "운동 30분", isUserInput = false),
                OnBoardingItem(4, "이메일 답장", isUserInput = false)
            ),
            inputText = "",
            canProceed = true,
            onInputChange = {},
            onAddItem = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Phase2SelectScreenPreview() {
    TimeBoxTheme {
        Phase2SelectScreen(
            items = listOf(
                OnBoardingItem(1, "주간 보고서 작성", isUserInput = true),
                OnBoardingItem(2, "코드 리뷰 3개", isUserInput = true),
                OnBoardingItem(3, "운동 30분", isUserInput = true),
                OnBoardingItem(4, "이메일 답장", isUserInput = true)
            ),
            selectedItemIds = setOf(1, 2, 3),
            onToggleSelection = {},
            onNext = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Phase3ConfirmScreenPreview() {
    TimeBoxTheme {
        Phase3ConfirmScreen(
            selectedItems = listOf(
                OnBoardingItem(1, "주간 보고서 작성", isUserInput = true),
                OnBoardingItem(2, "코드 리뷰 3개", isUserInput = true),
                OnBoardingItem(3, "운동 30분", isUserInput = true)
            ),
            onComplete = {},
            onBack = {}
        )
    }
}
