package com.wngud.timebox.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.wngud.timebox.data.modal.DailyStats
import com.wngud.timebox.data.modal.EventColorType
import com.wngud.timebox.data.modal.ScheduleEvent
import com.wngud.timebox.data.modal.Task
import com.wngud.timebox.ui.theme.AccentRed
import com.wngud.timebox.ui.theme.BackgroundGray
import com.wngud.timebox.ui.theme.CardWhite
import com.wngud.timebox.ui.theme.EventBlueBg
import com.wngud.timebox.ui.theme.EventBlueBorder
import com.wngud.timebox.ui.theme.EventGreenBg
import com.wngud.timebox.ui.theme.EventGreenBorder
import com.wngud.timebox.ui.theme.SuccessGreen
import com.wngud.timebox.ui.theme.TextBlack
import com.wngud.timebox.ui.theme.TextGray

@Composable
fun DailySummaryCard(stats: DailyStats, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .background(Brush.horizontalGradient(listOf(Color(0xFF304FFE), Color(0xFF448AFF))))
                .padding(20.dp)
        ) {
            Column {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(
                        "2025.11.18 (화) 오늘의 결과",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("자세히 보기", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            "Detail",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        StatItem("집중 ${stats.focusTime}", Color.Green)
                        Spacer(Modifier.height(8.dp))
                        StatItem("버퍼 ${stats.bufferPercent}%", Color(0xFFFFD700))
                    }
                    Column(Modifier.weight(1f)) {
                        StatItem(
                            "Big Three ${stats.bigThreeCompleted}/${stats.bigThreeTotal}",
                            Color.Green
                        )
                        Spacer(Modifier.height(8.dp))
                        StatItem("효율 +${stats.efficiencyPercent}%", Color.Cyan)
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color.White, fontSize = 14.sp)
    }
}

@Composable
fun BigThreeSection(tasks: List<Task>) {
    var isExpanded by remember { mutableStateOf(true) }
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFA000))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Big Three",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextBlack
                    )
                }
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        "Toggle",
                        tint = TextBlack
                    )
                }
            }
            if (isExpanded) {
                Spacer(Modifier.height(12.dp))
                tasks.forEach { task ->
                    TaskItem(task)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: Task) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundGray, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (task.isCompleted) Icons.Default.Star else Icons.Outlined.Star,
            null,
            tint = if (task.isCompleted) SuccessGreen else TextGray
        )
        Spacer(Modifier.width(12.dp))
        Text(task.title, style = MaterialTheme.typography.bodyMedium, color = TextBlack)
    }
}

@Composable
fun TimelineSection(events: List<ScheduleEvent>) {
    val startHour = 8
    val endHour = 15
    val hourHeight = 80.dp

    Box(Modifier.fillMaxWidth()) {
        Column {
            for (hour in startHour..endHour) {
                Row(Modifier.height(hourHeight), verticalAlignment = Alignment.Top) {
                    Text(
                        String.format("%02d:00", hour),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.width(40.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color.LightGray.copy(alpha = 0.5f))
                    )
                }
            }
        }
        events.forEach { event ->
            val start = event.startTime.hour + (event.startTime.minute / 60f)
            val end = event.endTime.hour + (event.endTime.minute / 60f)
            val topOffset = (start - startHour) * hourHeight.value
            val height = (end - start) * hourHeight.value
            EventCard(
                event,
                Modifier
                    .padding(start = 56.dp)
                    .offset(y = topOffset.dp)
                    .height(height.dp)
                    .fillMaxWidth()
            )
        }
        val currentTimeOffset = (10.5f - startHour) * hourHeight.value
        Box(
            Modifier
                .offset(y = currentTimeOffset.dp)
                .fillMaxWidth()
                .height(12.dp)
                .zIndex(1f)
        ) {
            Box(
                Modifier
                    .offset(x = (-4).dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(AccentRed)
            )
            Box(
                Modifier
                    .padding(start = 40.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AccentRed)
                    .align(Alignment.CenterStart)
            )
        }
    }
}

@Composable
fun EventCard(event: ScheduleEvent, modifier: Modifier) {
    val (bgColor, borderColor) = when (event.colorType) {
        EventColorType.GREEN -> EventGreenBg to EventGreenBorder
        EventColorType.BLUE -> EventBlueBg to EventBlueBorder
        else -> BackgroundGray to TextGray
    }
    Card(
        modifier = modifier.padding(bottom = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp), verticalArrangement = Arrangement.Center
        ) {
            Text(event.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextBlack)
            Text(event.timeRange, fontSize = 12.sp, color = TextGray)
        }
    }
}