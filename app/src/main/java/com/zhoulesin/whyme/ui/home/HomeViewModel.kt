package com.zhoulesin.whyme.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.data.local.AppInitializer
import com.zhoulesin.whyme.domain.model.DailyGoal
import com.zhoulesin.whyme.domain.model.LearningRecord
import com.zhoulesin.whyme.domain.model.UserStats
import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.domain.usecase.GetTodayRecordUseCase
import com.zhoulesin.whyme.domain.usecase.GetUserStatsUseCase
import com.zhoulesin.whyme.domain.usecase.GetDailyGoalUseCase
import com.zhoulesin.whyme.domain.usecase.GetWordsForReviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
        todayAccuracy = 0f
    ),
    val dailyGoal: DailyGoal = DailyGoal(),
    val todayRecord: LearningRecord? = null,
    val wordsForReview: List<Word> = emptyList(),
    val dailySentence: String = "Practice makes perfect! 熟能生巧！",
    val isLoading: Boolean = true,
    val wordDatabaseReady: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val getDailyGoalUseCase: GetDailyGoalUseCase,
    private val getTodayRecordUseCase: GetTodayRecordUseCase,
    private val getWordsForReviewUseCase: GetWordsForReviewUseCase,
    private val appInitializer: AppInitializer
) : ViewModel() {

    init {
        // 应用启动时初始化词库
        appInitializer.initializeIfNeeded()
    }

    val uiState: StateFlow<HomeUiState> = combine(
        getUserStatsUseCase(),
        getDailyGoalUseCase(),
        getTodayRecordUseCase(),
        getWordsForReviewUseCase()
    ) { stats, goal, record, reviewWords ->
        HomeUiState(
            userStats = stats,
            dailyGoal = goal,
            todayRecord = record,
            wordsForReview = reviewWords,
            isLoading = false,
            wordDatabaseReady = true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
}
