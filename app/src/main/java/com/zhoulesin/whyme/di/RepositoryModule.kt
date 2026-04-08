package com.zhoulesin.whyme.di

import com.zhoulesin.whyme.data.repository.LearningRepositoryImpl
import com.zhoulesin.whyme.data.repository.WordRepositoryImpl
import com.zhoulesin.whyme.domain.repository.LearningRepository
import com.zhoulesin.whyme.domain.repository.WordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 仓库依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWordRepository(
        wordRepositoryImpl: WordRepositoryImpl
    ): WordRepository

    @Binds
    @Singleton
    abstract fun bindLearningRepository(
        learningRepositoryImpl: LearningRepositoryImpl
    ): LearningRepository
}
