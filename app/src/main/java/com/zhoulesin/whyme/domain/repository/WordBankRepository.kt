package com.zhoulesin.whyme.domain.repository

import com.zhoulesin.whyme.domain.model.LevelProgress
import com.zhoulesin.whyme.domain.model.UserWordBankSettings
import com.zhoulesin.whyme.domain.model.WordLevel
import kotlinx.coroutines.flow.Flow

/**
 * 词库仓库接口
 */
interface WordBankRepository {
    
    /**
     * 获取用户词库设置
     */
    fun getSettings(): Flow<UserWordBankSettings>
    
    /**
     * 获取当前学习级别
     */
    fun getCurrentLevel(): Flow<WordLevel>
    
    /**
     * 获取已启用的级别列表
     */
    fun getEnabledLevels(): Flow<Set<WordLevel>>
    
    /**
     * 更新当前学习级别
     */
    suspend fun setCurrentLevel(level: WordLevel)
    
    /**
     * 启用/禁用某个级别
     */
    suspend fun setLevelEnabled(level: WordLevel, enabled: Boolean)
    
    /**
     * 获取所有级别的进度
     */
    fun getAllLevelProgress(): Flow<List<LevelProgress>>
    
    /**
     * 获取指定级别的进度
     */
    suspend fun getLevelProgress(level: WordLevel): LevelProgress?
    
    /**
     * 更新级别进度
     */
    suspend fun updateLevelProgress(level: WordLevel, learnedWords: Int, masteredWords: Int)
    
    /**
     * 增加已学习词数
     */
    suspend fun incrementLearnedWords(level: WordLevel)
    
    /**
     * 增加已掌握词数
     */
    suspend fun incrementMasteredWords(level: WordLevel)
    
    /**
     * 初始化级别进度数据
     */
    suspend fun initializeLevelProgress(totalWordsMap: Map<WordLevel, Int>)
}
