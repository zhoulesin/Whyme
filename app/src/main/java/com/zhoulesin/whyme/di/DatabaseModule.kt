package com.zhoulesin.whyme.di

import com.zhoulesin.whyme.data.local.UserDatabaseManager
import com.zhoulesin.whyme.data.local.dao.FavoriteDao
import com.zhoulesin.whyme.data.local.dao.UserWordBankSettingsDao
import com.zhoulesin.whyme.data.local.dao.UserWordProgressDao
import com.zhoulesin.whyme.data.local.dao.WordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWordDao(userDatabaseManager: UserDatabaseManager): WordDao {
        return userDatabaseManager.getWordDao()
    }

    @Provides
    @Singleton
    fun provideUserWordProgressDao(userDatabaseManager: UserDatabaseManager): UserWordProgressDao {
        return userDatabaseManager.getUserWordProgressDao()
    }

    @Provides
    @Singleton
    fun provideFavoriteDao(userDatabaseManager: UserDatabaseManager): FavoriteDao {
        return userDatabaseManager.getFavoriteDao()
    }

    @Provides
    @Singleton
    fun provideUserWordBankSettingsDao(userDatabaseManager: UserDatabaseManager): UserWordBankSettingsDao {
        return userDatabaseManager.getUserWordBankSettingsDao()
    }
}
