package com.zhoulesin.whyme.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.data.local.AppInitializer
import com.zhoulesin.whyme.domain.model.DailyGoal
import com.zhoulesin.whyme.domain.model.UserStats
import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.domain.model.WordLevel
import com.zhoulesin.whyme.domain.usecase.GetDailyGoalUseCase
import com.zhoulesin.whyme.domain.usecase.GetUserStatsUseCase
import com.zhoulesin.whyme.domain.usecase.GetWordsForLearningUseCase
import com.zhoulesin.whyme.domain.usecase.GetWordsForReviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
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
    val wordsForReview: List<Word> = emptyList(),
    val newWordsCount: Int = 0,
    val dailySentence: String = "Practice makes perfect! 熟能生巧！",
    val isLoading: Boolean = true,
    val wordDatabaseReady: Boolean = false,
    val currentLevel: WordLevel = WordLevel.DEFAULT
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val getDailyGoalUseCase: GetDailyGoalUseCase,
    private val getWordsForLearningUseCase: GetWordsForLearningUseCase,
    private val getWordsForReviewUseCase: GetWordsForReviewUseCase,
    private val appInitializer: AppInitializer
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        appInitializer.initializeIfNeeded()
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                getWordsForReviewUseCase(level = WordLevel.CET6),
                getWordsForLearningUseCase(level = WordLevel.CET6)
            ) { reviewWords, learningWords ->
                reviewWords to learningWords
            }.collect { (reviewWords, learningWords) ->
                _uiState.update {
                    it.copy(
                        currentLevel = WordLevel.CET6,
                        wordsForReview = reviewWords,
                        newWordsCount = learningWords.size
                    )
                }
            }
        }

        viewModelScope.launch {
            combine(
                getUserStatsUseCase(),
                getDailyGoalUseCase()
            ) { stats, goal ->
                Pair(stats, goal)
            }.collect { (stats, goal) ->
                _uiState.update { state ->
                    state.copy(
                        userStats = stats,
                        dailyGoal = goal,
                        isLoading = false,
                        wordDatabaseReady = true
                    )
                }
            }
        }
    }

    fun refresh() {
        loadData()
    }
}
