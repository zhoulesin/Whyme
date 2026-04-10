package com.zhoulesin.whyme.di

import com.zhoulesin.whyme.data.repository.WordRepositoryImpl
import com.zhoulesin.whyme.data.repository.WordBankRepositoryImpl
import com.zhoulesin.whyme.domain.repository.WordRepository
import com.zhoulesin.whyme.domain.repository.WordBankRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
    abstract fun bindWordBankRepository(
        wordBankRepositoryImpl: WordBankRepositoryImpl
    ): WordBankRepository
}
