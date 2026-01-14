package com.wngud.timebox.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wngud.timebox.data.modal.Task
import com.wngud.timebox.domain.repository.BrainDumpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val brainDumpRepository: BrainDumpRepository
) : ViewModel() {
    
    private val _bigThreeTasks = MutableStateFlow<List<Task>>(emptyList())
    val bigThreeTasks: StateFlow<List<Task>> = _bigThreeTasks.asStateFlow()
    
    init {
        collectBigThreeTasks()
    }
    
    private fun collectBigThreeTasks() {
        viewModelScope.launch {
            brainDumpRepository.getBigThreeItems().collect { entities ->
                _bigThreeTasks.value = entities.map { entity ->
                    Task(
                        id = entity.id.toString(),
                        title = entity.content,
                        isCompleted = false,  // 초기값 (추후 별도 저장 가능)
                        brainDumpId = entity.id
                    )
                }
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
}