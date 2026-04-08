package com.zhoulesin.whyme.domain.model

/**
 * 复习结果
 */
enum class ReviewResult {
    AGAIN,      // 不认识，需要立即复习
    HARD,       // 模糊，稍后复习
    GOOD,       // 认识
    EASY        // 太简单
}

/**
 * 学习模式
 */
enum class LearningMode {
    NEW_WORD,   // 新词学习
    REVIEW,     // 复习模式
    QUIZ        // 测试模式
}

/**
 * 学习状态
 */
sealed class LearningState {
    data object Idle : LearningState()

    data class Learning(
        val currentWord: Word,
        val index: Int,
        val total: Int,
        val mode: LearningMode = LearningMode.NEW_WORD
    ) : LearningState()

    data class Testing(
        val currentWord: Word,
        val questionType: QuestionType,
        val index: Int,
        val total: Int,
        val options: List<QuizOption> = emptyList()
    ) : LearningState()

    data class QuizResult(
        val correctCount: Int,
        val totalCount: Int,
        val accuracy: Float,
        val mode: LearningMode
    ) : LearningState()

    data class Completed(
        val learned: Int,
        val reviewed: Int,
        val accuracy: Float
    ) : LearningState()

    data class Error(val message: String) : LearningState()
}

/**
 * 测试题型
 */
enum class QuestionType {
    WORD_TO_CHINESE,    // 英文选中文
    CHINESE_TO_WORD,    // 中文选英文
    SPELLING            // 拼写单词
}

/**
 * 测试选项
 */
data class QuizOption(
    val text: String,
    val isCorrect: Boolean
)

/**
 * 测试会话结果
 */
data class QuizSessionResult(
    val mode: LearningMode,
    val startTime: Long,
    val endTime: Long,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wordResults: List<WordQuizResult>
) {
    val accuracy: Float get() = if (totalQuestions > 0) correctAnswers.toFloat() / totalQuestions else 0f
}

/**
 * 单词答题结果
 */
data class WordQuizResult(
    val wordId: Long,
    val selectedAnswer: String,
    val isCorrect: Boolean,
    val responseTimeMs: Long
)
