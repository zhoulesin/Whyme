package com.zhoulesin.whyme.domain.model

import java.time.LocalDate

/**
 * 单词实体
 */
data class Word(
    val id: Long = 0,
    val word: String,
    val phonetic: String,
    val definition: String,
    val example: String,
    val translation: String,
    val masteryLevel: Int = 0, // 0-5 掌握程度
    val isFavorite: Boolean = false,
    val nextReviewDate: LocalDate? = null,
    val reviewCount: Int = 0
) {
    val isMastered: Boolean get() = masteryLevel >= 4
    val needsReview: Boolean get() = nextReviewDate?.let { it <= LocalDate.now() } ?: true
}

/**
 * 记忆曲线复习间隔（天）
 */
object ReviewIntervals {
    val LEVEL_0 = 1    // 刚学习
    val LEVEL_1 = 2    // 第1次复习
    val LEVEL_2 = 4    // 第2次复习
    val LEVEL_3 = 7    // 第3次复习
    val LEVEL_4 = 15   // 第4次复习
    val LEVEL_5 = 30   // 完全掌握
}
