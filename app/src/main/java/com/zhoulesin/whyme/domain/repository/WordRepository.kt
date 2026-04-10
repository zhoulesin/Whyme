package com.zhoulesin.whyme.domain.repository

import com.zhoulesin.whyme.domain.model.ReviewResult
import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.domain.model.WordLevel
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * 单词仓库接口
 */
interface WordRepository {
    /**
     * 获取所有单词
     */
    fun getAllWords(): Flow<List<Word>>

    /**
     * 获取需要复习的单词
     * @param limit 每日复习上限
     * @param level 词库级别（可选）
     */
    fun getWordsForReview(limit: Int = 50, level: WordLevel? = null): Flow<List<Word>>

    /**
     * 获取今日新学单词
     * @param limit 限制数量
     * @param level 词库级别（可选）
     */
    fun getTodayNewWords(limit: Int, level: WordLevel? = null): Flow<List<Word>>

    /**
     * 获取今日已学习的单词
     */
    fun getTodayLearnedWords(): Flow<List<Word>>

    /**
     * 获取收藏的单词
     */
    fun getFavoriteWords(): Flow<List<Word>>

    /**
     * 获取生词本（收藏的未掌握单词）
     */
    fun getNewWordsBook(): Flow<List<Word>>

    /**
     * 根据ID获取单词
     */
    suspend fun getWordById(id: Long): Word?

    /**
     * 添加新单词
     */
    suspend fun insertWord(word: Word): Long

    /**
     * 批量添加单词
     */
    suspend fun insertWords(words: List<Word>)

    /**
     * 更新单词
     */
    suspend fun updateWord(word: Word)

    /**
     * 删除单词
     */
    suspend fun deleteWord(word: Word)

    /**
     * 更新单词复习信息
     * @param wordId 单词ID
     * @param masteryLevel 新的掌握等级
     * @param nextReviewDate 下次复习日期
     * @param isLearned 是否标记为已学习
     * @param reviewResult 复习结果
     */
    suspend fun updateWordReview(
        wordId: Long,
        masteryLevel: Int,
        nextReviewDate: LocalDate,
        isLearned: Boolean = false,
        reviewResult: ReviewResult? = null
    )

    /**
     * 切换单词收藏状态
     * @return true 表示已收藏，false 表示已取消收藏
     */
    suspend fun toggleFavorite(wordId: Long): Boolean

    /**
     * 获取单词总数
     */
    suspend fun getWordCount(): Int

    /**
     * 获取已掌握的单词数 (masteryLevel >= 4)
     */
    suspend fun getMasteredWordCount(): Int

    /**
     * 获取学习中单词数 (0 < masteryLevel < 4)
     */
    suspend fun getLearningWordCount(): Int

    /**
     * 获取陌生单词数 (masteryLevel = 0)
     */
    suspend fun getUnknownWordCount(): Int

    /**
     * 获取今日新词学习数量
     */
    suspend fun getTodayNewWordsCount(): Int

    /**
     * 获取今日复习数量
     */
    suspend fun getTodayReviewCount(): Int

    /**
     * 搜索单词
     */
    fun searchWords(query: String): Flow<List<Word>>

    /**
     * 获取指定词库的单词
     */
    fun getWordsByBank(wordBank: String): Flow<List<Word>>

    /**
     * 获取已学习但未掌握的单词（需要继续复习）
     */
    fun getWordsNeedingReview(): Flow<List<Word>>

    /**
     * 获取所有已学习的单词
     */
    fun getAllLearnedWords(): Flow<List<Word>>

    // 新增方法：学习记录相关

    /**
     * 记录单词学习
     * @param wordId 单词ID
     * @param level 词库级别
     * @param masteryLevel 掌握级别
     */
    suspend fun recordWordLearning(wordId: Long, level: String, masteryLevel: Int)

    /**
     * 获取单词的学习记录
     */
    fun getWordLearningRecords(wordId: Long): Flow<List<com.zhoulesin.whyme.data.local.entity.LearningRecordEntity>>

    /**
     * 获取指定日期范围内的学习记录
     */
    fun getLearningRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<com.zhoulesin.whyme.data.local.entity.LearningRecordEntity>>

    /**
     * 获取指定级别的学习记录
     */
    fun getLearningRecordsByLevel(level: String): Flow<List<com.zhoulesin.whyme.data.local.entity.LearningRecordEntity>>

    // 新增方法：每日学习记录相关

    /**
     * 记录每日学习数据
     */
    suspend fun recordDailyLearning(date: Long, wordsLearned: Int, wordsReviewed: Int, correctCount: Int, totalQuestions: Int, durationMinutes: Int, accuracy: Float)

    /**
     * 获取指定日期的学习记录
     */
    suspend fun getDailyLearningRecord(date: Long): com.zhoulesin.whyme.data.local.entity.DailyLearningRecordEntity?

    /**
     * 获取最近的学习记录
     */
    fun getRecentDailyLearningRecords(limit: Int): Flow<List<com.zhoulesin.whyme.data.local.entity.DailyLearningRecordEntity>>

    /**
     * 获取指定日期范围内的学习记录
     */
    fun getDailyLearningRecordsByDateRange(startDate: Long, endDate: Long): Flow<List<com.zhoulesin.whyme.data.local.entity.DailyLearningRecordEntity>>

    // 新增方法：复习记录相关

    /**
     * 记录单词复习
     */
    suspend fun recordWordReview(wordId: Long, level: String, masteryLevel: Int, isCorrect: Boolean, reviewResult: String?, durationSeconds: Int)

    /**
     * 获取单词的复习记录
     */
    fun getWordReviewRecords(wordId: Long): Flow<List<com.zhoulesin.whyme.data.local.entity.ReviewRecordEntity>>

    /**
     * 获取指定日期范围内的复习记录
     */
    fun getReviewRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<com.zhoulesin.whyme.data.local.entity.ReviewRecordEntity>>

    // 新增方法：测试记录相关

    /**
     * 记录测试
     */
    suspend fun recordTest(testType: String, totalQuestions: Int, correctCount: Int, accuracy: Float, durationSeconds: Int, questionCount: Int, source: String?)

    /**
     * 获取所有测试记录
     */
    fun getAllTestRecords(): Flow<List<com.zhoulesin.whyme.data.local.entity.TestRecordEntity>>

    /**
     * 获取指定日期范围内的测试记录
     */
    fun getTestRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<com.zhoulesin.whyme.data.local.entity.TestRecordEntity>>

    // 新增方法：打卡记录相关

    /**
     * 记录打卡
     */
    suspend fun recordCheckIn(date: Long, learningMinutes: Int, wordsLearned: Int, wordsReviewed: Int)

    /**
     * 获取指定日期的打卡记录
     */
    suspend fun getCheckInRecord(date: Long): com.zhoulesin.whyme.data.local.entity.CheckInRecordEntity?

    /**
     * 获取所有打卡记录
     */
    fun getAllCheckInRecords(): Flow<List<com.zhoulesin.whyme.data.local.entity.CheckInRecordEntity>>

    /**
     * 获取当前连续打卡天数
     */
    suspend fun getCurrentStreak(): Int

    /**
     * 获取最长连续打卡天数
     */
    suspend fun getLongestStreak(): Int

    /**
     * 获取总打卡天数
     */
    suspend fun getTotalCheckInDays(): Int
}
