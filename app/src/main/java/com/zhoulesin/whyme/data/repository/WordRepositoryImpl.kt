package com.zhoulesin.whyme.data.repository

import com.zhoulesin.whyme.data.local.dao.FavoriteDao
import com.zhoulesin.whyme.data.local.dao.UserWordProgressDao
import com.zhoulesin.whyme.data.local.dao.WordDao
import com.zhoulesin.whyme.data.local.entity.UserWordProgressEntity
import com.zhoulesin.whyme.data.local.entity.toDomain
import com.zhoulesin.whyme.data.local.entity.toDomainList
import com.zhoulesin.whyme.data.local.entity.toEntity
import com.zhoulesin.whyme.data.local.entity.toProgressEntity
import com.zhoulesin.whyme.domain.model.ReviewResult
import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.domain.model.WordLevel
import com.zhoulesin.whyme.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 单词仓库实现
 * 新的实现：从多个表组合数据
 */
@Singleton
class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao,
    private val userWordProgressDao: UserWordProgressDao,
    private val favoriteDao: FavoriteDao
) : WordRepository {

    /**
     * 组合单词基础信息、学习进度和收藏状态
     */
    private fun combineWordData(
        wordsFlow: Flow<List<com.zhoulesin.whyme.data.local.entity.WordEntity>>
    ): Flow<List<Word>> = flow {
        wordsFlow.collect { wordEntities ->
            if (wordEntities.isEmpty()) {
                emit(emptyList())
                return@collect
            }

            // 获取所有单词ID
            val wordIds = wordEntities.map { it.id }

            // 获取学习进度
            val progressMap = wordIds.associateWith { wordId ->
                userWordProgressDao.getProgressByWordId(wordId)
            }

            // 获取收藏状态
            val favoriteIds = favoriteDao.getFavoriteWordIds().first().toSet()

            // 组合数据
            val words = wordEntities.map { entity ->
                entity.toDomain(
                    progress = progressMap[entity.id],
                    isFavorite = favoriteIds.contains(entity.id)
                )
            }

            emit(words)
        }
    }

    override fun getAllWords(): Flow<List<Word>> =
        combineWordData(wordDao.getAllWords())

    override fun getWordsForReview(limit: Int, level: WordLevel?): Flow<List<Word>> =
        userWordProgressDao.getWordsForReview(LocalDate.now().toEpochDay(), limit, level?.name)
            .map { wordEntities ->
                val favoriteIds = favoriteDao.getFavoriteWordIds().first().toSet()
                wordEntities.map { entity ->
                    val progress = userWordProgressDao.getProgressByWordId(entity.id)
                    entity.toDomain(progress, favoriteIds.contains(entity.id))
                }
            }

    override fun getTodayNewWords(limit: Int, level: WordLevel?): Flow<List<Word>> =
        userWordProgressDao.getLearningWords(limit, level?.name)
            .map { wordEntities ->
                println("WordRepositoryImpl.getTodayNewWords: level=${level?.name}, entities count=${wordEntities.size}")
                val favoriteIds = favoriteDao.getFavoriteWordIds().first().toSet()
                wordEntities.map { entity ->
                    val progress = userWordProgressDao.getProgressByWordId(entity.id)
                    entity.toDomain(progress, favoriteIds.contains(entity.id))
                }
            }

    override fun getTodayLearnedWords(): Flow<List<Word>> {
        val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())) * 1000
        return userWordProgressDao.getTodayLearnedWords(todayStart)
            .map { wordEntities ->
                val favoriteIds = favoriteDao.getFavoriteWordIds().first().toSet()
                wordEntities.map { entity ->
                    val progress = userWordProgressDao.getProgressByWordId(entity.id)
                    entity.toDomain(progress, favoriteIds.contains(entity.id))
                }
            }
    }

    override fun getFavoriteWords(): Flow<List<Word>> =
        favoriteDao.getFavoriteWords()
            .map { wordEntities ->
                wordEntities.map { entity ->
                    val progress = userWordProgressDao.getProgressByWordId(entity.id)
                    entity.toDomain(progress, true) // 从收藏表获取的肯定已收藏
                }
            }

    override fun getNewWordsBook(): Flow<List<Word>> =
        favoriteDao.getNewWordsBook()
            .map { wordEntities ->
                wordEntities.map { entity ->
                    val progress = userWordProgressDao.getProgressByWordId(entity.id)
                    entity.toDomain(progress, true)
                }
            }

    override suspend fun getWordById(id: Long): Word? {
        val entity = wordDao.getWordById(id) ?: return null
        val progress = userWordProgressDao.getProgressByWordId(id)
        val isFavorite = favoriteDao.isFavorite(id)
        return entity.toDomain(progress, isFavorite)
    }

    override suspend fun insertWord(word: Word): Long {
        val wordId = wordDao.insertWord(word.toEntity())
        // 如果有学习进度，也插入进度表
        if (word.masteryLevel > 0 || word.isLearned) {
            userWordProgressDao.insertOrUpdateProgress(
                word.copy(id = wordId).toProgressEntity()
            )
        }
        // 如果已收藏，添加到收藏表
        if (word.isFavorite) {
            favoriteDao.addFavorite(
                com.zhoulesin.whyme.data.local.entity.FavoriteEntity(wordId = wordId)
            )
        }
        return wordId
    }

    override suspend fun insertWords(words: List<Word>) {
        words.forEach { insertWord(it) }
    }

    override suspend fun updateWord(word: Word) {
        wordDao.updateWord(word.toEntity())
        // 更新学习进度
        userWordProgressDao.insertOrUpdateProgress(word.toProgressEntity())
    }

    override suspend fun deleteWord(word: Word) {
        wordDao.deleteWord(word.toEntity())
    }

    override suspend fun updateWordReview(
        wordId: Long,
        masteryLevel: Int,
        nextReviewDate: LocalDate,
        isLearned: Boolean,
        reviewResult: ReviewResult?
    ) {
        val timestamp = System.currentTimeMillis()
        val isCorrect = reviewResult == ReviewResult.GOOD || reviewResult == ReviewResult.EASY

        // 先确保有学习进度记录
        val existingProgress = userWordProgressDao.getProgressByWordId(wordId)
        if (existingProgress == null) {
            // 创建新的学习进度记录
            userWordProgressDao.insertOrUpdateProgress(
                UserWordProgressEntity(
                    wordId = wordId,
                    masteryLevel = masteryLevel,
                    isLearned = isLearned,
                    isNew = false,
                    nextReviewDate = nextReviewDate.toEpochDay(),
                    reviewCount = 1,
                    correctCount = if (isCorrect) 1 else 0,
                    lastReviewResult = reviewResult?.name,
                    learnedAt = if (isLearned) timestamp else null,
                    lastReviewedAt = timestamp
                )
            )
        } else {
            // 更新现有记录
            userWordProgressDao.updateReview(
                wordId = wordId,
                level = masteryLevel,
                nextReviewDate = nextReviewDate.toEpochDay(),
                isLearned = isLearned,
                lastReviewedAt = timestamp,
                reviewResult = reviewResult?.name,
                correctIncrement = if (isCorrect) 1 else 0
            )
        }
    }

    override suspend fun toggleFavorite(wordId: Long): Boolean {
        return favoriteDao.toggleFavorite(wordId)
    }

    override suspend fun getWordCount(): Int =
        wordDao.getWordCount()

    override suspend fun getMasteredWordCount(): Int =
        userWordProgressDao.getMasteredWordCount()

    override suspend fun getLearningWordCount(): Int =
        userWordProgressDao.getLearningWordCount()

    override suspend fun getUnknownWordCount(): Int {
        val total = wordDao.getWordCount()
        val learned = userWordProgressDao.getMasteredWordCount() + userWordProgressDao.getLearningWordCount()
        return total - learned
    }

    override suspend fun getTodayNewWordsCount(): Int {
        val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())) * 1000
        return userWordProgressDao.getTodayNewWordsCount(todayStart)
    }

    override suspend fun getTodayReviewCount(): Int {
        val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())) * 1000
        return userWordProgressDao.getTodayReviewCount(todayStart)
    }

    override fun searchWords(query: String): Flow<List<Word>> =
        combineWordData(wordDao.searchWords(query))

    override fun getWordsByBank(wordBank: String): Flow<List<Word>> =
        combineWordData(wordDao.getWordsByBank(wordBank))

    override fun getWordsNeedingReview(): Flow<List<Word>> {
        // 获取学习中但未掌握的单词（需要继续复习）
        return userWordProgressDao.getAllProgress()
            .map { progressList ->
                progressList.filter { it.masteryLevel in 1..3 }
                    .mapNotNull { progress ->
                        wordDao.getWordById(progress.wordId)?.let { entity ->
                            val isFavorite = favoriteDao.isFavorite(entity.id)
                            entity.toDomain(progress, isFavorite)
                        }
                    }
            }
    }

    override fun getAllLearnedWords(): Flow<List<Word>> {
        // 获取所有已学习的单词
        return userWordProgressDao.getLearnedWords()
            .map { wordEntities ->
                wordEntities.mapNotNull { wordEntity ->
                    val progress = userWordProgressDao.getProgressByWordId(wordEntity.id)
                    val isFavorite = favoriteDao.isFavorite(wordEntity.id)
                    wordEntity.toDomain(progress, isFavorite)
                }
            }
    }
}
