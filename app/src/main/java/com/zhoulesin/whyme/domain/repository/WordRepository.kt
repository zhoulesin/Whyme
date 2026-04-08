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
}
