package com.zhoulesin.whyme.domain.model

/**
 * 单词掌握等级
 * 基于艾宾浩斯遗忘曲线设计的6级掌握体系
 */
enum class MasteryLevel(
    val value: Int,
    val description: String,
    val chineseName: String
) {
    UNKNOWN(0, "Unlearned", "陌生"),
    LEARNING(1, "Learning", "学习中"),
    FAMILIAR(2, "Familiar", "熟悉"),
    UNDERSTOOD(3, "Understood", "理解"),
    MASTERED(4, "Mastered", "掌握"),
    PERFECTED(5, "Perfected", "精通");

    companion object {
        fun fromInt(value: Int): MasteryLevel = entries.find { it.value == value } ?: UNKNOWN
    }
}

/**
 * 记忆曲线复习间隔（天）
 * 基于艾宾浩斯遗忘曲线理论
 */
object ReviewIntervals {
    // 各等级对应的基础复习间隔
    const val LEVEL_0 = 0    // 新词，当天复习
    const val LEVEL_1 = 1     // 第1次复习后
    const val LEVEL_2 = 2     // 第2次复习后
    const val LEVEL_3 = 4     // 第3次复习后
    const val LEVEL_4 = 7     // 第4次复习后
    const val LEVEL_5 = 15    // 第5次复习后
    const val LEVEL_6 = 30    // 完全掌握

    // 各等级到达精通需要的复习次数
    const val REVIEWS_TO_MASTER = 6

    // 获取当前等级对应的间隔
    fun getInterval(level: Int): Int = when (level) {
        0 -> LEVEL_0
        1 -> LEVEL_1
        2 -> LEVEL_2
        3 -> LEVEL_3
        4 -> LEVEL_4
        5 -> LEVEL_5
        else -> LEVEL_6
    }
}

/**
 * 每日学习限制
 */
object DailyLimits {
    const val DEFAULT_NEW_WORDS_LIMIT = 10   // 默认每日新词上限
    const val DEFAULT_REVIEW_LIMIT = 50      // 默认每日复习上限
    const val MAX_NEW_WORDS_LIMIT = 30       // 最大每日新词上限
    const val MAX_REVIEW_LIMIT = 100        // 最大每日复习上限
}
