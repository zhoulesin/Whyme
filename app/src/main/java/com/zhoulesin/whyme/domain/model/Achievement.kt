package com.zhoulesin.whyme.domain.model

/**
 * 用户成就/徽章
 */
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: AchievementIcon,
    val achievementType: AchievementType,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val progress: Float = 0f,  // 0.0 - 1.0
    val requirement: Int = 0,    // 达成条件数值
    val current: Int = 0        // 当前进度
)

/**
 * 成就图标类型
 */
enum class AchievementIcon {
    STAR,           // 星星
    TROPHY,         // 奖杯
    MEDAL,          // 勋章
    FIRE,           // 火焰
    DIAMOND,        // 钻石
    CROWN,          // 皇冠
    BOOK,           // 书本
    LIGHTNING       // 闪电
}

/**
 * 成就类型
 */
enum class AchievementType {
    LEARNING,       // 学习类
    STREAK,         // 连续类
    MASTERY,        // 掌握类
    SPECIAL         // 特殊类
}

/**
 * 预定义成就列表
 */
object Achievements {
    // 学习类成就
    val FIRST_LEARN = Achievement(
        id = "first_learn",
        name = "初学者",
        description = "完成首次学习",
        icon = AchievementIcon.STAR,
        requirement = 1,
        achievementType = AchievementType.LEARNING
    )

    val LEARN_10 = Achievement(
        id = "learn_10",
        name = "小试牛刀",
        description = "学习10个单词",
        icon = AchievementIcon.BOOK,
        requirement = 10,
        achievementType = AchievementType.LEARNING
    )

    val LEARN_50 = Achievement(
        id = "learn_50",
        name = "渐入佳境",
        description = "学习50个单词",
        icon = AchievementIcon.MEDAL,
        requirement = 50,
        achievementType = AchievementType.LEARNING
    )

    val LEARN_100 = Achievement(
        id = "learn_100",
        name = "单词达人",
        description = "学习100个单词",
        icon = AchievementIcon.TROPHY,
        requirement = 100,
        achievementType = AchievementType.LEARNING
    )

    val LEARN_500 = Achievement(
        id = "learn_500",
        name = "词汇专家",
        description = "学习500个单词",
        icon = AchievementIcon.DIAMOND,
        requirement = 500,
        achievementType = AchievementType.LEARNING
    )

    val LEARN_1000 = Achievement(
        id = "learn_1000",
        name = "词汇大师",
        description = "学习1000个单词",
        icon = AchievementIcon.CROWN,
        requirement = 1000,
        achievementType = AchievementType.LEARNING
    )

    // 连续打卡类成就
    val STREAK_3 = Achievement(
        id = "streak_3",
        name = "初露头角",
        description = "连续学习3天",
        icon = AchievementIcon.FIRE,
        requirement = 3,
        achievementType = AchievementType.STREAK
    )

    val STREAK_7 = Achievement(
        id = "streak_7",
        name = "一周坚持",
        description = "连续学习7天",
        icon = AchievementIcon.FIRE,
        requirement = 7,
        achievementType = AchievementType.STREAK
    )

    val STREAK_30 = Achievement(
        id = "streak_30",
        name = "月度学习者",
        description = "连续学习30天",
        icon = AchievementIcon.FIRE,
        requirement = 30,
        achievementType = AchievementType.STREAK
    )

    val STREAK_100 = Achievement(
        id = "streak_100",
        name = "百日战士",
        description = "连续学习100天",
        icon = AchievementIcon.LIGHTNING,
        requirement = 100,
        achievementType = AchievementType.STREAK
    )

    // 掌握类成就
    val MASTERY_10 = Achievement(
        id = "mastery_10",
        name = "初窥门径",
        description = "掌握10个单词",
        icon = AchievementIcon.STAR,
        requirement = 10,
        achievementType = AchievementType.MASTERY
    )

    val MASTERY_100 = Achievement(
        id = "mastery_100",
        name = "熟能生巧",
        description = "掌握100个单词",
        icon = AchievementIcon.MEDAL,
        requirement = 100,
        achievementType = AchievementType.MASTERY
    )

    val MASTERY_500 = Achievement(
        id = "mastery_500",
        name = "炉火纯青",
        description = "掌握500个单词",
        icon = AchievementIcon.TROPHY,
        requirement = 500,
        achievementType = AchievementType.MASTERY
    )

    // 特殊成就
    val PERFECT_DAY = Achievement(
        id = "perfect_day",
        name = "完美一天",
        description = "单日学习正确率100%",
        icon = AchievementIcon.DIAMOND,
        requirement = 1,
        achievementType = AchievementType.SPECIAL
    )

    val SPEED_DAY = Achievement(
        id = "speed_day",
        name = "速度之星",
        description = "一天内学习并复习50个单词",
        icon = AchievementIcon.LIGHTNING,
        requirement = 50,
        achievementType = AchievementType.SPECIAL
    )

    val ALL_ACHIEVEMENTS = listOf(
        // 学习类
        FIRST_LEARN, LEARN_10, LEARN_50, LEARN_100, LEARN_500, LEARN_1000,
        // 连续类
        STREAK_3, STREAK_7, STREAK_30, STREAK_100,
        // 掌握类
        MASTERY_10, MASTERY_100, MASTERY_500,
        // 特殊类
        PERFECT_DAY, SPEED_DAY
    )

    // 获取指定类型的成就
    fun getByType(type: AchievementType): List<Achievement> =
        ALL_ACHIEVEMENTS.filter { it.achievementType == type }

    // 根据ID获取成就
    fun getById(id: String): Achievement? =
        ALL_ACHIEVEMENTS.find { it.id == id }
}
