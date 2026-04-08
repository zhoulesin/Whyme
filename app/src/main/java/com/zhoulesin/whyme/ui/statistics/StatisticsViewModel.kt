package com.zhoulesin.whyme.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.domain.model.DailyGoal
import com.zhoulesin.whyme.domain.model.LearningRecord
import com.zhoulesin.whyme.domain.model.UserStats
import com.zhoulesin.whyme.domain.repository.LearningRepository
import com.zhoulesin.whyme.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 学习统计数据
 */
data class StatisticsUiState(
    // 总体统计
    val totalWordsLearned: Int = 0,
    val totalWordsReviewed: Int = 0,
    val totalCorrect: Int = 0,
    val totalQuestions: Int = 0,
    val totalMinutes: Long = 0,
    
    // 连续打卡
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    
    // 今日统计
    val todayWordsLearned: Int = 0,
    val todayWordsReviewed: Int = 0,
    val todayAccuracy: Float = 0f,
    val todayMinutes: Long = 0,
    
    // 每日目标
    val dailyGoal: DailyGoal = DailyGoal(),
    val goalProgress: Float = 0f, // 目标完成百分比
    
    // 本周数据
    val weeklyRecords: List<DailyRecord> = emptyList(),
    
    // 单词统计
    val totalWords: Int = 0,
    val masteredWords: Int = 0,
    val learningWords: Int = 0,
    val unknownWords: Int = 0,
    
    // 掌握率
    val masteryRate: Float = 0f,
    
    val isLoading: Boolean = true
)

/**
 * 每日学习记录（用于图表）
 */
data class DailyRecord(
    val date: LocalDate,
    val dateText: String, // 格式化后的日期文本，如 "周一"
    val wordsLearned: Int = 0,
    val wordsReviewed: Int = 0,
    val accuracy: Float = 0f
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val learningRepository: LearningRepository,
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            // 加载学习相关数据
            combine(
                learningRepository.getUserStats(),
                learningRepository.getDailyGoal(),
                learningRepository.getRecordsBetween(
                    LocalDate.now().minusDays(6),
                    LocalDate.now()
                )
            ) { userStats, goal, weeklyRecords ->
                Triple(userStats, goal, weeklyRecords)
            }.collect { (userStats, goal, weeklyRecords) ->
                // 计算本周每日记录
                val dayFormatter = DateTimeFormatter.ofPattern("E")
                val weeklyData = (0..6).map { daysAgo ->
                    val date = LocalDate.now().minusDays(daysAgo.toLong())
                    val record = weeklyRecords.find { it.date == date }
                    DailyRecord(
                        date = date,
                        dateText = when (daysAgo) {
                            0 -> "今天"
                            1 -> "昨天"
                            else -> date.format(dayFormatter)
                        },
                        wordsLearned = record?.wordsLearned ?: 0,
                        wordsReviewed = record?.wordsReviewed ?: 0,
                        accuracy = record?.accuracy ?: 0f
                    )
                }.reversed()
                
                // 计算今日目标进度
                val todayTotal = (userStats.todayWordsLearned + userStats.todayWordsReviewed)
                val goalTarget = goal.wordsPerDay + goal.reviewPerDay
                val goalProgress = if (goalTarget > 0) (todayTotal.toFloat() / goalTarget).coerceAtMost(1f) else 0f
                
                // 计算总正确率
                val totalQuestions = userStats.totalWordsLearned + userStats.totalWordsReviewed
                val totalCorrect = userStats.todayAccuracy * (userStats.todayWordsLearned + userStats.todayWordsReviewed)
                
                // 加载单词统计数据
                val totalWords = wordRepository.getWordCount()
                val mastered = wordRepository.getMasteredWordCount()
                val learning = wordRepository.getLearningWordCount()
                val unknown = wordRepository.getUnknownWordCount()
                val masteryRate = if (totalWords > 0) mastered.toFloat() / totalWords else 0f
                
                _uiState.value = StatisticsUiState(
                    totalWordsLearned = userStats.totalWordsLearned,
                    totalWordsReviewed = userStats.totalWordsReviewed,
                    totalCorrect = totalCorrect.toInt(),
                    totalQuestions = totalQuestions,
                    totalMinutes = userStats.totalLearningMinutes,
                    currentStreak = userStats.currentStreak,
                    longestStreak = userStats.longestStreak,
                    todayWordsLearned = userStats.todayWordsLearned,
                    todayWordsReviewed = userStats.todayWordsReviewed,
                    todayAccuracy = userStats.todayAccuracy,
                    todayMinutes = userStats.totalLearningMinutes % 60,
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
