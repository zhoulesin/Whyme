package com.zhoulesin.whyme.ui.wordbank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.domain.model.LevelProgress
import com.zhoulesin.whyme.domain.model.UserWordBankSettings
import com.zhoulesin.whyme.domain.model.WordLevel
import com.zhoulesin.whyme.domain.repository.WordBankRepository
import com.zhoulesin.whyme.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WordBankUiState(
    val settings: UserWordBankSettings = UserWordBankSettings(),
    val currentLevel: WordLevel = WordLevel.DEFAULT,
    val enabledLevels: Set<WordLevel> = setOf(WordLevel.DEFAULT),
    val levelProgressMap: Map<WordLevel, LevelProgress> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class WordBankViewModel @Inject constructor(
    private val wordBankRepository: WordBankRepository,
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WordBankUiState())
    val uiState: StateFlow<WordBankUiState> = _uiState.asStateFlow()

    init {
        loadWordBankSettings()
        loadLevelProgress()
    }

    private fun loadWordBankSettings() {
        viewModelScope.launch {
            combine(
                wordBankRepository.getSettings(),
                wordBankRepository.getCurrentLevel(),
                wordBankRepository.getEnabledLevels()
            ) { settings, currentLevel, enabledLevels ->
                Triple(settings, currentLevel, enabledLevels)
            }.collect { (settings, currentLevel, enabledLevels) ->
                _uiState.update { state ->
                    state.copy(
                        settings = settings,
                        currentLevel = currentLevel,
                        enabledLevels = enabledLevels,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadLevelProgress() {
        viewModelScope.launch {
            val progressMap = mutableMapOf<WordLevel, LevelProgress>()
            for (level in WordLevel.entries) {
                val total = wordRepository.getWordCount()
                val mastered = wordRepository.getMasteredWordCount()
                val learned = wordRepository.getLearningWordCount()
                progressMap[level] = LevelProgress(
                    level = level,
                    totalWords = total,
                    learnedWords = learned,
                    masteredWords = mastered
                )
            }
            _uiState.update { it.copy(levelProgressMap = progressMap) }
        }
    }

    fun setCurrentLevel(level: WordLevel) {
        viewModelScope.launch {
            wordBankRepository.setCurrentLevel(level)
        }
    }

    fun setLevelEnabled(level: WordLevel, enabled: Boolean) {
        viewModelScope.launch {
            wordBankRepository.setLevelEnabled(level, enabled)
        }
    }

    fun getCurrentLevelProgress(): LevelProgress? {
        return _uiState.value.levelProgressMap[_uiState.value.currentLevel]
    }

    fun getLevelProgress(level: WordLevel): LevelProgress? {
        return _uiState.value.levelProgressMap[level]
    }
}
