package com.wngud.timebox.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wngud.timebox.data.local.toBrainDumpItem
import com.wngud.timebox.data.modal.EventColorType
import com.wngud.timebox.data.modal.ScheduleSlot
import com.wngud.timebox.data.modal.Task
import com.wngud.timebox.domain.repository.BrainDumpRepository
import com.wngud.timebox.domain.repository.ScheduleRepository
import com.wngud.timebox.presentation.brainDump.BrainDumpItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val brainDumpRepository: BrainDumpRepository,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {
    
    private val _bigThreeTasks = MutableStateFlow<List<Task>>(emptyList())
    val bigThreeTasks: StateFlow<List<Task>> = _bigThreeTasks.asStateFlow()
    
    // 스케줄 슬롯 (타임라인에 배치된 일정)
    private val _scheduleSlots = MutableStateFlow<List<ScheduleSlot>>(emptyList())
    val scheduleSlots: StateFlow<List<ScheduleSlot>> = _scheduleSlots.asStateFlow()
    
    // 선택된 시간 슬롯 (클릭한 30분 단위 시간대)
    private val _selectedTimeSlot = MutableStateFlow<Pair<LocalTime, LocalTime>?>(null)
    val selectedTimeSlot: StateFlow<Pair<LocalTime, LocalTime>?> = _selectedTimeSlot.asStateFlow()
    
    // BottomSheet 표시 여부
    private val _showBrainDumpSelector = MutableStateFlow(false)
    val showBrainDumpSelector: StateFlow<Boolean> = _showBrainDumpSelector.asStateFlow()
    
    // 배치 가능한 BrainDump 아이템 (이미 스케줄에 배치되지 않은 것)
    private val _availableBrainDumpItems = MutableStateFlow<List<BrainDumpItem>>(emptyList())
    val availableBrainDumpItems: StateFlow<List<BrainDumpItem>> = _availableBrainDumpItems.asStateFlow()
    
    init {
        collectBigThreeTasks()
        collectScheduleSlots()
        collectAvailableBrainDumpItems()
    }
    
    private fun collectBigThreeTasks() {
        viewModelScope.launch {
            brainDumpRepository.getBigThreeItems().collect { entities ->
                _bigThreeTasks.value = entities.map { entity ->
                    Task(
                        id = entity.id.toString(),
                        title = entity.content,
                        isCompleted = false,
                        brainDumpId = entity.id
                    )
                }
            }
        }
    }
    
    private fun collectScheduleSlots() {
        viewModelScope.launch {
            scheduleRepository.getScheduleSlotsForDate(LocalDate.now()).collect { slots ->
                _scheduleSlots.value = slots
            }
        }
    }
    
    private fun collectAvailableBrainDumpItems() {
        viewModelScope.launch {
            // BrainDump 아이템과 스케줄 슬롯을 결합하여 아직 배치되지 않은 아이템만 필터링
            combine(
                brainDumpRepository.getBrainDumpItems(),
                scheduleRepository.getScheduleSlotsForDate(LocalDate.now())
            ) { brainDumpEntities, scheduleSlots ->
                val scheduledItemIds = scheduleSlots.map { it.brainDumpItemId }.toSet()
                brainDumpEntities
                    .filter { it.id !in scheduledItemIds }
                    .map { it.toBrainDumpItem() }
            }.collect { availableItems ->
                _availableBrainDumpItems.value = availableItems
            }
        }
    }
    
    fun toggleTaskCompletion(taskId: String) {
        _bigThreeTasks.update { tasks ->
            tasks.map { task ->
                if (task.id == taskId) {
                    task.copy(isCompleted = !task.isCompleted)
                } else {
                    task
                }
            }
        }
    }
    
    /**
     * 30분 시간 슬롯 클릭 처리
     */
    fun onTimeSlotClick(startTime: LocalTime) {
        viewModelScope.launch {
            val endTime = startTime.plusMinutes(30)
            
            // 해당 시간대가 비어있는지 확인
            val isAvailable = scheduleRepository.isTimeSlotAvailable(
                startTime = startTime,
                endTime = endTime,
                date = LocalDate.now()
            )
            
            if (isAvailable) {
                _selectedTimeSlot.value = Pair(startTime, endTime)
                _showBrainDumpSelector.value = true
            }
        }
    }
    
    /**
     * BrainDump 아이템을 타임라인에 배치
     */
    fun placeBrainDumpItem(item: BrainDumpItem, startTime: LocalTime, durationMinutes: Int = 30) {
        viewModelScope.launch {
            val endTime = startTime.plusMinutes(durationMinutes.toLong())
            
            // 시간대 중복 확인
            val isAvailable = scheduleRepository.isTimeSlotAvailable(
                startTime = startTime,
                endTime = endTime,
                date = LocalDate.now()
            )
            
            if (isAvailable) {
                val scheduleSlot = ScheduleSlot(
                    brainDumpItemId = item.id,
                    title = item.content,
                    startTime = startTime,
                    endTime = endTime,
                    colorType = if (item.isBigThree) EventColorType.BLUE else EventColorType.GREEN,
                    date = LocalDate.now()
                )
                
                scheduleRepository.insertScheduleSlot(scheduleSlot)
                
                // BottomSheet 닫기
                dismissBrainDumpSelector()
            }
        }
    }
    
    /**
     * 스케줄 슬롯 제거
     */
    fun removeScheduleSlot(slotId: String) {
        viewModelScope.launch {
            scheduleRepository.deleteScheduleSlot(slotId)
        }
    }
    
    /**
     * 스케줄 슬롯 이동 (드래그앤드롭용)
     */
    fun moveScheduleSlot(slot: ScheduleSlot, newStartTime: LocalTime) {
        viewModelScope.launch {
            val duration = java.time.Duration.between(slot.startTime, slot.endTime)
            val newEndTime = newStartTime.plus(duration)
            
            // 새 위치가 사용 가능한지 확인 (자기 자신 제외)
            val isAvailable = scheduleRepository.isTimeSlotAvailable(
                startTime = newStartTime,
                endTime = newEndTime,
                date = LocalDate.now()
            )
            
            if (isAvailable) {
                val updatedSlot = slot.copy(
                    startTime = newStartTime,
                    endTime = newEndTime
                )
                scheduleRepository.updateScheduleSlot(updatedSlot)
            }
        }
    }
    
    /**
     * BottomSheet 닫기
     */
    fun dismissBrainDumpSelector() {
        _showBrainDumpSelector.value = false
        _selectedTimeSlot.value = null
    }
}
