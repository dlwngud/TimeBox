package com.wngud.timebox.presentation.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wngud.timebox.ui.theme.TimeBoxTheme

/**
 * [Stateful] 통계 화면의 Route 컴포저블.
 * 향후 ViewModel과의 의존성을 가지고 상태를 UI 전용 컴포저블에 전달합니다.
 * 현재는 ViewModel이 없으므로 직접 StatsScreen을 호출합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsRoute(
    onBack: () -> Unit
) {
    // TODO: ViewModel 추가 시 여기서 상태를 수집하고 StatsScreen에 전달
    // val viewModel: StatsViewModel = hiltViewModel()
    // val uiState by viewModel.uiState.collectAsState()
    
    StatsScreen(onBack = onBack)
}

/**
 * [Stateless] 통계 UI 렌더링을 담당하는 컴포저블.
 * ViewModel 의존성 없이 상태(State)와 이벤트만 전달받아 프리뷰와 테스트가 용이합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "2025.11.18 (화) 오늘의 AI 분석",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item { AIInsightCard() }

            item {
                Text(
                    text = "Big Three 달성 분석",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }

            item {
                BigThreeItem(
                    icon = "📄",
                    title = "제품 기획 문서 완성",
                    badge = "완벽해요!",
                    badgeColor = Color(0xFF4CAF50),
                    targetTime = "목표 60분",
                    actualTime = "실제 55분",
                    efficiency = "+8%",
                    isPositive = true,
                    comment = "집중력이 최고조였어요 👍"
                )
            }

            item {
                BigThreeItem(
                    icon = "✈️",
                    title = "운동 30분",
                    badge = "아쉽지만 괜찮아요!",
                    badgeColor = Color(0xFFFF9800),
                    targetTime = "목표 30분",
                    actualTime = "실제 25분",
                    efficiency = "-17%",
                    isPositive = false,
                    comment = "피로도가 조금 있었네요."
                )
            }

            item { AIDailyRecommendation() }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun AIInsightCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Face, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "AI가 분석한 오늘의 인사이트", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = buildAnnotatedString {
                    append("오늘 아침 9-11시 집중도는 ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) { append("90%") }
                    append("로 매우 높았어요! 하지만 오후 3-4시에는 집중력이 60%로 떨어졌네요.")
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "AI 추천 피크 시간", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "09:00~11:00", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.tertiary))
                }
            }
        }
    }
}

@Composable
fun BigThreeItem(icon: String, title: String, badge: String, badgeColor: Color, targetTime: String, actualTime: String, efficiency: String, isPositive: Boolean, comment: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                }
                Surface(color = badgeColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                    Text(text = badge, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = badgeColor))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "$targetTime → $actualTime", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = if (isPositive) Icons.Default.ThumbUp else Icons.Default.Close, contentDescription = null, tint = if (isPositive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "효율 $efficiency", style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isPositive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = comment, style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
        }
    }
}

@Composable
fun AIDailyRecommendation() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "AI의 내일 제안", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "내일 운동 블록을 08:30으로 옮기면 더 효율적이에요!", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSecondaryContainer))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary), shape = RoundedCornerShape(10.dp), modifier = Modifier.height(44.dp)) {
                Text(text = "바로 적용하기", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}


// ============ Previews ============

@Preview(name = "Stats Screen - Light", showBackground = true)
@Composable
fun StatsScreenPreview() {
    TimeBoxTheme(darkTheme = false) {
        StatsScreen(onBack = {})
    }
}

@Preview(name = "Stats Screen - Dark", showBackground = true)
@Composable
fun StatsScreenDarkPreview() {
    TimeBoxTheme(darkTheme = true) {
        StatsScreen(onBack = {})
    }
}

@Preview(name = "AI Insight Card", showBackground = true)
@Composable
fun AIInsightCardPreview() {
    TimeBoxTheme(darkTheme = true) {
        AIInsightCard()
    }
}

@Preview(name = "Big Three Item - Success", showBackground = true)
@Composable
fun BigThreeItemSuccessPreview() {
    TimeBoxTheme(darkTheme = true) {
        BigThreeItem(
            icon = "📄",
            title = "제품 기획 문서 완성",
            badge = "완벽해요!",
            badgeColor = Color(0xFF4CAF50),
            targetTime = "목표 60분",
            actualTime = "실제 55분",
            efficiency = "+8%",
            isPositive = true,
            comment = "집중력이 최고조였어요 👍"
        )
    }
}

@Preview(name = "Big Three Item - Warning", showBackground = true)
@Composable
fun BigThreeItemWarningPreview() {
    TimeBoxTheme(darkTheme = true) {
        BigThreeItem(
            icon = "✈️",
            title = "운동 30분",
            badge = "아쉽지만 괜찮아요!",
            badgeColor = Color(0xFFFF9800),
            targetTime = "목표 30분",
            actualTime = "실제 25분",
            efficiency = "-17%",
            isPositive = false,
            comment = "피로도가 조금 있었네요."
        )
    }
}

@Preview(name = "AI Daily Recommendation", showBackground = true)
@Composable
fun AIDailyRecommendationPreview() {
    TimeBoxTheme(darkTheme = true) {
        AIDailyRecommendation()
    }
}
