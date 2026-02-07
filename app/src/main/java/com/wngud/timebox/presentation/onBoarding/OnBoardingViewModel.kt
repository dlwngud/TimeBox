package com.wngud.timebox.presentation.onBoarding

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// ------------------------------------------------------------------------
// 온보딩 전용 데이터 모델 (UI Layer)
// ------------------------------------------------------------------------
data class OnBoardingItem(
    val id: Int,
    val title: String,
    val category: String = "사용자 입력",
    val duration: String = "방금 전",
    var isChecked: Boolean = false,
    var isAiRecommended: Boolean = false,
    val isUserInput: Boolean = false // 사용자가 직접 입력한 항목인지 구분
)

// ------------------------------------------------------------------------
// UI 상태 정의
// ------------------------------------------------------------------------
data class OnBoardingUiState(
    val inputText: String = "",
    val userInputItems: List<OnBoardingItem> = emptyList(), // 사용자가 입력한 항목들
    val selectedItemIds: Set<Int> = emptySet(), // Big Three로 선택된 항목 ID
    val canProceedToPhase2: Boolean = false, // Phase 2로 진행 가능 여부
    val currentPage: Int = 0 // 현재 온보딩 페이지 (0: Phase1, 1: Phase2, 2: Phase3)
)

// ------------------------------------------------------------------------
// 사용자 액션 정의
// ------------------------------------------------------------------------
sealed class OnBoardingIntent {
    data class UpdateInputText(val text: String) : OnBoardingIntent()
    data object AddItem : OnBoardingIntent()
    data class ToggleSelection(val itemId: Int) : OnBoardingIntent()
    data object GoNext : OnBoardingIntent()
    data object GoBack : OnBoardingIntent()
    data object Complete : OnBoardingIntent()
}

@HiltViewModel
class OnBoardingViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(OnBoardingUiState())
    val uiState: StateFlow<OnBoardingUiState> = _uiState.asStateFlow()

    private var nextUserId = 1000 // 사용자 입력 항목의 ID는 1000부터 시작

    init {
        // Phase 2에서 AI 추천 항목 미리 설정
        initializeAiRecommendations()
    }

    private fun initializeAiRecommendations() {
        _uiState.update { currentState ->
            // 사용자 입력 항목 중 첫 3개를 AI 추천으로 설정
            val updatedUserItems = currentState.userInputItems.mapIndexed { index, item ->
                if (index < 3) item.copy(isAiRecommended = true) else item
            }
            currentState.copy(userInputItems = updatedUserItems)
        }
    }

    fun processIntent(intent: OnBoardingIntent) {
        when (intent) {
            is OnBoardingIntent.UpdateInputText -> {
                _uiState.update { it.copy(inputText = intent.text) }
            }

            OnBoardingIntent.AddItem -> {
                val currentText = _uiState.value.inputText.trim()
                if (currentText.isNotBlank()) {
                    val newItem = OnBoardingItem(
                        id = nextUserId++,
                        title = currentText,
                        category = "사용자 입력",
                        duration = "방금 전",
                        isUserInput = true,
                        isAiRecommended = false // 처음엔 false, Phase 2 진입 시 첫 번째 항목을 추천으로 설정
                    )

                    _uiState.update { currentState ->
                        val updatedUserItems = currentState.userInputItems + newItem
                        
                        // 사용자 입력 항목 중 첫 번째를 AI 추천으로 설정
                        val updatedUserItemsWithAi = updatedUserItems.mapIndexed { index, item ->
                            if (index == 0) item.copy(isAiRecommended = true) else item
                        }
                        
                        currentState.copy(
                            userInputItems = updatedUserItemsWithAi,
                            inputText = "",
                            canProceedToPhase2 = updatedUserItems.isNotEmpty() // 최소 1개 입력 시 진행 가능
                        )
                    }
                }
            }

            is OnBoardingIntent.ToggleSelection -> {
                _uiState.update { currentState ->
                    val currentSelected = currentState.selectedItemIds
                    val newSelected = if (currentSelected.contains(intent.itemId)) {
                        currentSelected - intent.itemId
                    } else {
                        if (currentSelected.size < 3) {
                            currentSelected + intent.itemId
                        } else {
                            currentSelected // 이미 3개 선택됨
                        }
                    }
                    currentState.copy(selectedItemIds = newSelected)
                }
            }

            OnBoardingIntent.GoNext -> {
                _uiState.update { currentState ->
                    val nextPage = (currentState.currentPage + 1).coerceIn(0, 2)
                    currentState.copy(currentPage = nextPage)
                }
            }

            OnBoardingIntent.GoBack -> {
                _uiState.update { currentState ->
                    val previousPage = (currentState.currentPage - 1).coerceIn(0, 2)
                    currentState.copy(currentPage = previousPage)
                }
            }

            OnBoardingIntent.Complete -> {
                // 온보딩 완료 시 필요한 로직 (현재는 비어있음)
                // 실제로는 여기서 데이터베이스에 저장하거나 다른 작업을 수행할 수 있습니다
            }
        }
    }

    /**
     * Phase 2에서 표시할 전체 항목 리스트 (AI 추천 항목이 최상단에 배치됨)
     */
    fun getCombinedItemsForPhase2(): List<OnBoardingItem> {
        val currentState = _uiState.value
        val allItems = currentState.userInputItems
        
        // AI 추천 항목을 최상단에 배치
        val aiRecommended = allItems.filter { it.isAiRecommended }
        val others = allItems.filter { !it.isAiRecommended }
        
        return aiRecommended + others
    }

    /**
     * Phase 1에서 표시할 항목 리스트 (사용자가 입력한 항목만)
     */
    fun getPhase1Items(): List<OnBoardingItem> {
        return _uiState.value.userInputItems
    }
}
