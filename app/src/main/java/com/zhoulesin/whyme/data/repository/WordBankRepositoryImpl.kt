package com.zhoulesin.whyme.data.repository

import com.zhoulesin.whyme.data.local.dao.UserWordBankSettingsDao
import com.zhoulesin.whyme.data.local.entity.UserWordBankSettingsEntity
import com.zhoulesin.whyme.domain.model.UserWordBankSettings
import com.zhoulesin.whyme.domain.model.WordLevel
import com.zhoulesin.whyme.domain.repository.WordBankRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordBankRepositoryImpl @Inject constructor(
    private val settingsDao: UserWordBankSettingsDao
) : WordBankRepository {

    override fun getSettings(): Flow<UserWordBankSettings> {
        return settingsDao.getSettings().map { entity ->
            entity?.toDomain() ?: UserWordBankSettings()
        }
    }

    override fun getCurrentLevel(): Flow<WordLevel> {
        return settingsDao.getSettings().map { entity ->
            entity?.currentLevel?.let { WordLevel.fromName(it) } ?: WordLevel.DEFAULT
        }
    }

    override fun getEnabledLevels(): Flow<Set<WordLevel>> {
        return settingsDao.getSettings().map { entity ->
            entity?.parseEnabledLevels() ?: setOf(WordLevel.DEFAULT)
        }
    }

    override suspend fun setCurrentLevel(level: WordLevel) {
        ensureSettingsExist()
        settingsDao.updateCurrentLevel(level.name)
    }

    override suspend fun setLevelEnabled(level: WordLevel, enabled: Boolean) {
        ensureSettingsExist()
        val current = settingsDao.getSettingsOnce() ?: return
        val enabledLevels = current.parseEnabledLevels().toMutableSet()

        if (enabled) {
            enabledLevels.add(level)
        } else {
            enabledLevels.remove(level)
            if (current.currentLevel == level.name && enabledLevels.isNotEmpty()) {
                settingsDao.updateCurrentLevel(enabledLevels.first().name)
            }
        }

        settingsDao.updateEnabledLevels(enabledLevels.toJsonString())
    }

    private suspend fun ensureSettingsExist() {
        if (settingsDao.getSettingsOnce() == null) {
            settingsDao.insertOrUpdate(UserWordBankSettingsEntity())
        }
    }

    private fun UserWordBankSettingsEntity.toDomain(): UserWordBankSettings {
        return UserWordBankSettings(
            id = id,
            currentLevel = WordLevel.fromName(currentLevel),
            enabledLevels = parseEnabledLevels()
        )
    }

    private fun UserWordBankSettingsEntity.parseEnabledLevels(): Set<WordLevel> {
        return try {
            val jsonArray = JSONArray(enabledLevels)
            (0 until jsonArray.length()).mapNotNull { i ->
                try {
                    WordLevel.fromName(jsonArray.getString(i))
                } catch (e: Exception) {
                    null
                }
            }.toSet()
        } catch (e: Exception) {
            setOf(WordLevel.DEFAULT)
        }
    }

    private fun Set<WordLevel>.toJsonString(): String {
        val jsonArray = JSONArray()
        forEach { jsonArray.put(it.name) }
        return jsonArray.toString()
    }
}
