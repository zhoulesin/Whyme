package com.zhoulesin.whyme.data.repository

import com.zhoulesin.whyme.data.datastore.CurrentUser
import com.zhoulesin.whyme.data.local.dao.LevelProgressDao
import com.zhoulesin.whyme.data.local.dao.UserWordBankSettingsDao
import com.zhoulesin.whyme.data.local.entity.LevelProgressEntity
import com.zhoulesin.whyme.data.local.entity.UserWordBankSettingsEntity
import com.zhoulesin.whyme.domain.model.LevelProgress
import com.zhoulesin.whyme.domain.model.UserWordBankSettings
import com.zhoulesin.whyme.domain.model.WordLevel
import com.zhoulesin.whyme.domain.repository.WordBankRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordBankRepositoryImpl @Inject constructor(
    private val settingsDao: UserWordBankSettingsDao,
    private val levelProgressDao: LevelProgressDao
) : WordBankRepository {

    private fun uid(): String = CurrentUser.userId

    override fun getSettings(): Flow<UserWordBankSettings> {
        return settingsDao.getSettings(uid()).map { entity ->
            entity?.toDomain() ?: UserWordBankSettings()
        }
    }

    override fun getCurrentLevel(): Flow<WordLevel> {
        return settingsDao.getSettings(uid()).map { entity ->
            entity?.currentLevel?.let { WordLevel.fromName(it) } ?: WordLevel.DEFAULT
        }
    }

    override fun getEnabledLevels(): Flow<Set<WordLevel>> {
        return settingsDao.getSettings(uid()).map { entity ->
            entity?.parseEnabledLevels() ?: setOf(WordLevel.DEFAULT)
        }
    }

    override suspend fun setCurrentLevel(level: WordLevel) {
        ensureSettingsExist()
        settingsDao.updateCurrentLevel(uid(), level.name)
    }

    override suspend fun setLevelEnabled(level: WordLevel, enabled: Boolean) {
        ensureSettingsExist()
        val current = settingsDao.getSettingsOnce(uid()) ?: return
        val enabledLevels = current.parseEnabledLevels().toMutableSet()

        if (enabled) {
            enabledLevels.add(level)
        } else {
            enabledLevels.remove(level)
            if (current.currentLevel == level.name && enabledLevels.isNotEmpty()) {
                settingsDao.updateCurrentLevel(uid(), enabledLevels.first().name)
            }
        }

        settingsDao.updateEnabledLevels(uid(), enabledLevels.toJsonString())
    }

    override fun getAllLevelProgress(): Flow<List<LevelProgress>> {
        return levelProgressDao.getAllProgress(uid()).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getLevelProgress(level: WordLevel): LevelProgress? {
        return levelProgressDao.getProgress(uid(), level.name)?.toDomain()
    }

    override suspend fun updateLevelProgress(level: WordLevel, learnedWords: Int, masteredWords: Int) {
        val today = LocalDate.now().toEpochDay()
        levelProgressDao.updateProgress(uid(), level.name, learnedWords, masteredWords, today)
    }

    override suspend fun incrementLearnedWords(level: WordLevel) {
        val today = LocalDate.now().toEpochDay()
        levelProgressDao.incrementLearnedWords(uid(), level.name, today)
    }

    override suspend fun incrementMasteredWords(level: WordLevel) {
        levelProgressDao.incrementMasteredWords(uid(), level.name)
    }

    override suspend fun initializeLevelProgress(totalWordsMap: Map<WordLevel, Int>) {
        WordLevel.entries.forEach { level ->
            val total = totalWordsMap[level] ?: 0
            val existing = levelProgressDao.getProgress(uid(), level.name)
            if (existing == null) {
                levelProgressDao.insertOrUpdate(
                    LevelProgressEntity(
                        level = level.name,
                        userId = uid(),
                        totalWords = total
                    )
                )
            } else if (existing.totalWords != total) {
                levelProgressDao.updateTotalWords(uid(), level.name, total)
            }
        }
    }

    private suspend fun ensureSettingsExist() {
        if (settingsDao.getSettingsOnce(uid()) == null) {
            settingsDao.insertOrUpdate(UserWordBankSettingsEntity(userId = uid()))
        }
    }

    private fun UserWordBankSettingsEntity.toDomain(): UserWordBankSettings {
        return UserWordBankSettings(
            id = 0,
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

    private fun LevelProgressEntity.toDomain(): LevelProgress {
        return LevelProgress(
            level = WordLevel.fromName(level),
            totalWords = totalWords,
            learnedWords = learnedWords,
            masteredWords = masteredWords,
            lastStudyDate = lastStudyDate?.let { LocalDate.ofEpochDay(it) }
        )
    }
}
