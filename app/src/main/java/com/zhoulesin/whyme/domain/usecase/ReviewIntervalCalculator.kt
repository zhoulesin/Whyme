package com.zhoulesin.whyme.domain.usecase

import com.zhoulesin.whyme.domain.model.ReviewResult
import com.zhoulesin.whyme.domain.model.ReviewIntervals
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 复习间隔计算器
 * 基于艾宾浩斯遗忘曲线和 SM-2 算法
 */
@Singleton
class ReviewIntervalCalculator @Inject constructor() {

    /**
     * 计算下次复习日期
     *
     * @param currentLevel 当前掌握等级 (0-5)
     * @param result 复习结果
     * @param reviewCount 已复习次数
     * @return 下次复习日期
     */
    fun calculateNextReviewDate(
        currentLevel: Int,
        result: ReviewResult,
        reviewCount: Int
    ): LocalDate {
        val baseInterval = ReviewIntervals.getInterval(currentLevel)
        val adjustedInterval = adjustIntervalByResult(baseInterval, result)
        val adjustedIntervalByCount = adjustIntervalByReviewCount(adjustedInterval, reviewCount)

        return when (result) {
            ReviewResult.AGAIN -> {
                // 不认识：当天晚些时候再次出现（简化处理：明天）
                LocalDate.now().plusDays(1)
            }
            ReviewResult.HARD -> {
                // 模糊：保持原计划间隔
                LocalDate.now().plusDays(adjustedIntervalByCount.toLong())
            }
            ReviewResult.GOOD -> {
                // 认识：根据当前等级计算正常间隔
                val goodInterval = (adjustedIntervalByCount * 1.2).toInt().coerceAtLeast(1)
                LocalDate.now().plusDays(goodInterval.toLong())
            }
            ReviewResult.EASY -> {
                // 太简单：大幅延长间隔
                val easyInterval = (adjustedIntervalByCount * 2.0).toInt().coerceAtLeast(adjustedIntervalByCount + 1)
                LocalDate.now().plusDays(easyInterval.toLong())
            }
        }
    }

    /**
     * 根据复习结果调整间隔
     */
    private fun adjustIntervalByResult(baseInterval: Int, result: ReviewResult): Int {
        return when (result) {
            ReviewResult.AGAIN -> (baseInterval * 0.5).toInt().coerceAtLeast(1)
            ReviewResult.HARD -> baseInterval
            ReviewResult.GOOD -> (baseInterval * 1.2).toInt().coerceAtLeast(1)
            ReviewResult.EASY -> (baseInterval * 2.0).toInt().coerceAtLeast(1)
        }
    }

    /**
     * 根据复习次数调整间隔
     * 复习次数越多，间隔应该越长
     */
    private fun adjustIntervalByReviewCount(interval: Int, reviewCount: Int): Int {
        val multiplier = when {
            reviewCount <= 1 -> 1.0
            reviewCount <= 3 -> 1.2
            reviewCount <= 5 -> 1.5
            else -> 2.0
        }
        return (interval * multiplier).toInt().coerceAtLeast(1)
    }

    /**
     * 计算新的掌握等级
     *
     * @param currentLevel 当前等级
     * @param result 复习结果
     * @return 新的等级 (0-5)
     */
    fun calculateNewLevel(currentLevel: Int, result: ReviewResult): Int {
        return when (result) {
            ReviewResult.AGAIN -> {
                // 不认识：降级到0（重新学习）
                0
            }
            ReviewResult.HARD -> {
                // 模糊：等级-1，但不低于0
                (currentLevel - 1).coerceAtLeast(0)
            }
            ReviewResult.GOOD -> {
                // 认识：等级+1，最高5
                (currentLevel + 1).coerceAtMost(5)
            }
            ReviewResult.EASY -> {
                // 太简单：等级+2，跳过中间等级
                (currentLevel + 2).coerceAtMost(5)
            }
        }
    }

    /**
     * 判断是否需要将单词标记为已学习
     */
    fun shouldMarkAsLearned(currentLevel: Int, result: ReviewResult): Boolean {
        return currentLevel == 0 && result != ReviewResult.AGAIN
    }

    /**
     * 判断是否已达到精通状态
     */
    fun isMastered(level: Int): Boolean {
        return level >= 4
    }

    /**
     * 获取等级描述
     */
    fun getLevelDescription(level: Int): String {
        return when (level) {
            0 -> "陌生"
            1 -> "学习中"
            2 -> "熟悉"
            3 -> "理解"
            4 -> "掌握"
            5 -> "精通"
            else -> "未知"
        }
    }

    /**
     * 计算学习进度百分比
     * 基于到精通还需多少次正确复习
     */
    fun calculateProgressPercentage(currentLevel: Int): Float {
        return (currentLevel.toFloat() / 5f) * 100
    }
}
