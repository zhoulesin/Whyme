package com.zhoulesin.whyme.domain.repository

import com.zhoulesin.whyme.domain.model.UserWordBankSettings
import com.zhoulesin.whyme.domain.model.WordLevel
import kotlinx.coroutines.flow.Flow

interface WordBankRepository {

    fun getSettings(): Flow<UserWordBankSettings>

    fun getCurrentLevel(): Flow<WordLevel>

    fun getEnabledLevels(): Flow<Set<WordLevel>>

    suspend fun setCurrentLevel(level: WordLevel)

    suspend fun setLevelEnabled(level: WordLevel, enabled: Boolean)
}
