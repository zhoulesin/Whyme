package com.zhoulesin.whyme.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.domain.model.Achievement
import com.zhoulesin.whyme.domain.model.Achievements
import com.zhoulesin.whyme.domain.model.DailyGoal
import com.zhoulesin.whyme.domain.model.UserStats
import com.zhoulesin.whyme.domain.repository.WordRepository
import com.zhoulesin.whyme.domain.usecase.GetDailyGoalUseCase
import com.zhoulesin.whyme.domain.usecase.GetUserStatsUseCase
import com.zhoulesin.whyme.domain.usecase.UpdateDailyGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userStats: UserStats = UserStats(
        totalWordsLearned = 0,
        totalWordsReviewed = 0,
        currentStreak = 0,
        longestStreak = 0,
        totalLearningMinutes = 0,
        todayWordsLearned = 0,
        todayWordsReviewed = 0,
        todayTests = 0,
        todayTestAccuracy = 0f,
        todayLearningMinutes = 0,
        todayAccuracy = 0f
    ),
    val dailyGoal: DailyGoal = DailyGoal(),
    val totalWords: Int = 0,
    val masteredWords: Int = 0,
    val achievements: List<Achievement> = Achievements.ALL_ACHIEVEMENTS,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val getDailyGoalUseCase: GetDailyGoalUseCase,
    private val updateDailyGoalUseCase: UpdateDailyGoalUseCase,
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _masteredWords = MutableStateFlow(0)
    val masteredWords: StateFlow<Int> = _masteredWords.asStateFlow()

    val uiState: StateFlow<ProfileUiState> = combine(
        getUserStatsUseCase(),
        getDailyGoalUseCase()
    ) { stats, goal ->
        ProfileUiState(
            userStats = stats,
            dailyGoal = goal,
            masteredWords = _masteredWords.value,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    init {
        loadWordCounts()
    }

    private fun loadWordCounts() {
        viewModelScope.launch {
            _masteredWords.value = wordRepository.getMasteredWordCount()
        }
    }

    fun updateDailyGoal(goal: DailyGoal) {
        viewModelScope.launch {
            updateDailyGoalUseCase(goal)
        }
    }
}
