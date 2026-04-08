package com.zhoulesin.whyme.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.data.local.AppInitializer
import com.zhoulesin.whyme.domain.model.DailyGoal
import com.zhoulesin.whyme.domain.model.LearningRecord
import com.zhoulesin.whyme.domain.model.UserStats
import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.domain.model.WordLevel
import com.zhoulesin.whyme.domain.usecase.GetTodayRecordUseCase
import com.zhoulesin.whyme.domain.usecase.GetUserStatsUseCase
import com.zhoulesin.whyme.domain.usecase.GetDailyGoalUseCase
import com.zhoulesin.whyme.domain.usecase.GetWordsForLearningUseCase
import com.zhoulesin.whyme.domain.usecase.GetWordsForReviewUseCase
import com.zhoulesin.whyme.domain.repository.WordBankRepository
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
        todayAccuracy = 0f
    ),
    val dailyGoal: DailyGoal = DailyGoal(),
    val todayRecord: LearningRecord? = null,
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
    private val getTodayRecordUseCase: GetTodayRecordUseCase,
    private val getWordsForLearningUseCase: GetWordsForLearningUseCase,
    private val getWordsForReviewUseCase: GetWordsForReviewUseCase,
    private val wordBankRepository: WordBankRepository,
    private val appInitializer: AppInitializer
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // 应用启动时初始化词库
        appInitializer.initializeIfNeeded()
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            wordBankRepository.getCurrentLevel().flatMapLatest { currentLevel ->
                combine(
                    getWordsForReviewUseCase(),
                    getWordsForLearningUseCase(level = currentLevel)
                ) { reviewWords, learningWords ->
                    Triple(currentLevel, reviewWords, learningWords)
                }
            }.collect { (currentLevel, reviewWords, learningWords) ->
                _uiState.update {
                    it.copy(
                        currentLevel = currentLevel,
                        wordsForReview = reviewWords,
                        newWordsCount = learningWords.size
                    )
                }
            }
        }

        viewModelScope.launch {
            combine(
                getUserStatsUseCase(),
                getDailyGoalUseCase(),
                getTodayRecordUseCase()
            ) { stats, goal, record ->
                Triple(stats, goal, record)
            }.collect { (stats, goal, record) ->
                _uiState.update { state ->
                    state.copy(
                        userStats = stats,
                        dailyGoal = goal,
                        todayRecord = record,
                        isLoading = false,
                        wordDatabaseReady = true
                    )
                }
            }
        }
    }

    /**
     * 刷新数据（当从学习页面返回时调用）
     */
    fun refresh() {
        loadData()
    }
}
