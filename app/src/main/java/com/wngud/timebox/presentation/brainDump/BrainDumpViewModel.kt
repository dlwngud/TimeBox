package com.wngud.timebox.presentation.brainDump

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wngud.timebox.data.local.BrainDumpEntity
import com.wngud.timebox.data.local.toBrainDumpItem
import com.wngud.timebox.domain.repository.BrainDumpRepository
import com.wngud.timebox.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ------------------------------------------------------------------------
// 1. State Definition (상태 정의) - ViewModel 내부
// ------------------------------------------------------------------------
data class BrainDumpUiState(
    val items: List<BrainDumpItem> = emptyList(), // Room에서 로드될 실제 데이터
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val editingItemId: Long? = null, // 현재 수정 중인 아이템의 ID (null이면 수정 중 아님)
    val editingInputText: String = "", // 수정 다이얼로그의 입력 텍스트
    val selectedBigThreeIds: Set<Long> = emptySet(), // Big Three로 선택된 아이템 ID 목록
    val lastDeletedItem: BrainDumpItem? = null // 마지막으로 삭제된 아이템 (복구용)
)

// ------------------------------------------------------------------------
// UI 단으로 전달될 단발성 이벤트 정의 (예: SnackBar 메시지, Navigation 이벤트)
// ------------------------------------------------------------------------
data class SnackbarEvent(
    val message: String,
    val actionLabel: String? = null,
    val deletedItem: BrainDumpItem? = null // 복구를 위해 삭제된 아이템 저장
)

// ------------------------------------------------------------------------
// 2. Intent Definition (사용자 액션 정의)
// ------------------------------------------------------------------------
sealed class BrainDumpIntent {
    data class InputTextChanged(val newText: String) : BrainDumpIntent()
    data object SendClick : BrainDumpIntent()
    data class DeleteItem(val id: Long) : BrainDumpIntent()
    data object ClearAllItems : BrainDumpIntent()
    data class StartEditItem(val item: BrainDumpItem) : BrainDumpIntent() // 수정 시작
    data class EditInputTextChanged(val newText: String) : BrainDumpIntent() // 수정 중 텍스트 변경
    data object SaveEditedItem : BrainDumpIntent() // 수정 저장
    data object CancelEditItem : BrainDumpIntent() // 수정 취소
    data class ToggleBigThree(val id: Long) : BrainDumpIntent() // Big Three 토글
    data object UndoDeleteItem : BrainDumpIntent() // 삭제 복구
}

