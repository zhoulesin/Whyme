package com.zhoulesin.whyme.domain.usecase

import com.zhoulesin.whyme.data.local.PreferencesDataStore
import com.zhoulesin.whyme.domain.model.DailyGoal
import com.zhoulesin.whyme.domain.model.UserStats
import com.zhoulesin.whyme.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserStatsUseCase @Inject constructor(
    private val wordRepository: WordRepository,
    private val preferencesDataStore: PreferencesDataStore
) {
    operator fun invoke(): Flow<UserStats> = kotlinx.coroutines.flow.combine(
        preferencesDataStore.dailyGoal,
        preferencesDataStore.currentStreak,
        preferencesDataStore.longestStreak
    ) { goal, currentStreak, longestStreak ->
        val totalLearned = wordRepository.getTotalLearnedWordCount()
        val mastered = wordRepository.getMasteredWordCount()
        val todayNew = wordRepository.getTodayNewWordsCount()
        val todayReview = wordRepository.getTodayReviewCount()
        val todayTests = wordRepository.getTodayTestCount()
        val todayTestAccuracy = wordRepository.getTodayTestAccuracy()
        val todayLearningMinutes = wordRepository.getTodayLearningMinutes()

        UserStats(
            totalWordsLearned = totalLearned,
            totalWordsReviewed = wordRepository.getTotalReviewCount(),
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalLearningMinutes = 0,
            todayWordsLearned = todayNew,
            todayWordsReviewed = todayReview,
            todayTests = todayTests,
            todayTestAccuracy = todayTestAccuracy,
            todayLearningMinutes = todayLearningMinutes,
            todayAccuracy = if (todayReview > 0) mastered.toFloat() / todayReview else 0f
        )
    }
}

class GetDailyGoalUseCase @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) {
    operator fun invoke(): Flow<DailyGoal> = preferencesDataStore.dailyGoal
}

class UpdateDailyGoalUseCase @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) {
    suspend operator fun invoke(goal: DailyGoal) {
        preferencesDataStore.updateDailyGoal(goal)
    }
}

class RecordLearningSessionUseCase @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) {
    suspend operator fun invoke(
        wordsLearned: Int,
        wordsReviewed: Int,
        correctCount: Int,
        durationSeconds: Long
    ) {
        preferencesDataStore.checkAndUpdateStreak()
    }
}
