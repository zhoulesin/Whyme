package com.zhoulesin.whyme.domain.usecase

import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.domain.model.ReviewResult
import com.zhoulesin.whyme.domain.model.ReviewIntervals
import com.zhoulesin.whyme.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * 单词相关用例
 */
class GetWordsForLearningUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(): Flow<List<Word>> = wordRepository.getTodayNewWords(10)
}

class GetWordsForReviewUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(): Flow<List<Word>> = wordRepository.getWordsForReview()
}

class GetFavoriteWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(): Flow<List<Word>> = wordRepository.getFavoriteWords()
}

class UpdateWordReviewUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(wordId: Long, result: ReviewResult) {
        val word = wordRepository.getWordById(wordId) ?: return
        val newLevel = when (result) {
            ReviewResult.AGAIN -> 0
            ReviewResult.HARD -> (word.masteryLevel - 1).coerceAtLeast(0)
            ReviewResult.GOOD -> word.masteryLevel + 1
            ReviewResult.EASY -> (word.masteryLevel + 2).coerceAtMost(5)
        }
        val intervalDays = when (newLevel) {
            0 -> ReviewIntervals.LEVEL_0
            1 -> ReviewIntervals.LEVEL_1
            2 -> ReviewIntervals.LEVEL_2
            3 -> ReviewIntervals.LEVEL_3
            4 -> ReviewIntervals.LEVEL_4
            else -> ReviewIntervals.LEVEL_5
        }
        val nextReviewDate = LocalDate.now().plusDays(intervalDays.toLong())
        wordRepository.updateMasteryLevel(wordId, newLevel, nextReviewDate)
    }
}

class ToggleFavoriteUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(wordId: Long) {
        wordRepository.toggleFavorite(wordId)
    }
}

class SearchWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(query: String): Flow<List<Word>> = wordRepository.searchWords(query)
}
