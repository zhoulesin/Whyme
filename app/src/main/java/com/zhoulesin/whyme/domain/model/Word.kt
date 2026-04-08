package com.zhoulesin.whyme.domain.model

import java.time.LocalDate

/**
 * 单词实体
 */
data class Word(
    val id: Long = 0,
    val word: String,
    val phonetic: String,
    val definition: String = "",
    val example: String = "",
    val translation: String,
    val masteryLevel: Int = 0, // 0-5 掌握程度
    val isFavorite: Boolean = false,
    val isLearned: Boolean = false,  // 是否已学习过
    val isNew: Boolean = true,       // 是否新词
    val nextReviewDate: LocalDate? = null,
    val reviewCount: Int = 0,
    val correctCount: Int = 0,       // 正确次数
    val wordBank: String? = null      // 来源词库
) {
    val isMastered: Boolean get() = masteryLevel >= 4
    val needsReview: Boolean get() = nextReviewDate?.let { it <= LocalDate.now() } ?: true

    /**
     * 获取掌握等级描述
     */
    val masteryLevelText: String
        get() = when (masteryLevel) {
            0 -> "陌生"
            1 -> "学习中"
            2 -> "熟悉"
            3 -> "理解"
            4 -> "掌握"
            5 -> "精通"
            else -> "未知"
        }

    /**
     * 正确率
     */
    val accuracy: Float
        get() = if (reviewCount > 0) correctCount.toFloat() / reviewCount else 0f
}
