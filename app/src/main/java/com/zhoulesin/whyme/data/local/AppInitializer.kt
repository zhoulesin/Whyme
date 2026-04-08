package com.zhoulesin.whyme.data.local

import android.content.Context
import com.zhoulesin.whyme.data.local.dao.WordDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 应用初始化管理器
 * 负责在应用启动时检查并初始化词库
 */
@Singleton
class AppInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesDataStore: PreferencesDataStore,
    private val wordDao: WordDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * 初始化应用数据
     * 调用时机：MainActivity 或 HomeScreen 首次加载时
     */
    fun initializeIfNeeded() {
        scope.launch {
            // 检查是否需要初始化词库
            val isInitialized = preferencesDataStore.isWordDatabaseInitialized.first()
            val wordCount = wordDao.getWordCount()

            if (!isInitialized || wordCount == 0) {
                // 执行词库初始化
                val initializer = DatabaseInitializer(context)
                initializer.initializeWordDatabase(wordDao)
                preferencesDataStore.setWordDatabaseInitialized()
            }
        }
    }
}
