package com.zhoulesin.whyme.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.zhoulesin.whyme.domain.model.DailyGoal
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * 用户偏好设置 DataStore
 */
@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val WORDS_PER_DAY = intPreferencesKey("words_per_day")
        val REVIEW_PER_DAY = intPreferencesKey("review_per_day")
        val MINUTES_PER_DAY = intPreferencesKey("minutes_per_day")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LONGEST_STREAK = intPreferencesKey("longest_streak")
        val LAST_LEARNING_DATE = longPreferencesKey("last_learning_date")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val WORD_DATABASE_INITIALIZED = booleanPreferencesKey("word_database_initialized")
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
        var streak = 0
        context.dataStore.data.collect { preferences ->
            streak = preferences[PreferencesKeys.CURRENT_STREAK] ?: 0
        }
        return streak
    }

    suspend fun updateStreak(currentStreak: Int, longestStreak: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_STREAK] = currentStreak
            preferences[PreferencesKeys.LONGEST_STREAK] = longestStreak
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
}
