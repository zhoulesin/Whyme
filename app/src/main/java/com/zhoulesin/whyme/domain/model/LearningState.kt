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
 * 学习状态
 */
sealed class LearningState {
    data object Idle : LearningState()
    data class Learning(val currentWord: Word, val index: Int, val total: Int) : LearningState()
    data class Testing(val currentWord: Word, val questionType: QuestionType, val index: Int, val total: Int) : LearningState()
    data class Completed(val learned: Int, val reviewed: Int, val accuracy: Float) : LearningState()
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
