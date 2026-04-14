package com.zhoulesin.whyme.data.repository

import com.zhoulesin.whyme.data.local.dao.FavoriteDao
import com.zhoulesin.whyme.data.local.dao.UserWordProgressDao
import com.zhoulesin.whyme.data.local.dao.WordDao
import com.zhoulesin.whyme.data.local.dao.LearningRecordDao
import com.zhoulesin.whyme.data.local.dao.ReviewRecordDao
import com.zhoulesin.whyme.data.local.dao.TestRecordDao
import com.zhoulesin.whyme.data.local.dao.CheckInRecordDao
import com.zhoulesin.whyme.data.local.entity.UserWordProgressEntity
import com.zhoulesin.whyme.data.local.entity.FavoriteEntity
import com.zhoulesin.whyme.data.local.entity.LearningRecordEntity
import com.zhoulesin.whyme.data.local.entity.ReviewRecordEntity
import com.zhoulesin.whyme.data.local.entity.TestRecordEntity
import com.zhoulesin.whyme.data.local.entity.CheckInRecordEntity
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
    private val favoriteDao: FavoriteDao,
    private val learningRecordDao: LearningRecordDao,
    private val reviewRecordDao: ReviewRecordDao,
    private val testRecordDao: TestRecordDao,
    private val checkInRecordDao: CheckInRecordDao
) : WordRepository {

    private suspend fun getFavoriteIds(): Set<Long> {
        return favoriteDao.getFavoriteWordIds().first().toSet()
    }

    private suspend fun buildWordsWithState(wordEntities: List<com.zhoulesin.whyme.data.local.entity.WordEntity>): List<Word> {
        if (wordEntities.isEmpty()) return emptyList()
        val wordIds = wordEntities.map { it.id }
        val progressList = userWordProgressDao.getProgressByWordIds(wordIds)
        val progressMap = progressList.associateBy { it.wordId }
        val favoriteIds = favoriteDao.getFavoriteWordIdsIn(wordIds).toSet()
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
        userWordProgressDao.getWordsForReview(
            today = LocalDate.now().toEpochDay(),
            todayStartMillis = LocalDate.now()
                .atStartOfDay()
                .toEpochSecond(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())) * 1000,
            limit = limit,
            level = level?.name
        )
            .map { wordEntities ->
                val favoriteIds = getFavoriteIds()
                wordEntities.map { entity ->
                    val progress = userWordProgressDao.getProgressByWordId(entity.id)
                    entity.toDomain(progress, favoriteIds.contains(entity.id))
                }
            }

    override fun getTodayNewWords(limit: Int, level: WordLevel?): Flow<List<Word>> =
        userWordProgressDao.getLearningWords(limit, level?.name)
            .map { wordEntities ->
                val favoriteIds = getFavoriteIds()
                wordEntities.map { entity ->
                    val progress = userWordProgressDao.getProgressByWordId(entity.id)
                    entity.toDomain(progress, favoriteIds.contains(entity.id))
                }
            }

    override fun getTodayLearnedWords(): Flow<List<Word>> {
        val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())) * 1000
        return userWordProgressDao.getTodayLearnedWords(todayStart)
            .map { wordEntities ->
                val favoriteIds = getFavoriteIds()
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
                    entity.toDomain(progress, true)
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
        if (word.masteryLevel > 0 || word.isLearned) {
            userWordProgressDao.insertOrUpdateProgress(
                word.copy(id = wordId).toProgressEntity()
            )
        }
        if (word.isFavorite) {
            favoriteDao.addFavorite(FavoriteEntity(wordId = wordId))
        }
        return wordId
    }

    override suspend fun insertWords(words: List<Word>) {
        words.forEach { insertWord(it) }
    }

    override suspend fun updateWord(word: Word) {
        wordDao.updateWord(word.toEntity())
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

        val existingProgress = userWordProgressDao.getProgressByWordId(wordId)
        if (existingProgress == null) {
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

    override suspend fun getMasteredWordCount(): Int = userWordProgressDao.getMasteredWordCount()

    override suspend fun getLearningWordCount(): Int = userWordProgressDao.getLearningWordCount()

    override suspend fun getTotalLearnedWordCount(): Int = userWordProgressDao.getTotalLearnedWordCount()

    override suspend fun getUnknownWordCount(): Int {
        val total = wordDao.getWordCount()
        val learned = userWordProgressDao.getTotalLearnedWordCount()
        return total - learned
    }

    override suspend fun getTodayNewWordsCount(): Int {
        val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())) * 1000
        return userWordProgressDao.getTodayNewWordsCount(todayStart)
    }

    override suspend fun getTodayReviewCount(): Int {
        val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())) * 1000
        val todayEnd = todayStart + 24 * 60 * 60 * 1000 - 1
        return reviewRecordDao.getReviewCountByDateRange(todayStart, todayEnd)
    }

    override suspend fun getTotalReviewCount(): Int {
        return reviewRecordDao.getTotalReviewCount()
    }

    override suspend fun getTodayTestCount(): Int {
        val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())) * 1000
        val todayEnd = todayStart + 24 * 60 * 60 * 1000 - 1
        return testRecordDao.getTestCountByDateRange(todayStart, todayEnd)
    }

    override suspend fun getTodayTestAccuracy(): Float {
        val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())) * 1000
        val todayEnd = todayStart + 24 * 60 * 60 * 1000 - 1
        return testRecordDao.getAverageAccuracyByDateRange(todayStart, todayEnd)
    }

    override suspend fun getTodayLearningMinutes(): Int {
        val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())) * 1000
        val todayEnd = todayStart + 24 * 60 * 60 * 1000 - 1
        return checkInRecordDao.getTotalLearningMinutesByDateRange(todayStart, todayEnd)
    }

    override fun searchWords(query: String): Flow<List<Word>> =
        wordDao.searchWords(query).map { wordEntities ->
            buildWordsWithState(wordEntities)
        }

    override fun getWordsByBank(wordBank: String): Flow<List<Word>> =
        combineWordData(wordDao.getWordsByBank(wordBank))

    override fun getWordsNeedingReview(): Flow<List<Word>> {
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
        return userWordProgressDao.getLearnedWords()
            .map { wordEntities ->
                wordEntities.mapNotNull { wordEntity ->
                    val progress = userWordProgressDao.getProgressByWordId(wordEntity.id)
                    val isFavorite = favoriteDao.isFavorite(wordEntity.id)
                    wordEntity.toDomain(progress, isFavorite)
                }
            }
    }

    override suspend fun recordWordLearning(wordId: Long, level: String, masteryLevel: Int) {
        val learningRecord = LearningRecordEntity(
            wordId = wordId,
            level = level,
            masteryLevel = masteryLevel,
            isMastered = masteryLevel >= 4
        )
        learningRecordDao.insertRecord(learningRecord)
    }

    override fun getWordLearningRecords(wordId: Long): Flow<List<LearningRecordEntity>> {
        return learningRecordDao.getRecordsByWordId(wordId)
    }

    override fun getLearningRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<LearningRecordEntity>> {
        return learningRecordDao.getRecordsByDateRange(startTime, endTime)
    }

    override fun getLearningRecordsByLevel(level: String): Flow<List<LearningRecordEntity>> {
        return learningRecordDao.getRecordsByLevel(level)
    }

    // 复习记录相关方法

    override suspend fun recordWordReview(wordId: Long, level: String, masteryLevel: Int, isCorrect: Boolean, reviewResult: String?, durationSeconds: Int) {
        val reviewRecord = ReviewRecordEntity(
            wordId = wordId,
            level = level,
            masteryLevel = masteryLevel,
            durationSeconds = durationSeconds,
            isCorrect = isCorrect,
            reviewResult = reviewResult
        )
        reviewRecordDao.insertRecord(reviewRecord)
    }

    override fun getWordReviewRecords(wordId: Long): Flow<List<ReviewRecordEntity>> {
        return reviewRecordDao.getRecordsByWordId(wordId)
    }

    override fun getReviewRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<ReviewRecordEntity>> {
        return reviewRecordDao.getRecordsByDateRange(startTime, endTime)
    }

    // 测试记录相关方法

    override suspend fun recordTest(testType: String, totalQuestions: Int, correctCount: Int, accuracy: Float, durationSeconds: Int, questionCount: Int, source: String?) {
        val testRecord = TestRecordEntity(
            testType = testType,
            totalQuestions = totalQuestions,
            correctCount = correctCount,
            accuracy = accuracy,
            durationSeconds = durationSeconds,
            questionCount = questionCount,
            source = source
        )
        testRecordDao.insertRecord(testRecord)
    }

    override fun getAllTestRecords(): Flow<List<TestRecordEntity>> {
        return testRecordDao.getAllRecords()
    }

    override fun getTestRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<TestRecordEntity>> {
        return testRecordDao.getRecordsByDateRange(startTime, endTime)
    }

    // 打卡记录相关方法

    override suspend fun recordCheckIn(date: Long, learningMinutes: Int, wordsLearned: Int, wordsReviewed: Int) {
        val totalDays = checkInRecordDao.getTotalCheckInDays() + 1
        val currentStreak = checkInRecordDao.getCurrentStreak() ?: 0
        val newStreak = currentStreak + 1
        
        val checkInRecord = CheckInRecordEntity(
            checkInDate = date,
            streak = newStreak,
            totalDays = totalDays,
            learningMinutes = learningMinutes,
            wordsLearned = wordsLearned,
            wordsReviewed = wordsReviewed
        )
        checkInRecordDao.insertRecord(checkInRecord)
    }

    override suspend fun getCheckInRecord(date: Long): CheckInRecordEntity? {
        return checkInRecordDao.getRecordByDate(date)
    }

    override fun getAllCheckInRecords(): Flow<List<CheckInRecordEntity>> {
        return checkInRecordDao.getAllRecords()
    }

    override suspend fun getCurrentStreak(): Int {
        return checkInRecordDao.getCurrentStreak() ?: 0
    }

    override suspend fun getLongestStreak(): Int {
        return checkInRecordDao.getLongestStreak()
    }

    override suspend fun getTotalCheckInDays(): Int {
        return checkInRecordDao.getTotalCheckInDays()
    }
}
