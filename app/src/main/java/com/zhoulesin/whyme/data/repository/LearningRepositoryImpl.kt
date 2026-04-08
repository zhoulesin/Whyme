package com.zhoulesin.whyme.data.repository

import com.zhoulesin.whyme.data.local.PreferencesDataStore
import com.zhoulesin.whyme.data.local.dao.LearningRecordDao
import com.zhoulesin.whyme.data.local.entity.LearningRecordEntity
import com.zhoulesin.whyme.data.local.entity.toDomain
import com.zhoulesin.whyme.data.local.entity.toRecordDomainList
import com.zhoulesin.whyme.data.local.entity.toEntity
import com.zhoulesin.whyme.domain.model.DailyGoal
import com.zhoulesin.whyme.domain.model.LearningRecord
import com.zhoulesin.whyme.domain.model.UserStats
import com.zhoulesin.whyme.domain.repository.LearningRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 学习记录仓库实现
 */
@Singleton
class LearningRepositoryImpl @Inject constructor(
    private val learningRecordDao: LearningRecordDao,
    private val preferencesDataStore: PreferencesDataStore
) : LearningRepository {

    override fun getTodayRecord(): Flow<LearningRecord?> =
        learningRecordDao.getRecordByDate(LocalDate.now().toEpochDay())
            .map { it?.toDomain() }

    override suspend fun getRecordByDate(date: LocalDate): LearningRecord? =
        learningRecordDao.getRecordByDateSync(date.toEpochDay())?.toDomain()

    override fun getRecordsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<LearningRecord>> =
        learningRecordDao.getRecordsBetween(startDate.toEpochDay(), endDate.toEpochDay())
            .map { it.toRecordDomainList() }

    override suspend fun saveRecord(record: LearningRecord) =
        learningRecordDao.insertRecord(record.toEntity())

    override suspend fun updateRecord(record: LearningRecord) =
        learningRecordDao.updateRecord(record.toEntity())

    override fun getUserStats(): Flow<UserStats> = combine(
        learningRecordDao.getAllRecords(),
        getTodayRecord(),
        preferencesDataStore.dailyGoal
    ) { allRecords, todayRecord, goal ->
        val totalLearned = allRecords.sumOf { it.wordsLearned }
        val totalReviewed = allRecords.sumOf { it.wordsReviewed }
        val totalSeconds = allRecords.sumOf { it.durationSeconds }
        val streak = calculateStreak(allRecords)

        UserStats(
            totalWordsLearned = totalLearned,
            totalWordsReviewed = totalReviewed,
            currentStreak = streak,
            longestStreak = streak, // TODO: 从偏好设置获取
            totalLearningMinutes = totalSeconds / 60,
            todayWordsLearned = todayRecord?.wordsLearned ?: 0,
            todayWordsReviewed = todayRecord?.wordsReviewed ?: 0,
            todayAccuracy = todayRecord?.accuracy ?: 0f
        )
    }

    override fun getDailyGoal(): Flow<DailyGoal> = preferencesDataStore.dailyGoal

    override suspend fun updateDailyGoal(goal: DailyGoal) =
        preferencesDataStore.updateDailyGoal(goal)

    override suspend fun getCurrentStreak(): Int = preferencesDataStore.getCurrentStreak()

    override suspend fun getLongestStreak(): Int = 0 // TODO: 实现

    override suspend fun recordLearningSession(
        wordsLearned: Int,
        wordsReviewed: Int,
        correctCount: Int,
        durationSeconds: Long
    ) {
        val today = LocalDate.now()
        val existingRecord = getRecordByDate(today)

        val updatedRecord = if (existingRecord != null) {
            existingRecord.copy(
                wordsLearned = existingRecord.wordsLearned + wordsLearned,
                wordsReviewed = existingRecord.wordsReviewed + wordsReviewed,
                correctCount = existingRecord.correctCount + correctCount,
                durationSeconds = existingRecord.durationSeconds + durationSeconds
            )
        } else {
            LearningRecord(
                date = today,
                wordsLearned = wordsLearned,
                wordsReviewed = wordsReviewed,
                correctCount = correctCount,
                durationSeconds = durationSeconds
            )
        }

        saveRecord(updatedRecord)
    }

    private fun calculateStreak(records: List<LearningRecordEntity>): Int {
        if (records.isEmpty()) return 0

        val sortedRecords = records.sortedByDescending { it.date }
        var streak = 0
        var expectedDate = LocalDate.now()

        for (record in sortedRecords) {
            val recordDate = LocalDate.ofEpochDay(record.date)
            if (recordDate == expectedDate || recordDate == expectedDate.minusDays(1)) {
                streak++
                expectedDate = recordDate.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }
}
