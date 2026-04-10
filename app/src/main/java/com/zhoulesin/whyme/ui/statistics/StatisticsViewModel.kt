package com.zhoulesin.whyme.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.domain.model.DailyGoal
import com.zhoulesin.whyme.domain.model.UserStats
import com.zhoulesin.whyme.domain.repository.WordRepository
import com.zhoulesin.whyme.domain.usecase.GetUserStatsUseCase
import com.zhoulesin.whyme.domain.usecase.GetDailyGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatisticsUiState(
    val totalWordsLearned: Int = 0,
    val totalWordsReviewed: Int = 0,
    val totalCorrect: Int = 0,
    val totalQuestions: Int = 0,
    val totalMinutes: Long = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val todayWordsLearned: Int = 0,
    val todayWordsReviewed: Int = 0,
    val todayAccuracy: Float = 0f,
    val todayMinutes: Long = 0,
    val dailyGoal: DailyGoal = DailyGoal(),
    val goalProgress: Float = 0f,
    val weeklyRecords: List<DailyRecord> = emptyList(),
    val totalWords: Int = 0,
    val masteredWords: Int = 0,
    val learningWords: Int = 0,
    val unknownWords: Int = 0,
    val masteryRate: Float = 0f,
    val isLoading: Boolean = true
)

data class DailyRecord(
    val dayLabel: String,
    val wordsLearned: Int = 0,
    val wordsReviewed: Int = 0,
    val accuracy: Float = 0f
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val getDailyGoalUseCase: GetDailyGoalUseCase,
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            combine(
                getUserStatsUseCase(),
                getDailyGoalUseCase()
            ) { userStats, goal -> Pair(userStats, goal) }.collect { (userStats, goal) ->
                val todayTotal = userStats.todayWordsLearned + userStats.todayWordsReviewed
                val goalTarget = goal.wordsPerDay + goal.reviewPerDay
                val goalProgress = if (goalTarget > 0) (todayTotal.toFloat() / goalTarget).coerceAtMost(1f) else 0f

                val totalWords = wordRepository.getWordCount()
                val mastered = wordRepository.getMasteredWordCount()
                val learning = wordRepository.getLearningWordCount()
                val unknown = wordRepository.getUnknownWordCount()
                val masteryRate = if (totalWords > 0) mastered.toFloat() / totalWords else 0f

                val weeklyData = (0..6).map { daysAgo ->
                    val dayLabel = when (daysAgo) {
                        0 -> "今天"
                        1 -> "昨天"
                        2 -> "前天"
                        else -> "${daysAgo}天前"
                    }
                    DailyRecord(dayLabel = dayLabel)
                }.reversed()

                _uiState.value = StatisticsUiState(
                    totalWordsLearned = userStats.totalWordsLearned,
                    totalWordsReviewed = userStats.totalWordsReviewed,
                    currentStreak = userStats.currentStreak,
                    longestStreak = userStats.longestStreak,
                    todayWordsLearned = userStats.todayWordsLearned,
                    todayWordsReviewed = userStats.todayWordsReviewed,
                    todayAccuracy = userStats.todayAccuracy,
                    dailyGoal = goal,
                    goalProgress = goalProgress,
                    weeklyRecords = weeklyData,
                    totalWords = totalWords,
                    masteredWords = mastered,
                    learningWords = learning,
                    unknownWords = unknown,
                    masteryRate = masteryRate,
                    isLoading = false
                )
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadStatistics()
    }
}
