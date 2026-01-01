package com.wngud.timebox.presentation.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
                            color = Color(0xFF1565C0)
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
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // AI 분석 인사이트 카드
            item {
                AIInsightCard()
            }

            // Big Three 달성 분석
            item {
                Text(
                    text = "Big Three 달성 분석",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF111111)
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 제품 기획 문서 완성
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

            // 운동 30분
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

            // AI의 내일 제안
            item {
                AIDailyRecommendation()
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun AIInsightCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = null,
                    tint = Color(0xFF1565C0),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI가 분석한 오늘의 인사이트",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1565C0)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = buildAnnotatedString {
                    append("오늘 아침 9-11시 집중도는 ")
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFF1565C0),
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("90%")
                    }
                    append("로 매우 높았어요! 하지만 오후 3-4시에는 집중력이 60%로 떨어졌네요. 이 시간대에 휴식이나 가벼운 활동을 추천해요.")
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF424242)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // AI 추천 피크 시간
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI 추천 피크 시간",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1565C0)
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "09:00~11:00",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFFFA000)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 시간대별 그래프 (간단한 바 형태)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val hours = listOf("08", "09", "10", "11", "12", "13", "14", "15", "16")
                val heights = listOf(0.4f, 0.9f, 0.9f, 0.85f, 0.5f, 0.45f, 0.3f, 0.35f, 0.4f)
                val highlights = listOf(false, true, true, true, false, false, false, false, false)

                hours.forEachIndexed { index, hour ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(60.dp * heights[index])
                                .background(
                                    color = if (highlights[index]) Color(0xFF1565C0) else Color(0xFFBBDEFB),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = hour,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = if (highlights[index]) Color(0xFF1565C0) else Color(0xFF757575),
                                fontWeight = if (highlights[index]) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BigThreeItem(
    icon: String,
    title: String,
    badge: String,
    badgeColor: Color,
    targetTime: String,
    actualTime: String,
    efficiency: String,
    isPositive: Boolean,
    comment: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = icon,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF111111)
                        )
                    )
                }

                Surface(
                    color = badgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$targetTime → $actualTime",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = Color(0xFF616161)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isPositive) Icons.Default.ThumbUp else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isPositive) Color(0xFF4CAF50) else Color(0xFFFF5252),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "효율 $efficiency ${if (isPositive) "상승" else "하락"}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFFF5252)
                    )
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = comment,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        color = Color(0xFF616161)
                    )
                )
            }
        }
    }
}

@Composable
fun AIDailyRecommendation() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEDE7F6)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFF673AB7),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI의 내일 제안",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF673AB7)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = buildAnnotatedString {
                    append("내일 운동 블록을 ")
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFF673AB7),
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("08:30")
                    }
                    append(" 으로 옮기면 아침 집중 피크와 맞춰 더 잘 달성 수 있어요!")
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF424242)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF673AB7)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Text(
                    text = "바로 적용하기",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatsScreenPreview() {
    MaterialTheme {
        StatsScreen(onBack = {})
    }
}