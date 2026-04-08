package com.zhoulesin.whyme.domain.usecase

import com.zhoulesin.whyme.domain.model.DailyGoal
import com.zhoulesin.whyme.domain.model.LearningRecord
import com.zhoulesin.whyme.domain.model.UserStats
import com.zhoulesin.whyme.domain.repository.LearningRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * 获取今日学习记录用例
 */
class GetTodayRecordUseCase @Inject constructor(
    private val learningRepository: LearningRepository
) {
    operator fun invoke(): Flow<LearningRecord?> = learningRepository.getTodayRecord()
}

/**
 * 获取用户统计数据用例
 */
class GetUserStatsUseCase @Inject constructor(
    private val learningRepository: LearningRepository
) {
    operator fun invoke(): Flow<UserStats> = learningRepository.getUserStats()
}

/**
 * 获取每日学习目标用例
 */
class GetDailyGoalUseCase @Inject constructor(
    private val learningRepository: LearningRepository
) {
    operator fun invoke(): Flow<DailyGoal> = learningRepository.getDailyGoal()
}

/**
 * 更新每日学习目标用例
 */
class UpdateDailyGoalUseCase @Inject constructor(
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(goal: DailyGoal) {
        learningRepository.updateDailyGoal(goal)
    }
}

/**
 * 记录学习会话用例
 */
class RecordLearningSessionUseCase @Inject constructor(
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(
        wordsLearned: Int,
        wordsReviewed: Int,
        correctCount: Int,
        durationSeconds: Long
    ) {
        learningRepository.recordLearningSession(wordsLearned, wordsReviewed, correctCount, durationSeconds)
    }
}

/**
 * 获取学习进度用例
 */
class GetLearningProgressUseCase @Inject constructor(
    private val learningRepository: LearningRepository
) {
    fun invoke(): Flow<Pair<Int, Int>> {
        return kotlinx.coroutines.flow.combine(
            learningRepository.getTodayRecord(),
            learningRepository.getDailyGoal()
        ) { record, goal ->
            val learned = record?.wordsLearned ?: 0
            val reviewed = record?.wordsReviewed ?: 0
            val total = goal.wordsPerDay + goal.reviewPerDay
            Pair(learned + reviewed, total)
        }
    }
}
