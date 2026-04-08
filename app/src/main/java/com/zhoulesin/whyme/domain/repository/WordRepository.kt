package com.zhoulesin.whyme.domain.repository

import com.zhoulesin.whyme.domain.model.Word
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
     */
    fun getWordsForReview(): Flow<List<Word>>

    /**
     * 获取今日新学单词
     */
    fun getTodayNewWords(limit: Int): Flow<List<Word>>

    /**
     * 获取收藏的单词
     */
    fun getFavoriteWords(): Flow<List<Word>>

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
     * 更新单词掌握程度
     */
    suspend fun updateMasteryLevel(wordId: Long, level: Int, nextReviewDate: LocalDate)

    /**
     * 切换收藏状态
     */
    suspend fun toggleFavorite(wordId: Long)

    /**
     * 获取单词总数
     */
    suspend fun getWordCount(): Int

    /**
     * 获取已掌握的单词数
     */
    suspend fun getMasteredWordCount(): Int

    /**
     * 搜索单词
     */
    fun searchWords(query: String): Flow<List<Word>>
}
