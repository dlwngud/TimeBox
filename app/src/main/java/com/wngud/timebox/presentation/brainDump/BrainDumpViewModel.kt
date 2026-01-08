package com.wngud.timebox.presentation.brainDump

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wngud.timebox.data.local.BrainDumpEntity
import com.wngud.timebox.data.local.toBrainDumpItem
import com.wngud.timebox.domain.repository.BrainDumpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val error: String? = null
)

// ------------------------------------------------------------------------
// 2. Intent Definition (사용자 액션 정의)
// ------------------------------------------------------------------------
sealed class BrainDumpIntent {
    data class InputTextChanged(val newText: String) : BrainDumpIntent()
    data object SendClick : BrainDumpIntent()
    data class DeleteItem(val id: Long) : BrainDumpIntent()
    data object ClearAllItems : BrainDumpIntent()
}

@HiltViewModel
class BrainDumpViewModel @Inject constructor(
    private val repository: BrainDumpRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrainDumpUiState())
    val uiState: StateFlow<BrainDumpUiState> = _uiState.asStateFlow()

    init {
        collectBrainDumpItems()
    }

    private fun collectBrainDumpItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.getBrainDumpItems().collect { entities ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            items = entities.map { it.toBrainDumpItem() }, // Entity에서 UI Model로 매핑
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
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
                        } catch (e: Exception) {
                            _uiState.update { it.copy(error = e.localizedMessage) }
                        }
                    }
                }
            }
            is BrainDumpIntent.DeleteItem -> {
                viewModelScope.launch {
                    try {
                        repository.deleteBrainDumpItem(intent.id)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.localizedMessage) }
                    }
                }
            }
            BrainDumpIntent.ClearAllItems -> {
                viewModelScope.launch {
                    try {
                        repository.deleteAllBrainDumpItems()
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.localizedMessage) }
                    }
                }
            }
        }
    }
}
