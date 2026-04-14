package com.zhoulesin.whyme.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.domain.model.DailyGoal
import com.zhoulesin.whyme.domain.usecase.GetDailyGoalUseCase
import com.zhoulesin.whyme.domain.usecase.UpdateDailyGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val learnPerDayInput: String = "",
    val reviewPerDayInput: String = "",
    val testPerDayInput: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getDailyGoalUseCase: GetDailyGoalUseCase,
    private val updateDailyGoalUseCase: UpdateDailyGoalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeDailyGoal()
    }

    private fun observeDailyGoal() {
        viewModelScope.launch {
            getDailyGoalUseCase().collect { goal ->
                _uiState.update { state ->
                    state.copy(
                        learnPerDayInput = goal.wordsPerDay.toString(),
                        reviewPerDayInput = goal.reviewPerDay.toString(),
                        testPerDayInput = goal.testsPerDay.toString(),
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun onLearnInputChanged(value: String) {
        _uiState.update { it.copy(learnPerDayInput = value.filter(Char::isDigit), saveSuccess = false) }
    }

    fun onReviewInputChanged(value: String) {
        _uiState.update { it.copy(reviewPerDayInput = value.filter(Char::isDigit), saveSuccess = false) }
    }

    fun onTestInputChanged(value: String) {
        _uiState.update { it.copy(testPerDayInput = value.filter(Char::isDigit), saveSuccess = false) }
    }

    fun save() {
        val learn = _uiState.value.learnPerDayInput.toIntOrNull()
        val review = _uiState.value.reviewPerDayInput.toIntOrNull()
        val test = _uiState.value.testPerDayInput.toIntOrNull()

        if (learn == null || review == null || test == null || learn <= 0 || review <= 0 || test <= 0) {
            _uiState.update { it.copy(errorMessage = "请输入大于 0 的整数") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveSuccess = false, errorMessage = null) }
            updateDailyGoalUseCase(
                DailyGoal(
                    wordsPerDay = learn,
                    reviewPerDay = review,
                    testsPerDay = test
                )
            )
            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    fun consumeSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
