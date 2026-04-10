package com.zhoulesin.whyme.data.local.dao

import androidx.room.*
import com.zhoulesin.whyme.data.local.entity.UserWordBankSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserWordBankSettingsDao {

    @Query("SELECT * FROM user_word_bank_settings LIMIT 1")
    fun getSettings(): Flow<UserWordBankSettingsEntity?>

    @Query("SELECT * FROM user_word_bank_settings LIMIT 1")
    suspend fun getSettingsOnce(): UserWordBankSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: UserWordBankSettingsEntity)

    @Query("UPDATE user_word_bank_settings SET currentLevel = :level")
    suspend fun updateCurrentLevel(level: String)

    @Query("UPDATE user_word_bank_settings SET enabledLevels = :levels")
    suspend fun updateEnabledLevels(levels: String)
}
