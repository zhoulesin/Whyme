package com.zhoulesin.whyme.data.local.dao

import androidx.room.*
import com.zhoulesin.whyme.data.local.entity.LevelProgressEntity
import com.zhoulesin.whyme.data.local.entity.UserWordBankSettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * 用户词库设置 DAO
 */
@Dao
interface UserWordBankSettingsDao {
    
    /**
     * 获取用户词库设置
     */
    @Query("SELECT * FROM user_word_bank_settings WHERE id = 1")
    fun getSettings(): Flow<UserWordBankSettingsEntity?>
    
    /**
     * 获取当前设置（首次创建默认设置）
     */
    @Query("SELECT * FROM user_word_bank_settings WHERE id = 1")
    suspend fun getSettingsOnce(): UserWordBankSettingsEntity?
    
    /**
     * 插入或更新设置
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: UserWordBankSettingsEntity)
    
    /**
     * 更新当前级别
     */
    @Query("UPDATE user_word_bank_settings SET currentLevel = :level WHERE id = 1")
    suspend fun updateCurrentLevel(level: String)
    
    /**
     * 更新已启用的级别
     */
    @Query("UPDATE user_word_bank_settings SET enabledLevels = :levels WHERE id = 1")
    suspend fun updateEnabledLevels(levels: String)
}

/**
 * 级别进度 DAO
 */
@Dao
interface LevelProgressDao {
    
    /**
     * 获取所有级别的进度
     */
    @Query("SELECT * FROM level_progress")
    fun getAllProgress(): Flow<List<LevelProgressEntity>>
    
    /**
     * 获取指定级别的进度
     */
    @Query("SELECT * FROM level_progress WHERE level = :level")
    suspend fun getProgress(level: String): LevelProgressEntity?
    
    /**
     * 插入或更新进度
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: LevelProgressEntity)
    
    /**
     * 更新指定级别的进度
     */
    @Query("UPDATE level_progress SET learnedWords = :learned, masteredWords = :mastered, lastStudyDate = :date WHERE level = :level")
    suspend fun updateProgress(level: String, learned: Int, mastered: Int, date: Long?)
    
    /**
     * 更新总词数
     */
    @Query("UPDATE level_progress SET totalWords = :total WHERE level = :level")
    suspend fun updateTotalWords(level: String, total: Int)
    
    /**
     * 增加已学习词数
     */
    @Query("UPDATE level_progress SET learnedWords = learnedWords + 1, lastStudyDate = :date WHERE level = :level")
    suspend fun incrementLearnedWords(level: String, date: Long)
    
    /**
     * 增加已掌握词数
     */
    @Query("UPDATE level_progress SET masteredWords = masteredWords + 1 WHERE level = :level")
    suspend fun incrementMasteredWords(level: String)
}
