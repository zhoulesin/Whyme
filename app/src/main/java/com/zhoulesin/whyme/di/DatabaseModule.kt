package com.zhoulesin.whyme.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zhoulesin.whyme.data.local.AppDatabase
import com.zhoulesin.whyme.data.local.DatabaseInitializer
import com.zhoulesin.whyme.data.local.dao.LearningRecordDao
import com.zhoulesin.whyme.data.local.dao.WordDao
import com.zhoulesin.whyme.data.local.dao.UserWordBankSettingsDao
import com.zhoulesin.whyme.data.local.dao.LevelProgressDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

/**
 * 数据库依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        wordDaoProvider: Provider<WordDao>
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // 数据库首次创建时初始化词库
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        val initializer = DatabaseInitializer(context)
                        val wordDao = wordDaoProvider.get()
                        initializer.initializeWordDatabase(wordDao)
                    }
                }
            })
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideWordDao(database: AppDatabase): WordDao {
        return database.wordDao()
    }

    @Provides
    @Singleton
    fun provideLearningRecordDao(database: AppDatabase): LearningRecordDao {
        return database.learningRecordDao()
    }

    @Provides
    @Singleton
    fun provideUserWordBankSettingsDao(database: AppDatabase): UserWordBankSettingsDao {
        return database.userWordBankSettingsDao()
    }

    @Provides
    @Singleton
    fun provideLevelProgressDao(database: AppDatabase): LevelProgressDao {
        return database.levelProgressDao()
    }
}