@HiltViewModel
class BrainDumpViewModel @Inject constructor(
    private val repository: BrainDumpRepository,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrainDumpUiState())
    val uiState: StateFlow<BrainDumpUiState> = _uiState.asStateFlow()

    // 단발성 UI 이벤트를 위한 Channel
    private val _snackbarEvent = Channel<SnackbarEvent>(Channel.BUFFERED)
    val snackbarEvent = _snackbarEvent.receiveAsFlow()

    init {
        print("BrainDumpViewModel: ViewModel initialized!") // ViewModel 생성 확인 로그
        collectBrainDumpItems()
    }

    private fun collectBrainDumpItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.getBrainDumpItems().collect { entities ->
                    val items = entities.map { it.toBrainDumpItem() }
                    _uiState.update { currentState ->
                        currentState.copy(
                            items = items,
                            isLoading = false,
                            error = null,
                            selectedBigThreeIds = items.filter { it.isBigThree }.map { it.id }.toSet()
                        )
                    }
                    print("BrainDumpViewModel: Items collected. Count: ${items.size}")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                print("BrainDumpViewModel: Error collecting items: ${e.localizedMessage}")
            }
        }
    }

    // ------------------------------------------------------------------------
    // 3. Intent 처리 (로직 처리)
    // ------------------------------------------------------------------------
    fun processIntent(intent: BrainDumpIntent) {
        when (intent) {
            is BrainDumpIntent.InputTextChanged -> {
                _uiState.update { it.copy(inputText = intent.newText) }
            }
            BrainDumpIntent.SendClick -> {
                if (_uiState.value.inputText.isNotBlank()) {
                    val newItemContent = _uiState.value.inputText
                    viewModelScope.launch {
                        try {
                            repository.insertBrainDumpItem(BrainDumpEntity(content = newItemContent))
                            _uiState.update { it.copy(inputText = "") } // 입력창 초기화
                            print("BrainDumpViewModel: Item inserted successfully: $newItemContent")
                        } catch (e: Exception) {
                            _uiState.update { it.copy(error = e.localizedMessage) }
                            print("BrainDumpViewModel: Error inserting item: ${e.localizedMessage}")
                        }
                    }
                }
            }
            is BrainDumpIntent.DeleteItem -> {
                val deletedItemId = intent.id
                viewModelScope.launch {
                    // 먼저 삭제될 아이템을 찾아서 저장
                    val itemToDelete = _uiState.value.items.find { it.id == deletedItemId }
                    itemToDelete?.let { item ->
                        // BrainDumpItem에 timestamp가 있으므로, 이 정보를 저장
                        _uiState.update { it.copy(lastDeletedItem = item) }
                        try {
                            repository.deleteBrainDumpItem(deletedItemId)
                            print("BrainDumpViewModel: Item deleted successfully: ${item.id}")
                            // 스낵바 이벤트를 발행
                            _snackbarEvent.send(SnackbarEvent(
                                message = "'${item.content}'이(가) 삭제되었습니다.",
                                actionLabel = "복구",
                                deletedItem = item // 원본 아이템 객체 전달
                            ))
                        } catch (e: Exception) {
                            _uiState.update { it.copy(error = e.localizedMessage) }
                            print("BrainDumpViewModel: Error deleting item: ${e.localizedMessage}")
                        }
                    }
                }
            }
            BrainDumpIntent.UndoDeleteItem -> {
                viewModelScope.launch {
                    _uiState.value.lastDeletedItem?.let { itemToRestore ->
                        try {
                            // 삭제되기 전의 원래 id, content, timestamp, isBigThree 상태를 모두 사용하여 복구
                            repository.insertBrainDumpItem(BrainDumpEntity(
                                id = itemToRestore.id,
                                content = itemToRestore.content,
                                timestamp = itemToRestore.timestamp, // BrainDumpItem의 timestamp를 직접 사용
                                isBigThree = itemToRestore.isBigThree
                            ))
                            _uiState.update { it.copy(lastDeletedItem = null) } // 복구 후 lastDeletedItem 초기화
                            print("BrainDumpViewModel: Item restored successfully: ${itemToRestore.id}")
                        } catch (e: Exception) {
                            _uiState.update { it.copy(error = e.localizedMessage) }
                            print("BrainDumpViewModel: Error restoring item: ${e.localizedMessage}")
                        }
                    }
                }
            }
            BrainDumpIntent.ClearAllItems -> {
                viewModelScope.launch {
                    try {
                        repository.deleteAllBrainDumpItems()
                        print("BrainDumpViewModel: All items cleared successfully")
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.localizedMessage) }
                        print("BrainDumpViewModel: Error clearing all items: ${e.localizedMessage}")
                    }
                }
            }
            is BrainDumpIntent.StartEditItem -> {
                _uiState.update {
                    it.copy(
                        editingItemId = intent.item.id,
                        editingInputText = intent.item.content // 기존 내용을 수정 다이얼로그에 미리 채움
                    )
                }
            }
            is BrainDumpIntent.EditInputTextChanged -> {
                _uiState.update { it.copy(editingInputText = intent.newText) }
            }
            BrainDumpIntent.SaveEditedItem -> {
                _uiState.value.editingItemId?.let { id ->
                    if (_uiState.value.editingInputText.isNotBlank()) {
                        val editedContent = _uiState.value.editingInputText
                        viewModelScope.launch {
                            try {
                                // 기존 아이템을 찾아서 timestamp와 isBigThree 유지
                                val originalItem = _uiState.value.items.find { it.id == id }
                                if (originalItem != null) {
                                    repository.updateBrainDumpItem(
                                        BrainDumpEntity(
                                            id = id,
                                            content = editedContent,
                                            timestamp = originalItem.timestamp, // BrainDumpItem의 timestamp를 직접 사용
                                            isBigThree = originalItem.isBigThree
                                        )
                                    )
                                    print("BrainDumpViewModel: Item updated successfully: $id - $editedContent")
                                } else {
                                    // TODO: originalItem을 찾지 못한 경우 오류 처리
                                    _uiState.update { it.copy(error = "원본 아이템을 찾을 수 없습니다.") }
                                    print("BrainDumpViewModel: Error: original item not found for ID: $id")
                                }
                                _uiState.update { 
                                    it.copy(editingItemId = null, editingInputText = "") // 수정 모드 종료
                                } // 업데이트 후 UI 상태를 갱신하기 위해 한 번만 호출
                            } catch (e: Exception) {
                                _uiState.update { it.copy(error = e.localizedMessage) }
                                print("BrainDumpViewModel: Error saving edited item: ${e.localizedMessage}")
                            }
                        }
                    }
                }
            }
            BrainDumpIntent.CancelEditItem -> {
                _uiState.update { it.copy(editingItemId = null, editingInputText = "") } // 수정 모드 종료
            }
            is BrainDumpIntent.ToggleBigThree -> {
                val itemId = intent.id
                val currentItem = _uiState.value.items.find { it.id == itemId }

                currentItem?.let { item ->
                    val newBigThreeState = !item.isBigThree

                    // Big Three 개수 제한 로직
                    val currentBigThreeCount = _uiState.value.items.count { it.isBigThree }

                    if (newBigThreeState && currentBigThreeCount >= 3) {
                        // 이미 3개가 선택되어 있고, 새로 선택하려고 하면 에러 처리 또는 무시
                        _uiState.update { it.copy(error = "Big Three는 최대 3개까지 선택할 수 있습니다.") }
                        print("BrainDumpViewModel: Big Three limit reached for item: $itemId")
                        return // 더 이상 진행하지 않음
                    }

                    viewModelScope.launch {
                        try {
                            // 기존 아이템의 timestamp 유지하고 isBigThree 상태만 변경
                            repository.updateBrainDumpItem(
                                BrainDumpEntity(
                                    id = item.id,
                                    content = item.content,
                                    timestamp = item.timestamp, // BrainDumpItem의 timestamp를 직접 사용
                                    isBigThree = newBigThreeState
                                )
                            )
                            
                            // 연결된 스케줄 슬롯의 색상도 업데이트
                            scheduleRepository.updateScheduleColorByBrainDumpId(item.id, newBigThreeState)
                            
                            print("BrainDumpViewModel: Big Three toggled successfully for item: $itemId to $newBigThreeState")
                            // UI 상태 업데이트는 collectBrainDumpItems()를 통해 자동으로 이루어질 것임
                        } catch (e: Exception) {
                            _uiState.update { it.copy(error = e.localizedMessage) }
                            print("BrainDumpViewModel: Error toggling Big Three for item: $itemId - ${e.localizedMessage}")
                        }
                    }
                }
            }
        }
    }
}
