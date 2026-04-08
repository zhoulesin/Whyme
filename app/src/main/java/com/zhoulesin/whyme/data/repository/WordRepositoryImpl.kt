package com.zhoulesin.whyme.data.repository

import com.zhoulesin.whyme.data.local.dao.WordDao
import com.zhoulesin.whyme.data.local.entity.toDomain
import com.zhoulesin.whyme.data.local.entity.toDomainList
import com.zhoulesin.whyme.data.local.entity.toEntity
import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 单词仓库实现
 */
@Singleton
class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao
) : WordRepository {

    override fun getAllWords(): Flow<List<Word>> =
        wordDao.getAllWords().map { it.toDomainList() }

    override fun getWordsForReview(): Flow<List<Word>> =
        wordDao.getWordsForReview(LocalDate.now().toEpochDay()).map { it.toDomainList() }

    override fun getTodayNewWords(limit: Int): Flow<List<Word>> =
        wordDao.getNewWords(limit).map { it.toDomainList() }

    override fun getFavoriteWords(): Flow<List<Word>> =
        wordDao.getFavoriteWords().map { it.toDomainList() }

    override suspend fun getWordById(id: Long): Word? =
        wordDao.getWordById(id)?.toDomain()

    override suspend fun insertWord(word: Word): Long =
        wordDao.insertWord(word.toEntity())

    override suspend fun insertWords(words: List<Word>) =
        wordDao.insertWords(words.map { it.toEntity() })

    override suspend fun updateWord(word: Word) =
        wordDao.updateWord(word.toEntity())

    override suspend fun deleteWord(word: Word) =
        wordDao.deleteWord(word.toEntity())

    override suspend fun updateMasteryLevel(wordId: Long, level: Int, nextReviewDate: LocalDate) =
        wordDao.updateMasteryLevel(wordId, level, nextReviewDate.toEpochDay(), System.currentTimeMillis())

    override suspend fun toggleFavorite(wordId: Long) =
        wordDao.toggleFavorite(wordId)

    override suspend fun getWordCount(): Int =
        wordDao.getWordCount()

    override suspend fun getMasteredWordCount(): Int =
        wordDao.getMasteredWordCount()

    override fun searchWords(query: String): Flow<List<Word>> =
        wordDao.searchWords(query).map { it.toDomainList() }
}
