package com.zhoulesin.whyme.data.repository

import com.zhoulesin.whyme.data.datastore.CurrentUser
import com.zhoulesin.whyme.data.local.dao.FavoriteDao
import com.zhoulesin.whyme.data.local.dao.UserWordProgressDao
import com.zhoulesin.whyme.data.local.dao.WordDao
import com.zhoulesin.whyme.data.local.entity.UserWordProgressEntity
import com.zhoulesin.whyme.data.local.entity.FavoriteEntity
import com.zhoulesin.whyme.data.local.entity.toDomain
import com.zhoulesin.whyme.data.local.entity.toDomainList
import com.zhoulesin.whyme.data.local.entity.toEntity
import com.zhoulesin.whyme.data.local.entity.toProgressEntity
import com.zhoulesin.whyme.domain.model.ReviewResult
import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.domain.model.WordLevel
import com.zhoulesin.whyme.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao,
    private val userWordProgressDao: UserWordProgressDao,
    private val favoriteDao: FavoriteDao
) : WordRepository {

    private fun uid(): String = CurrentUser.userId

    private suspend fun getFavoriteIds(): Set<Long> {
        return favoriteDao.getFavoriteWordIds(uid()).first().toSet()
    }

    private suspend fun buildWordsWithState(wordEntities: List<com.zhoulesin.whyme.data.local.entity.WordEntity>): List<Word> {
        if (wordEntities.isEmpty()) return emptyList()
        val wordIds = wordEntities.map { it.id }
        val progressList = userWordProgressDao.getProgressByWordIds(uid(), wordIds)
        val progressMap = progressList.associateBy { it.wordId }
        val favoriteIds = favoriteDao.getFavoriteWordIdsIn(uid(), wordIds).toSet()
        return wordEntities.map { entity ->
            entity.toDomain(
                progress = progressMap[entity.id],
                isFavorite = favoriteIds.contains(entity.id)
            )
        }
    }

    private fun combineWordData(
        wordsFlow: Flow<List<com.zhoulesin.whyme.data.local.entity.WordEntity>>
    ): Flow<List<Word>> = flow {
        wordsFlow.collect { wordEntities ->
            emit(buildWordsWithState(wordEntities))
        }
    }

    override fun getAllWords(): Flow<List<Word>> =
        combineWordData(wordDao.getAllWords())

    override fun getWordsForReview(limit: Int, level: WordLevel?): Flow<List<Word>> =
        userWordProgressDao.getWordsForReview(uid(), LocalDate.now().toEpochDay(), limit, level?.name)
            .map { wordEntities ->
                val favoriteIds = getFavoriteIds()
                wordEntities.map { entity ->
                    val progress = userWordProgressDao.getProgressByWordId(uid(), entity.id)
                    entity.toDomain(progress, favoriteIds.contains(entity.id))
                }
            }

    override fun getTodayNewWords(limit: Int, level: WordLevel?): Flow<List<Word>> =
        userWordProgressDao.getLearningWords(uid(), limit, level?.name)
            .map { wordEntities ->
                val favoriteIds = getFavoriteIds()
                wordEntities.map { entity ->
                    val progress = userWordProgressDao.getProgressByWordId(uid(), entity.id)
                    entity.toDomain(progress, favoriteIds.contains(entity.id))
                }
            }

    override fun getTodayLearnedWords(): Flow<List<Word>> {
        val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())) * 1000
        return userWordProgressDao.getTodayLearnedWords(uid(), todayStart)
            .map { wordEntities ->
                val favoriteIds = getFavoriteIds()
                wordEntities.map { entity ->
                    val progress = userWordProgressDao.getProgressByWordId(uid(), entity.id)
                    entity.toDomain(progress, favoriteIds.contains(entity.id))
                }
            }
    }

    override fun getFavoriteWords(): Flow<List<Word>> =
        favoriteDao.getFavoriteWords(uid())
            .map { wordEntities ->
                wordEntities.map { entity ->
                    val progress = userWordProgressDao.getProgressByWordId(uid(), entity.id)
                    entity.toDomain(progress, true)
                }
            }

    override fun getNewWordsBook(): Flow<List<Word>> =
        favoriteDao.getNewWordsBook(uid())
            .map { wordEntities ->
                wordEntities.map { entity ->
                    val progress = userWordProgressDao.getProgressByWordId(uid(), entity.id)
                    entity.toDomain(progress, true)
                }
            }

    override suspend fun getWordById(id: Long): Word? {
        val entity = wordDao.getWordById(id) ?: return null
        val progress = userWordProgressDao.getProgressByWordId(uid(), id)
        val isFavorite = favoriteDao.isFavorite(uid(), id)
        return entity.toDomain(progress, isFavorite)
    }

    override suspend fun insertWord(word: Word): Long {
        val wordId = wordDao.insertWord(word.toEntity())
        if (word.masteryLevel > 0 || word.isLearned) {
            userWordProgressDao.insertOrUpdateProgress(
                word.copy(id = wordId).toProgressEntity().copy(userId = uid())
            )
        }
        if (word.isFavorite) {
            favoriteDao.addFavorite(FavoriteEntity(userId = uid(), wordId = wordId))
        }
        return wordId
    }

    override suspend fun insertWords(words: List<Word>) {
        words.forEach { insertWord(it) }
    }

    override suspend fun updateWord(word: Word) {
        wordDao.updateWord(word.toEntity())
        userWordProgressDao.insertOrUpdateProgress(word.toProgressEntity().copy(userId = uid()))
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

        val existingProgress = userWordProgressDao.getProgressByWordId(uid(), wordId)
        if (existingProgress == null) {
            userWordProgressDao.insertOrUpdateProgress(
                UserWordProgressEntity(
                    userId = uid(),
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
            userWordProgressDao.updateReview(
                userId = uid(),
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
        return favoriteDao.toggleFavorite(uid(), wordId)
    }

    override suspend fun getWordCount(): Int =
        wordDao.getWordCount()

    override suspend fun getMasteredWordCount(): Int =
        userWordProgressDao.getMasteredWordCount(uid())

    override suspend fun getLearningWordCount(): Int =
        userWordProgressDao.getLearningWordCount(uid())

    override suspend fun getUnknownWordCount(): Int {
        val total = wordDao.getWordCount()
        val learned = userWordProgressDao.getMasteredWordCount(uid()) + userWordProgressDao.getLearningWordCount(uid())
        return total - learned
    }

    override suspend fun getTodayNewWordsCount(): Int {
        val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())) * 1000
        return userWordProgressDao.getTodayNewWordsCount(uid(), todayStart)
    }

    override suspend fun getTodayReviewCount(): Int {
        val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())) * 1000
        return userWordProgressDao.getTodayReviewCount(uid(), todayStart)
    }

    override fun searchWords(query: String): Flow<List<Word>> =
        wordDao.searchWords(query).map { wordEntities ->
            buildWordsWithState(wordEntities)
        }

    override fun getWordsByBank(wordBank: String): Flow<List<Word>> =
        combineWordData(wordDao.getWordsByBank(wordBank))

    override fun getWordsNeedingReview(): Flow<List<Word>> {
        return userWordProgressDao.getAllProgress(uid())
            .map { progressList ->
                progressList.filter { it.masteryLevel in 1..3 }
                    .mapNotNull { progress ->
                        wordDao.getWordById(progress.wordId)?.let { entity ->
                            val isFavorite = favoriteDao.isFavorite(uid(), entity.id)
                            entity.toDomain(progress, isFavorite)
                        }
                    }
            }
    }

    override fun getAllLearnedWords(): Flow<List<Word>> {
        return userWordProgressDao.getLearnedWords(uid())
            .map { wordEntities ->
                wordEntities.mapNotNull { wordEntity ->
                    val progress = userWordProgressDao.getProgressByWordId(uid(), wordEntity.id)
                    val isFavorite = favoriteDao.isFavorite(uid(), wordEntity.id)
                    wordEntity.toDomain(progress, isFavorite)
                }
            }
    }
}
