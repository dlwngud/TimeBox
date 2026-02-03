package com.wngud.timebox.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wngud.timebox.presentation.brainDump.BrainDumpItem
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * BrainDump 아이템을 선택할 수 있는 BottomSheet (개선된 디자인)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainDumpItemSelectorBottomSheet(
    items: List<BrainDumpItem>,
    selectedTimeSlot: Pair<LocalTime, LocalTime>,
    onItemSelected: (BrainDumpItem) -> Unit,
    onDirectEntry: (String) -> Unit, // 직접 입력 시 호출되는 콜백 추가
    onDismiss: () -> Unit
) {
    var selectedItem by remember { mutableStateOf<BrainDumpItem?>(null) }
    var directEntryText by remember { mutableStateOf("") }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Column(modifier = Modifier.padding(bottom = 20.dp)) {
                Text(
                    text = "일정 추가",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimeSlot(selectedTimeSlot),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 직접 입력 영역 (추가됨)
            OutlinedTextField(
                value = directEntryText,
                onValueChange = { 
                    directEntryText = it
                    if (it.isNotBlank()) selectedItem = null // 텍스트 입력 시 리스트 선택 해제
                },
                placeholder = { 
                    Text(
                        text = "무엇을 할까요?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                ),
                singleLine = true
            )

            // 구분선 및 리스트 헤더 (추가됨)
            Text(
                text = "또는 BrainDump에서 가져오기",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 300.dp) // 입력창이 추가되어 최대 높이 소폭 조정
                ) {
                    items(items) { item ->
                        BrainDumpItemOptionCard(
                            item = item,
                            isSelected = selectedItem == item,
                            onClick = { 
                                selectedItem = item
                                directEntryText = "" // 리스트 선택 시 직접 입력 텍스트 초기화
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            // Confirm Button
            val isEnabled = directEntryText.isNotBlank() || selectedItem != null
            Button(
                onClick = {
                    if (directEntryText.isNotBlank()) {
                        onDirectEntry(directEntryText)
                    } else if (selectedItem != null) {
                        onItemSelected(selectedItem!!)
                    }
                },
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "확인",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )
            }
        }
    }
}

/**
 * BrainDump 아이템 선택 옵션 카드 (ThemeOptionItem 스타일)
 */
@Composable
private fun BrainDumpItemOptionCard(
    item: BrainDumpItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.background
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Big Three 표시
                if (item.isBigThree) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFC107).copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Big Three",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                // 아이템 내용
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.content,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 2
                    )
                    
                    if (item.formattedTimestamp.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.formattedTimestamp,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 선택 표시
            if (isSelected) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
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
