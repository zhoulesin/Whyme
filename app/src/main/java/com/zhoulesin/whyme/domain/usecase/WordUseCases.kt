package com.zhoulesin.whyme.domain.usecase

import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.domain.model.ReviewResult
import com.zhoulesin.whyme.domain.model.DailyLimits
import com.zhoulesin.whyme.domain.model.WordLevel
import com.zhoulesin.whyme.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

/**
 * 单词相关用例
 */
class GetWordsForLearningUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    /**
     * 获取今日待学习的新词
     * @param limit 每日新词上限
     * @param level 词库级别（可选）
     */
    operator fun invoke(limit: Int = DailyLimits.DEFAULT_NEW_WORDS_LIMIT, level: WordLevel? = null): Flow<List<Word>> =
        wordRepository.getTodayNewWords(limit, level)
}

class GetWordsForReviewUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    /**
     * 获取今日需要复习的单词
     * @param limit 每日复习上限
     * @param level 词库级别（可选）
     */
    operator fun invoke(limit: Int = DailyLimits.DEFAULT_REVIEW_LIMIT, level: WordLevel? = null): Flow<List<Word>> =
        wordRepository.getWordsForReview(limit, level)
}

class GetFavoriteWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(level: WordLevel? = null): Flow<List<Word>> =
        wordRepository.getFavoriteWords().map { words ->
            if (level != null) words.filter { it.level == level } else words
        }
}

class UpdateWordReviewUseCase @Inject constructor(
    private val wordRepository: WordRepository,
    private val calculator: ReviewIntervalCalculator
) {
    /**
     * 更新单词复习信息
     * 根据复习结果计算新的掌握等级和下次复习日期
     */
    suspend operator fun invoke(wordId: Long, result: ReviewResult) {
        val word = wordRepository.getWordById(wordId) ?: return

        // 计算新的掌握等级
        val newLevel = calculator.calculateNewLevel(word.masteryLevel, result)

        // 计算下次复习日期
        val nextReviewDate = calculator.calculateNextReviewDate(
            currentLevel = word.masteryLevel,
            result = result,
            reviewCount = word.reviewCount
        )

        // 判断是否标记为已学习
        val isLearned = calculator.shouldMarkAsLearned(word.masteryLevel, result)

        // 更新单词信息
        wordRepository.updateWordReview(
            wordId = wordId,
            masteryLevel = newLevel,
            nextReviewDate = nextReviewDate,
            isLearned = isLearned,
            reviewResult = result
        )
    }
}

class ToggleFavoriteUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(wordId: Long): Boolean {
        return wordRepository.toggleFavorite(wordId)
    }
}

class SearchWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(query: String): Flow<List<Word>> = wordRepository.searchWords(query)
}
