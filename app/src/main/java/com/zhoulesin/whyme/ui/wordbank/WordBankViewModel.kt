package com.zhoulesin.whyme.ui.wordbank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.domain.model.LevelProgress
import com.zhoulesin.whyme.domain.model.UserWordBankSettings
import com.zhoulesin.whyme.domain.model.WordLevel
import com.zhoulesin.whyme.domain.repository.WordBankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 词库级别 UI 状态
 */
data class WordBankUiState(
    val settings: UserWordBankSettings = UserWordBankSettings(),
    val currentLevel: WordLevel = WordLevel.DEFAULT,
    val enabledLevels: Set<WordLevel> = setOf(WordLevel.DEFAULT),
    val levelProgressMap: Map<WordLevel, LevelProgress> = emptyMap(),
    val isLoading: Boolean = true
)

/**
 * 词库级别 ViewModel
 */
@HiltViewModel
class WordBankViewModel @Inject constructor(
    private val wordBankRepository: WordBankRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WordBankUiState())
    val uiState: StateFlow<WordBankUiState> = _uiState.asStateFlow()
    
    init {
        loadWordBankSettings()
        observeLevelProgress()
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
    
    private fun observeLevelProgress() {
        viewModelScope.launch {
            wordBankRepository.getAllLevelProgress().collect { progressList ->
                val progressMap = progressList.associateBy { it.level }
                _uiState.update { it.copy(levelProgressMap = progressMap) }
            }
        }
    }
    
    /**
     * 切换当前学习级别
     */
    fun setCurrentLevel(level: WordLevel) {
        viewModelScope.launch {
            wordBankRepository.setCurrentLevel(level)
        }
    }
    
    /**
     * 启用/禁用某个级别
     */
    fun setLevelEnabled(level: WordLevel, enabled: Boolean) {
        viewModelScope.launch {
            wordBankRepository.setLevelEnabled(level, enabled)
        }
    }
    
    /**
     * 获取当前级别的进度
     */
    fun getCurrentLevelProgress(): LevelProgress? {
        return _uiState.value.levelProgressMap[_uiState.value.currentLevel]
    }
    
    /**
     * 获取指定级别的进度
     */
    fun getLevelProgress(level: WordLevel): LevelProgress? {
        return _uiState.value.levelProgressMap[level]
    }
}
