package com.zhoulesin.whyme.di

import android.content.Context
import androidx.room.Room
import com.zhoulesin.whyme.data.local.AppDatabase
import com.zhoulesin.whyme.data.local.dao.LearningRecordDao
import com.zhoulesin.whyme.data.local.dao.WordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
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
}
