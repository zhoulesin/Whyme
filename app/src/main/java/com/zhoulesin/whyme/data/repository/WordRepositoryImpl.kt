package com.zhoulesin.whyme.data.repository

import com.zhoulesin.whyme.data.local.dao.WordDao
import com.zhoulesin.whyme.data.local.entity.toDomain
import com.zhoulesin.whyme.data.local.entity.toDomainList
import com.zhoulesin.whyme.data.local.entity.toEntity
import com.zhoulesin.whyme.domain.model.ReviewResult
import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.domain.model.WordLevel
import com.zhoulesin.whyme.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 单词仓库实现
 */
@Singleton
class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao
) : WordRepository {

    override fun getAllWords(): Flow<List<Word>> =
        wordDao.getAllWords().map { it.toDomainList() }

    override fun getWordsForReview(limit: Int, level: WordLevel?): Flow<List<Word>> =
        wordDao.getWordsForReview(LocalDate.now().toEpochDay(), limit, level?.name).map { it.toDomainList() }

    override fun getTodayNewWords(limit: Int, level: WordLevel?): Flow<List<Word>> =
        wordDao.getNewWords(limit, level?.name).map { it.toDomainList() }

    override fun getTodayLearnedWords(): Flow<List<Word>> {
        val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())) * 1000
        return wordDao.getTodayLearnedWords(todayStart).map { it.toDomainList() }
    }

    override fun getFavoriteWords(): Flow<List<Word>> =
        wordDao.getFavoriteWords().map { it.toDomainList() }

    override fun getNewWordsBook(): Flow<List<Word>> =
        wordDao.getNewWordsBook().map { it.toDomainList() }

    override suspend fun getWordById(id: Long): Word? =
        wordDao.getWordById(id)?.toDomain()

    override suspend fun insertWord(word: Word): Long =
        wordDao.insertWord(word.toEntity())

    override suspend fun insertWords(words: List<Word>) =
        wordDao.insertWords(words.map { it.toEntity() })

    override suspend fun updateWord(word: Word) =
        wordDao.updateWord(word.toEntity())

    override suspend fun deleteWord(word: Word) =
        wordDao.deleteWord(word.toEntity())

    override suspend fun updateWordReview(
        wordId: Long,
        masteryLevel: Int,
        nextReviewDate: LocalDate,
        isLearned: Boolean,
        reviewResult: ReviewResult?
    ) {
        val timestamp = System.currentTimeMillis()
        val isCorrect = reviewResult == ReviewResult.GOOD || reviewResult == ReviewResult.EASY
        wordDao.updateWordReview(
            wordId = wordId,
            level = masteryLevel,
            nextReviewDate = nextReviewDate.toEpochDay(),
            isLearned = isLearned,
            lastReviewedAt = timestamp,
            reviewResult = reviewResult?.name,
            correctIncrement = if (isCorrect) 1 else 0
        )
    }

    override suspend fun toggleFavorite(wordId: Long) =
        wordDao.toggleFavorite(wordId)

    override suspend fun getWordCount(): Int =
        wordDao.getWordCount()

    override suspend fun getMasteredWordCount(): Int =
        wordDao.getMasteredWordCount()

    override suspend fun getLearningWordCount(): Int =
        wordDao.getLearningWordCount()

    override suspend fun getUnknownWordCount(): Int =
        wordDao.getUnknownWordCount()

    override suspend fun getTodayNewWordsCount(): Int {
        val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())) * 1000
        return wordDao.getTodayNewWordsCount(todayStart)
    }

    override suspend fun getTodayReviewCount(): Int {
        val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())) * 1000
        return wordDao.getTodayReviewCount(todayStart)
    }

    override fun searchWords(query: String): Flow<List<Word>> =
        wordDao.searchWords(query).map { it.toDomainList() }

    override fun getWordsByBank(wordBank: String): Flow<List<Word>> =
        wordDao.getWordsByBank(wordBank).map { it.toDomainList() }

    override fun getWordsNeedingReview(): Flow<List<Word>> =
        wordDao.getWordsNeedingReview().map { it.toDomainList() }
}
