package com.zhoulesin.whyme.domain.model

/**
 * 用户成就/徽章
 */
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

/**
 * 预定义成就列表
 */
object Achievements {
    val FIRST_WORD = Achievement(
        id = "first_word",
        name = "初学者",
        description = "学习第一个单词",
        iconName = "Star"
    )
    val TEN_WORDS = Achievement(
        id = "ten_words",
        name = "小试牛刀",
        description = "学习10个单词",
        iconName = "EmojiEvents"
    )
    val HUNDRED_WORDS = Achievement(
        id = "hundred_words",
        name = "单词达人",
        description = "学习100个单词",
        iconName = "MilitaryTech"
    )
    val WEEK_STREAK = Achievement(
        id = "week_streak",
        name = "一周坚持",
        description = "连续学习7天",
        iconName = "LocalFireDepartment"
    )
    val MONTH_STREAK = Achievement(
        id = "month_streak",
        name = "月度学习者",
        description = "连续学习30天",
        iconName = "WorkspacePremium"
    )

    val ALL_ACHIEVEMENTS = listOf(
        FIRST_WORD, TEN_WORDS, HUNDRED_WORDS, WEEK_STREAK, MONTH_STREAK
    )
}
