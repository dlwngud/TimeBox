package com.wngud.timebox.presentation.onBoarding

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wngud.timebox.ui.theme.TimeBoxTheme
import kotlinx.coroutines.launch

@Composable
fun OnBoardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val currentPage = pagerState.currentPage
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 건너뛰기 버튼
        if (currentPage < 4) {
            Box(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = onComplete,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text("건너뛰기")
                }
            }
        } else {
            Spacer(Modifier.height(48.dp))
        }

        // 페이저
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnBoardingPage(page = page)
        }

        // 인디케이터 + 버튼
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PageIndicator(pageCount = 5, currentPage = currentPage)
            Spacer(Modifier.height(32.dp))

            if (currentPage == 4) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("시작하기")
                }
            } else {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(currentPage + 1)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("다음")
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun OnBoardingPage(page: Int) {
    val (title, desc, icon) = when (page) {
        0 -> Triple("자유롭게 생각을 쏟아내세요", "판단 없이, 모든 아이디어를 기록하세요.", Icons.Default.Edit)
        1 -> Triple("패턴을 발견하세요", "과거 기록을 보며 인사이트를 얻어요.", Icons.Default.Star)
        2 -> Triple("매일 리마인드", "저녁 9시, 하루를 정리할 시간이에요.", Icons.Default.Notifications)
        3 -> Triple("완전한 프라이버시", "데이터는 기기 내에만. 클라우드 없음.", Icons.Default.Lock)
        4 -> Triple("준비되셨나요?", "지금 바로 시작해보세요!", Icons.Default.CheckCircle)
        else -> Triple("", "", Icons.Default.Check)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(32.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(desc, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = Color.Gray)
    }
}

@Composable
fun PageIndicator(pageCount: Int, currentPage: Int) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        repeat(pageCount) { index ->
            val width = if (index == currentPage) 24.dp else 8.dp
            val color = if (index == currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    TimeBoxTheme {
        OnBoardingScreen({})
    }
}