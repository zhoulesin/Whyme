package com.zhoulesin.whyme.data.local.dao

import androidx.room.*
import com.zhoulesin.whyme.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 单词数据访问对象
 */
@Dao
interface WordDao {
    @Query("SELECT * FROM words ORDER BY createdAt DESC")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE nextReviewDate IS NOT NULL AND nextReviewDate <= :today ORDER BY nextReviewDate ASC")
    fun getWordsForReview(today: Long): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE masteryLevel = 0 ORDER BY createdAt ASC LIMIT :limit")
    fun getNewWords(limit: Int): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Long): WordEntity?

    @Query("SELECT * FROM words WHERE word LIKE '%' || :query || '%' OR translation LIKE '%' || :query || '%'")
    fun searchWords(query: String): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int

    @Query("SELECT COUNT(*) FROM words WHERE masteryLevel >= 4")
    suspend fun getMasteredWordCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    @Update
    suspend fun updateWord(word: WordEntity)

    @Delete
    suspend fun deleteWord(word: WordEntity)

    @Query("UPDATE words SET masteryLevel = :level, nextReviewDate = :nextReviewDate, reviewCount = reviewCount + 1, lastReviewedAt = :timestamp WHERE id = :wordId")
    suspend fun updateMasteryLevel(wordId: Long, level: Int, nextReviewDate: Long, timestamp: Long)

    @Query("UPDATE words SET isFavorite = NOT isFavorite WHERE id = :wordId")
    suspend fun toggleFavorite(wordId: Long)
}
