package com.wngud.timebox.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wngud.timebox.presentation.brainDump.BrainDumpItem
import com.wngud.timebox.ui.theme.DisabledGray
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * BrainDump 아이템을 선택할 수 있는 BottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainDumpItemSelectorBottomSheet(
    items: List<BrainDumpItem>,
    selectedTimeSlot: Pair<LocalTime, LocalTime>,
    onItemSelected: (BrainDumpItem) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text = "일정 추가",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = formatTimeSlot(selectedTimeSlot),
                style = MaterialTheme.typography.bodyMedium,
                color = DisabledGray,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            
            Divider(color = DisabledGray.copy(alpha = 0.2f))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Items list
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "배치 가능한 아이템이 없습니다.\nBrain Dump에서 새로운 아이템을 추가해보세요!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DisabledGray,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(items) { item ->
                        BrainDumpItemRow(
                            item = item,
                            onClick = { onItemSelected(item) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * BottomSheet 내부의 BrainDump 아이템 행
 */
@Composable
private fun BrainDumpItemRow(
    item: BrainDumpItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Big Three 표시
            if (item.isBigThree) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Big Three",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.content,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (item.formattedTimestamp.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.formattedTimestamp,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp
                        ),
                        color = DisabledGray
                    )
                }
            }
        }
    }
}

/**
 * 시간 슬롯을 포맷팅 (예: "오전 9:00 - 9:30")
 */
private fun formatTimeSlot(timeSlot: Pair<LocalTime, LocalTime>): String {
    val formatter = DateTimeFormatter.ofPattern("a h:mm")
    val startFormatted = timeSlot.first.format(formatter)
    val endFormatted = timeSlot.second.format(formatter)
    return "$startFormatted - ${endFormatted.split(" ")[1]}"
}
