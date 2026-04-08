package com.zhoulesin.whyme.domain.model

import java.time.LocalDate

/**
 * 词库级别枚举
 */
enum class WordLevel(
    val displayName: String,
    val shortName: String,
    val description: String,
    val order: Int
) {
    L1_PRIMARY("小学水平", "小学", "小学阶段基础词汇", 1),
    L2_JUNIOR("初中水平", "初中", "初中阶段核心词汇", 2),
    L3_SENIOR("高中水平", "高中", "高中阶段必考词汇", 3),
    L4_CET4("大学四级", "四级", "大学英语四级词汇", 4),
    L5_CET6("大学六级", "六级", "大学英语六级词汇", 5),
    L6_IELTS_TOEFL("雅思/托福", "雅思", "留学考试高频词汇", 6),
    L7_KAOYAN("考研必备", "考研", "研究生入学考试词汇", 7),
    L8_GRE("专业英语", "GRE", "美国研究生入学考试词汇", 8);

    companion object {
        /**
         * 默认级别：高中水平
         */
        val DEFAULT = L3_SENIOR

        /**
         * 根据名称获取级别
         */
        fun fromName(name: String): WordLevel {
            return entries.find { it.name == name } ?: DEFAULT
        }
    }
}

/**
 * 用户词库设置
 */
data class UserWordBankSettings(
    val id: Long = 0,
    val currentLevel: WordLevel = WordLevel.DEFAULT,
    val enabledLevels: Set<WordLevel> = setOf(WordLevel.DEFAULT)
)

/**
 * 级别学习进度
 */
data class LevelProgress(
    val level: WordLevel,
    val totalWords: Int = 0,
    val learnedWords: Int = 0,
    val masteredWords: Int = 0,
    val lastStudyDate: LocalDate? = null
) {
    /**
     * 学习进度百分比
     */
    val learningProgress: Float
        get() = if (totalWords > 0) learnedWords.toFloat() / totalWords else 0f

    /**
     * 掌握进度百分比
     */
    val masteryProgress: Float
        get() = if (totalWords > 0) masteredWords.toFloat() / totalWords else 0f

    /**
     * 级别完成状态
     */
    val isCompleted: Boolean
        get() = totalWords > 0 && learnedWords >= totalWords
}
