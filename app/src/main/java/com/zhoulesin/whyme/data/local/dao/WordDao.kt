package com.zhoulesin.whyme.data.local.dao

import androidx.room.*
import com.zhoulesin.whyme.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 级别单词数量
 */
data class LevelWordCount(
    val level: String,
    val count: Int
)

/**
 * 单词数据访问对象
 */
@Dao
interface WordDao {
    @Query("SELECT * FROM words ORDER BY createdAt DESC")
    fun getAllWords(): Flow<List<WordEntity>>

    /**
     * 获取需要复习的单词
     * @param today 今天的 Epoch day
     * @param limit 每日复习上限
     * @param level 词库级别（可选）
     */
    @Query("""
        SELECT * FROM words
        WHERE nextReviewDate IS NOT NULL
        AND nextReviewDate <= :today
        AND masteryLevel < 5
        AND (:level IS NULL OR level = :level)
        ORDER BY nextReviewDate ASC
        LIMIT :limit
    """)
    fun getWordsForReview(today: Long, limit: Int, level: String? = null): Flow<List<WordEntity>>

    /**
     * 获取学习页单词组。
     * 学习模式只按当前级别生成单词，不过滤是否已学习。
     */
    @Query("""
        SELECT * FROM words
        WHERE (:level IS NULL OR level = :level)
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    fun getLearningWords(limit: Int, level: String? = null): Flow<List<WordEntity>>

    /**
     * 获取今日已学习的单词
     */
    @Query("""
        SELECT * FROM words
        WHERE learnedAt IS NOT NULL
        AND learnedAt >= :todayStart
        ORDER BY learnedAt DESC
    """)
    fun getTodayLearnedWords(todayStart: Long): Flow<List<WordEntity>>

    /**
     * 获取收藏的单词
     */
    @Query("SELECT * FROM words WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteWords(): Flow<List<WordEntity>>

    /**
     * 获取生词本（收藏的未掌握单词）
     */
    @Query("""
        SELECT * FROM words
        WHERE isFavorite = 1 AND masteryLevel < 4
        ORDER BY masteryLevel ASC, createdAt DESC
    """)
    fun getNewWordsBook(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Long): WordEntity?

    @Query("SELECT * FROM words WHERE word LIKE '%' || :query || '%' OR translation LIKE '%' || :query || '%'")
    fun searchWords(query: String): Flow<List<WordEntity>>

    /**
     * 根据词库获取单词
     */
    @Query("SELECT * FROM words WHERE wordBank = :wordBank ORDER BY word ASC")
    fun getWordsByBank(wordBank: String): Flow<List<WordEntity>>

    /**
     * 获取需要继续复习的单词（学习中但未掌握）
     */
    @Query("""
        SELECT * FROM words
        WHERE masteryLevel > 0 AND masteryLevel < 4
        ORDER BY RANDOM()
    """)
    fun getWordsNeedingReview(): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int

    @Query("SELECT COUNT(*) FROM words WHERE masteryLevel >= 4")
    suspend fun getMasteredWordCount(): Int

    /**
     * 学习中单词数 (0 < masteryLevel < 4)
     */
    @Query("SELECT COUNT(*) FROM words WHERE masteryLevel > 0 AND masteryLevel < 4")
    suspend fun getLearningWordCount(): Int

    /**
     * 陌生单词数 (masteryLevel = 0)
     */
    @Query("SELECT COUNT(*) FROM words WHERE masteryLevel = 0")
    suspend fun getUnknownWordCount(): Int
    
    /**
     * 按级别获取单词总数
     */
    @Query("SELECT COUNT(*) FROM words WHERE level = :level")
    suspend fun getWordCountByLevel(level: String): Int
    
    /**
     * 按级别获取已掌握单词数
     */
    @Query("SELECT COUNT(*) FROM words WHERE level = :level AND masteryLevel >= 4")
    suspend fun getMasteredWordCountByLevel(level: String): Int
    
    /**
     * 按级别获取已学习单词数
     */
    @Query("SELECT COUNT(*) FROM words WHERE level = :level AND isLearned = 1")
    suspend fun getLearnedWordCountByLevel(level: String): Int
    
    /**
     * 获取所有级别的单词数量
     */
    @Query("SELECT level, COUNT(*) as count FROM words GROUP BY level")
    suspend fun getWordCountByAllLevels(): List<LevelWordCount>

    /**
     * 今日新词学习数量
     */
    @Query("""
        SELECT COUNT(*) FROM words
        WHERE learnedAt IS NOT NULL AND learnedAt >= :todayStart
    """)
    suspend fun getTodayNewWordsCount(todayStart: Long): Int

    /**
     * 今日复习数量
     */
    @Query("""
        SELECT COUNT(*) FROM words
        WHERE lastReviewedAt IS NOT NULL AND lastReviewedAt >= :todayStart
    """)
    suspend fun getTodayReviewCount(todayStart: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    @Update
    suspend fun updateWord(word: WordEntity)

    @Delete
    suspend fun deleteWord(word: WordEntity)

    @Query("""
        UPDATE words SET
            masteryLevel = :level,
            nextReviewDate = :nextReviewDate,
            reviewCount = reviewCount + 1,
            lastReviewedAt = :lastReviewedAt,
            isLearned = CASE WHEN :isLearned = 1 OR isLearned = 1 THEN 1 ELSE 0 END,
            learnedAt = CASE WHEN :isLearned = 1 AND learnedAt IS NULL THEN :lastReviewedAt ELSE learnedAt END,
            correctCount = correctCount + :correctIncrement,
            lastReviewResult = :reviewResult
        WHERE id = :wordId
    """)
    suspend fun updateWordReview(
        wordId: Long,
        level: Int,
        nextReviewDate: Long,
        isLearned: Boolean,
        lastReviewedAt: Long,
        reviewResult: String?,
        correctIncrement: Int
    )

    @Query("UPDATE words SET masteryLevel = :level, nextReviewDate = :nextReviewDate, reviewCount = reviewCount + 1, lastReviewedAt = :timestamp WHERE id = :wordId")
    suspend fun updateMasteryLevel(wordId: Long, level: Int, nextReviewDate: Long, timestamp: Long)

    @Query("UPDATE words SET isFavorite = NOT isFavorite WHERE id = :wordId")
    suspend fun toggleFavorite(wordId: Long)

    /**
     * 标记单词为已学习
     */
    @Query("UPDATE words SET isLearned = 1, isNew = 0, learnedAt = :timestamp WHERE id = :wordId")
    suspend fun markAsLearned(wordId: Long, timestamp: Long)
}
