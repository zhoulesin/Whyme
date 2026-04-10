package com.zhoulesin.whyme.data.local.dao

import androidx.room.*
import com.zhoulesin.whyme.data.local.entity.LevelProgressEntity
import com.zhoulesin.whyme.data.local.entity.UserWordBankSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserWordBankSettingsDao {

    @Query("SELECT * FROM user_word_bank_settings WHERE userId = :userId")
    fun getSettings(userId: String): Flow<UserWordBankSettingsEntity?>

    @Query("SELECT * FROM user_word_bank_settings WHERE userId = :userId")
    suspend fun getSettingsOnce(userId: String): UserWordBankSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: UserWordBankSettingsEntity)

    @Query("UPDATE user_word_bank_settings SET currentLevel = :level WHERE userId = :userId")
    suspend fun updateCurrentLevel(userId: String, level: String)

    @Query("UPDATE user_word_bank_settings SET enabledLevels = :levels WHERE userId = :userId")
    suspend fun updateEnabledLevels(userId: String, levels: String)
}

@Dao
interface LevelProgressDao {

    @Query("SELECT * FROM level_progress WHERE userId = :userId")
    fun getAllProgress(userId: String): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE userId = :userId AND level = :level")
    suspend fun getProgress(userId: String, level: String): LevelProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: LevelProgressEntity)

    @Query("UPDATE level_progress SET learnedWords = :learned, masteredWords = :mastered, lastStudyDate = :date WHERE userId = :userId AND level = :level")
    suspend fun updateProgress(userId: String, level: String, learned: Int, mastered: Int, date: Long?)

    @Query("UPDATE level_progress SET totalWords = :total WHERE userId = :userId AND level = :level")
    suspend fun updateTotalWords(userId: String, level: String, total: Int)

    @Query("UPDATE level_progress SET learnedWords = learnedWords + 1, lastStudyDate = :date WHERE userId = :userId AND level = :level")
    suspend fun incrementLearnedWords(userId: String, level: String, date: Long)

    @Query("UPDATE level_progress SET masteredWords = masteredWords + 1 WHERE userId = :userId AND level = :level")
    suspend fun incrementMasteredWords(userId: String, level: String)
}
