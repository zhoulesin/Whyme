package com.zhoulesin.whyme.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.zhoulesin.whyme.domain.model.DailyGoal
import com.zhoulesin.whyme.domain.model.LearningMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * 学习进度数据
 */
data class LearningProgress(
    val wordIds: List<Long>,
    val currentIndex: Int,
    val mode: String,
    val startTime: Long,
    val wordsReviewed: Int,
    val correctCount: Int
)

/**
 * 用户偏好设置 DataStore
 */
@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        // 每日目标
        val WORDS_PER_DAY = intPreferencesKey("words_per_day")
        val REVIEW_PER_DAY = intPreferencesKey("review_per_day")
        val MINUTES_PER_DAY = intPreferencesKey("minutes_per_day")

        // 连续打卡
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LONGEST_STREAK = intPreferencesKey("longest_streak")
        val LAST_LEARNING_DATE = longPreferencesKey("last_learning_date")

        // 状态标记
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val WORD_DATABASE_INITIALIZED = booleanPreferencesKey("word_database_initialized")

        // 学习进度
        val LEARNING_PROGRESS_MODE = stringPreferencesKey("learning_progress_mode")
        val LEARNING_PROGRESS_WORD_IDS = stringPreferencesKey("learning_progress_word_ids")
        val LEARNING_PROGRESS_CURRENT_INDEX = intPreferencesKey("learning_progress_current_index")
        val LEARNING_PROGRESS_START_TIME = longPreferencesKey("learning_progress_start_time")
        val LEARNING_PROGRESS_WORDS_REVIEWED = intPreferencesKey("learning_progress_words_reviewed")
        val LEARNING_PROGRESS_CORRECT_COUNT = intPreferencesKey("learning_progress_correct_count")

        // 成就
        val ACHIEVEMENT_FIRST_LEARN = booleanPreferencesKey("achievement_first_learn")
        val ACHIEVEMENT_LEARN_100 = booleanPreferencesKey("achievement_learn_100")
        val ACHIEVEMENT_CONTINUE_7_DAYS = booleanPreferencesKey("achievement_continue_7_days")
        val ACHIEVEMENT_PERFECT_DAY = booleanPreferencesKey("achievement_perfect_day")
    }

    val dailyGoal: Flow<DailyGoal> = context.dataStore.data.map { preferences ->
        DailyGoal(
            wordsPerDay = preferences[PreferencesKeys.WORDS_PER_DAY] ?: 10,
            reviewPerDay = preferences[PreferencesKeys.REVIEW_PER_DAY] ?: 20,
            minutesPerDay = preferences[PreferencesKeys.MINUTES_PER_DAY] ?: 15
        )
    }

    suspend fun updateDailyGoal(goal: DailyGoal) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WORDS_PER_DAY] = goal.wordsPerDay
            preferences[PreferencesKeys.REVIEW_PER_DAY] = goal.reviewPerDay
            preferences[PreferencesKeys.MINUTES_PER_DAY] = goal.minutesPerDay
        }
    }

    suspend fun getCurrentStreak(): Int {
        return context.dataStore.data.first()[PreferencesKeys.CURRENT_STREAK] ?: 0
    }

    suspend fun getLongestStreak(): Int {
        return context.dataStore.data.first()[PreferencesKeys.LONGEST_STREAK] ?: 0
    }

    suspend fun updateStreak(currentStreak: Int, longestStreak: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_STREAK] = currentStreak
            preferences[PreferencesKeys.LONGEST_STREAK] = longestStreak
            preferences[PreferencesKeys.LAST_LEARNING_DATE] = System.currentTimeMillis()
        }
    }

    /**
     * 检查并更新连续打卡
     * 应该在每次学习完成时调用
     */
    suspend fun checkAndUpdateStreak() {
        context.dataStore.edit { preferences ->
            val lastDate = preferences[PreferencesKeys.LAST_LEARNING_DATE] ?: 0L
            val currentStreak = preferences[PreferencesKeys.CURRENT_STREAK] ?: 0
            val longestStreak = preferences[PreferencesKeys.LONGEST_STREAK] ?: 0

            val today = LocalDate.now()
            val lastLearningDay = LocalDate.ofEpochDay(lastDate / (24 * 60 * 60 * 1000))
            val yesterday = today.minusDays(1)

            val newStreak = when {
                lastDate == 0L -> 1 // 首次学习
                lastLearningDay == today -> currentStreak // 今天已学习，保持不变
                lastLearningDay == yesterday -> currentStreak + 1 // 昨天学习，今天继续
                else -> 1 // 中断了，从头开始
            }

            preferences[PreferencesKeys.CURRENT_STREAK] = newStreak
            preferences[PreferencesKeys.LONGEST_STREAK] = maxOf(longestStreak, newStreak)
            preferences[PreferencesKeys.LAST_LEARNING_DATE] = System.currentTimeMillis()
        }
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = true
        }
    }

    /**
     * 检查词库是否已初始化
     */
    val isWordDatabaseInitialized: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WORD_DATABASE_INITIALIZED] ?: false
    }

    /**
     * 标记词库已初始化
     */
    suspend fun setWordDatabaseInitialized() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WORD_DATABASE_INITIALIZED] = true
        }
    }

    // ==================== 学习进度保存 ====================

    /**
     * 保存当前学习进度
     */
    suspend fun saveLearningProgress(progress: LearningProgress) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LEARNING_PROGRESS_MODE] = progress.mode
            preferences[PreferencesKeys.LEARNING_PROGRESS_WORD_IDS] = progress.wordIds.joinToString(",")
            preferences[PreferencesKeys.LEARNING_PROGRESS_CURRENT_INDEX] = progress.currentIndex
            preferences[PreferencesKeys.LEARNING_PROGRESS_START_TIME] = progress.startTime
            preferences[PreferencesKeys.LEARNING_PROGRESS_WORDS_REVIEWED] = progress.wordsReviewed
            preferences[PreferencesKeys.LEARNING_PROGRESS_CORRECT_COUNT] = progress.correctCount
        }
    }

    /**
     * 获取保存的学习进度
     */
    suspend fun getLearningProgress(): LearningProgress? {
        val preferences = context.dataStore.data.first()
        val mode = preferences[PreferencesKeys.LEARNING_PROGRESS_MODE] ?: return null
        val wordIdsStr = preferences[PreferencesKeys.LEARNING_PROGRESS_WORD_IDS] ?: return null

        return LearningProgress(
            wordIds = wordIdsStr.split(",").mapNotNull { it.toLongOrNull() },
            currentIndex = preferences[PreferencesKeys.LEARNING_PROGRESS_CURRENT_INDEX] ?: 0,
            mode = mode,
            startTime = preferences[PreferencesKeys.LEARNING_PROGRESS_START_TIME] ?: 0,
            wordsReviewed = preferences[PreferencesKeys.LEARNING_PROGRESS_WORDS_REVIEWED] ?: 0,
            correctCount = preferences[PreferencesKeys.LEARNING_PROGRESS_CORRECT_COUNT] ?: 0
        )
    }

    /**
     * 清除学习进度
     */
    suspend fun clearLearningProgress() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.LEARNING_PROGRESS_MODE)
            preferences.remove(PreferencesKeys.LEARNING_PROGRESS_WORD_IDS)
            preferences.remove(PreferencesKeys.LEARNING_PROGRESS_CURRENT_INDEX)
            preferences.remove(PreferencesKeys.LEARNING_PROGRESS_START_TIME)
            preferences.remove(PreferencesKeys.LEARNING_PROGRESS_WORDS_REVIEWED)
            preferences.remove(PreferencesKeys.LEARNING_PROGRESS_CORRECT_COUNT)
        }
    }

    /**
     * 是否有未完成的学习进度
     */
    suspend fun hasUnfinishedProgress(): Boolean {
        return getLearningProgress() != null
    }

    // ==================== 成就系统 ====================

    /**
     * 解锁首次学习成就
     */
    suspend fun unlockFirstLearnAchievement() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACHIEVEMENT_FIRST_LEARN] = true
        }
    }

    /**
     * 解锁学习100词成就
     */
    suspend fun unlockLearn100Achievement() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACHIEVEMENT_LEARN_100] = true
        }
    }

    /**
     * 解锁连续7天成就
     */
    suspend fun unlockContinue7DaysAchievement() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACHIEVEMENT_CONTINUE_7_DAYS] = true
        }
    }

    /**
     * 解锁完美一天成就
     */
    suspend fun unlockPerfectDayAchievement() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACHIEVEMENT_PERFECT_DAY] = true
        }
    }

    /**
     * 获取已解锁的成就列表
     */
    fun getUnlockedAchievements(): Flow<List<String>> = context.dataStore.data.map { preferences ->
        val achievements = mutableListOf<String>()
        if (preferences[PreferencesKeys.ACHIEVEMENT_FIRST_LEARN] == true) {
            achievements.add("first_learn")
        }
        if (preferences[PreferencesKeys.ACHIEVEMENT_LEARN_100] == true) {
            achievements.add("learn_100")
        }
        if (preferences[PreferencesKeys.ACHIEVEMENT_CONTINUE_7_DAYS] == true) {
            achievements.add("continue_7_days")
        }
        if (preferences[PreferencesKeys.ACHIEVEMENT_PERFECT_DAY] == true) {
            achievements.add("perfect_day")
        }
        achievements
    }
}
