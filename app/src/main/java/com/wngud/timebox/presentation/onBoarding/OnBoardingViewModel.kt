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
    val userInputText: String = "",
    val userInputItems: List<OnBoardingItem> = emptyList(), // 사용자가 입력한 항목들
    val sampleItems: List<OnBoardingItem> = listOf(
        OnBoardingItem(1, "이번 주 주간 보고서 초안 작성하기", "업무", "1분 전", false, false),
        OnBoardingItem(2, "세탁소 들러서 거울 코트 찾아오기", "개인", "3분 전", false, false),
        OnBoardingItem(3, "안부 전화 드리기", "가족", "15분 전", false, false),
        OnBoardingItem(4, "어제 회의록 정리", "인프라", "1시간 전", false, false)
    ),
    val selectedBigThree: Set<Int> = emptySet(), // Big Three로 선택된 항목 ID
    val canProceedToPhase2: Boolean = false, // Phase 2로 진행 가능 여부
    val currentPage: Int = 0 // 현재 온보딩 페이지 (0: Phase1, 1: Phase2, 2: Phase3)
)

// ------------------------------------------------------------------------
// 사용자 액션 정의
// ------------------------------------------------------------------------
sealed class OnBoardingIntent {
    data class UpdateInputText(val text: String) : OnBoardingIntent()
    data object AddUserItem : OnBoardingIntent()
    data class ToggleBigThree(val itemId: Int) : OnBoardingIntent()
    data object NavigateToNextPage : OnBoardingIntent()
    data object NavigateToPreviousPage : OnBoardingIntent()
    data object ResetOnBoarding : OnBoardingIntent()
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
            val updatedSampleItems = currentState.sampleItems.mapIndexed { index, item ->
                // 첫 번째 예시 항목을 AI 추천으로 설정
                if (index == 0) item.copy(isAiRecommended = true) else item
            }
            currentState.copy(sampleItems = updatedSampleItems)
        }
    }

    fun processIntent(intent: OnBoardingIntent) {
        when (intent) {
            is OnBoardingIntent.UpdateInputText -> {
                _uiState.update { it.copy(userInputText = intent.text) }
            }

            OnBoardingIntent.AddUserItem -> {
                val currentText = _uiState.value.userInputText.trim()
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
                            userInputText = "",
                            canProceedToPhase2 = updatedUserItems.isNotEmpty() // 최소 1개 입력 시 진행 가능
                        )
                    }
                }
            }

            is OnBoardingIntent.ToggleBigThree -> {
                _uiState.update { currentState ->
                    val currentSelected = currentState.selectedBigThree
                    val newSelected = if (currentSelected.contains(intent.itemId)) {
                        currentSelected - intent.itemId
                    } else {
                        if (currentSelected.size < 3) {
                            currentSelected + intent.itemId
                        } else {
                            currentSelected // 이미 3개 선택됨
                        }
                    }
                    currentState.copy(selectedBigThree = newSelected)
                }
            }

            OnBoardingIntent.NavigateToNextPage -> {
                _uiState.update { currentState ->
                    val nextPage = (currentState.currentPage + 1).coerceIn(0, 2)
                    currentState.copy(currentPage = nextPage)
                }
            }

            OnBoardingIntent.NavigateToPreviousPage -> {
                _uiState.update { currentState ->
                    val previousPage = (currentState.currentPage - 1).coerceIn(0, 2)
                    currentState.copy(currentPage = previousPage)
                }
            }

            OnBoardingIntent.ResetOnBoarding -> {
                _uiState.value = OnBoardingUiState()
                nextUserId = 1000
                initializeAiRecommendations()
            }
        }
    }

    /**
     * Phase 2에서 표시할 전체 항목 리스트 (AI 추천 항목이 최상단에 배치됨)
     */
    fun getCombinedItemsForPhase2(): List<OnBoardingItem> {
        val currentState = _uiState.value
        val allItems = currentState.userInputItems + currentState.sampleItems
        
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
