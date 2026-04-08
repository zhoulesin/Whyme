package com.zhoulesin.whyme.data.local.dao

import androidx.room.*
import com.zhoulesin.whyme.data.local.entity.UserWordProgressEntity
import com.zhoulesin.whyme.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 用户单词学习进度数据访问对象
 */
@Dao
interface UserWordProgressDao {

    /**
     * 获取所有学习进度
     */
    @Query("SELECT * FROM user_word_progress ORDER BY createdAt DESC")
    fun getAllProgress(): Flow<List<UserWordProgressEntity>>

    /**
     * 获取需要复习的单词（关联查询）
     */
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

    /**
     * 获取学习单词（未学习或新词）
     */
    @Query("""
        SELECT w.* FROM words w
        LEFT JOIN user_word_progress p ON w.id = p.wordId
        WHERE (:level IS NULL OR w.level = :level)
        AND (p.isLearned = 0 OR p.isLearned IS NULL)
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    fun getLearningWords(limit: Int, level: String? = null): Flow<List<WordEntity>>

    /**
     * 获取今日已学习的单词
     */
    @Query("""
        SELECT w.* FROM words w
        INNER JOIN user_word_progress p ON w.id = p.wordId
        WHERE p.learnedAt IS NOT NULL
        AND p.learnedAt >= :todayStart
        ORDER BY p.learnedAt DESC
    """)
    fun getTodayLearnedWords(todayStart: Long): Flow<List<WordEntity>>

    /**
     * 获取已学习的单词
     */
    @Query("""
        SELECT w.* FROM words w
        INNER JOIN user_word_progress p ON w.id = p.wordId
        WHERE p.isLearned = 1
        AND (:level IS NULL OR w.level = :level)
        ORDER BY p.learnedAt DESC
    """)
    fun getLearnedWords(level: String? = null): Flow<List<WordEntity>>

    /**
     * 获取单词的学习进度
     */
    @Query("SELECT * FROM user_word_progress WHERE wordId = :wordId")
    suspend fun getProgressByWordId(wordId: Long): UserWordProgressEntity?

    /**
     * 插入或更新学习进度
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: UserWordProgressEntity): Long

    /**
     * 更新复习状态
     */
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

    /**
     * 获取学习中单词数 (0 < masteryLevel < 4)
     */
    @Query("SELECT COUNT(*) FROM user_word_progress WHERE masteryLevel > 0 AND masteryLevel < 4")
    suspend fun getLearningWordCount(): Int

    /**
     * 获取已掌握单词数 (masteryLevel >= 4)
     */
    @Query("SELECT COUNT(*) FROM user_word_progress WHERE masteryLevel >= 4")
    suspend fun getMasteredWordCount(): Int

    /**
     * 按级别获取已掌握单词数
     */
    @Query("""
        SELECT COUNT(*) FROM words w
        INNER JOIN user_word_progress p ON w.id = p.wordId
        WHERE w.level = :level AND p.masteryLevel >= 4
    """)
    suspend fun getMasteredWordCountByLevel(level: String): Int

    /**
     * 按级别获取已学习单词数
     */
    @Query("""
        SELECT COUNT(*) FROM words w
        INNER JOIN user_word_progress p ON w.id = p.wordId
        WHERE w.level = :level AND p.isLearned = 1
    """)
    suspend fun getLearnedWordCountByLevel(level: String): Int

    /**
     * 今日新词学习数量
     */
    @Query("""
        SELECT COUNT(*) FROM user_word_progress
        WHERE learnedAt IS NOT NULL AND learnedAt >= :todayStart
    """)
    suspend fun getTodayNewWordsCount(todayStart: Long): Int

    /**
     * 今日复习数量
     */
    @Query("""
        SELECT COUNT(*) FROM user_word_progress
        WHERE lastReviewedAt IS NOT NULL AND lastReviewedAt >= :todayStart
    """)
    suspend fun getTodayReviewCount(todayStart: Long): Int

    /**
     * 删除所有学习进度
     */
    @Query("DELETE FROM user_word_progress")
    suspend fun clearAllProgress()
}
