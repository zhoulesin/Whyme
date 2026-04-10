package com.zhoulesin.whyme.data.local.dao

import androidx.room.*
import com.zhoulesin.whyme.data.local.entity.UserWordProgressEntity
import com.zhoulesin.whyme.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserWordProgressDao {

    @Query("SELECT * FROM user_word_progress ORDER BY createdAt DESC")
    fun getAllProgress(): Flow<List<UserWordProgressEntity>>

    @Query("""
        SELECT w.* FROM words w
        INNER JOIN user_word_progress p ON w.id = p.wordId
        WHERE p.nextReviewDate IS NOT NULL
        AND p.nextReviewDate <= :today
        AND p.masteryLevel < 5
        AND (:level IS NULL OR w.level = :level)
        ORDER BY p.nextReviewDate ASC
        LIMIT :limit
    """)
    fun getWordsForReview(today: Long, limit: Int, level: String? = null): Flow<List<WordEntity>>

    @Query("""
        SELECT w.* FROM words w
        LEFT JOIN user_word_progress p ON w.id = p.wordId
        WHERE (:level IS NULL OR w.level = :level)
        AND (p.isLearned = 0 OR p.isLearned IS NULL)
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    fun getLearningWords(limit: Int, level: String? = null): Flow<List<WordEntity>>

    @Query("""
        SELECT w.* FROM words w
        INNER JOIN user_word_progress p ON w.id = p.wordId
        WHERE p.learnedAt IS NOT NULL
        AND p.learnedAt >= :todayStart
        ORDER BY p.learnedAt DESC
    """)
    fun getTodayLearnedWords(todayStart: Long): Flow<List<WordEntity>>

    @Query("""
        SELECT w.* FROM words w
        INNER JOIN user_word_progress p ON w.id = p.wordId
        WHERE p.isLearned = 1
        AND (:level IS NULL OR w.level = :level)
        ORDER BY p.learnedAt DESC
    """)
    fun getLearnedWords(level: String? = null): Flow<List<WordEntity>>

    @Query("SELECT * FROM user_word_progress WHERE wordId = :wordId")
    suspend fun getProgressByWordId(wordId: Long): UserWordProgressEntity?

    @Query("SELECT * FROM user_word_progress WHERE wordId IN (:wordIds)")
    suspend fun getProgressByWordIds(wordIds: List<Long>): List<UserWordProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: UserWordProgressEntity): Long

    @Query("""
        UPDATE user_word_progress SET
            masteryLevel = :level,
            nextReviewDate = :nextReviewDate,
            reviewCount = reviewCount + 1,
            lastReviewedAt = :lastReviewedAt,
            isLearned = CASE WHEN :isLearned = 1 OR isLearned = 1 THEN 1 ELSE 0 END,
            learnedAt = CASE WHEN :isLearned = 1 AND learnedAt IS NULL THEN :lastReviewedAt ELSE learnedAt END,
            correctCount = correctCount + :correctIncrement,
            lastReviewResult = :reviewResult
        WHERE wordId = :wordId
    """)
    suspend fun updateReview(
        wordId: Long,
        level: Int,
        nextReviewDate: Long,
        isLearned: Boolean,
        lastReviewedAt: Long,
        reviewResult: String?,
        correctIncrement: Int
    )

    @Query("SELECT COUNT(*) FROM user_word_progress WHERE masteryLevel > 0 AND masteryLevel < 4")
    suspend fun getLearningWordCount(): Int

    @Query("SELECT COUNT(*) FROM user_word_progress WHERE masteryLevel >= 4")
    suspend fun getMasteredWordCount(): Int

    @Query("""
        SELECT COUNT(*) FROM words w
        INNER JOIN user_word_progress p ON w.id = p.wordId
        WHERE w.level = :level AND p.masteryLevel >= 4
    """)
    suspend fun getMasteredWordCountByLevel(level: String): Int

    @Query("""
        SELECT COUNT(*) FROM words w
        INNER JOIN user_word_progress p ON w.id = p.wordId
        WHERE w.level = :level AND p.isLearned = 1
    """)
    suspend fun getLearnedWordCountByLevel(level: String): Int

    @Query("""
        SELECT COUNT(*) FROM user_word_progress
        WHERE learnedAt IS NOT NULL AND learnedAt >= :todayStart
    """)
    suspend fun getTodayNewWordsCount(todayStart: Long): Int

    @Query("""
        SELECT COUNT(*) FROM user_word_progress
        WHERE lastReviewedAt IS NOT NULL AND lastReviewedAt >= :todayStart
    """)
    suspend fun getTodayReviewCount(todayStart: Long): Int

    @Query("DELETE FROM user_word_progress")
    suspend fun clearAllProgress()
}
