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

@Singleton
class AppInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesDataStore: PreferencesDataStore,
    private val userDatabaseManager: UserDatabaseManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initializeIfNeeded() {
        scope.launch {
            val isInitialized = preferencesDataStore.isWordDatabaseInitialized.first()
            if (!isInitialized) {
                val wordDao = userDatabaseManager.getWordDao()
                val wordCount = wordDao.getWordCount()
                if (wordCount == 0) {
                    val initializer = DatabaseInitializer(context)
                    initializer.initializeWordDatabase(wordDao)
                }
                preferencesDataStore.setWordDatabaseInitialized()
            }
        }
    }
}
