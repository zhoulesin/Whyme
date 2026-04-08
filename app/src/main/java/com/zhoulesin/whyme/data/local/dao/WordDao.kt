package com.zhoulesin.whyme.data.local.dao

import androidx.room.*
import com.zhoulesin.whyme.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 单词数据访问对象
 * 简化版本：只处理单词基础信息
 */
@Dao
interface WordDao {

    @Query("SELECT * FROM words ORDER BY word ASC")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Long): WordEntity?

    @Query("SELECT * FROM words WHERE word = :word LIMIT 1")
    suspend fun getWordByWord(word: String): WordEntity?

    @Query("SELECT * FROM words WHERE word LIKE '%' || :query || '%' OR translation LIKE '%' || :query || '%'")
    fun searchWords(query: String): Flow<List<WordEntity>>

    /**
     * 根据词库获取单词
     */
    @Query("SELECT * FROM words WHERE wordBank = :wordBank ORDER BY word ASC")
    fun getWordsByBank(wordBank: String): Flow<List<WordEntity>>

    /**
     * 根据级别获取单词
     */
    @Query("SELECT * FROM words WHERE level = :level ORDER BY word ASC")
    fun getWordsByLevel(level: String): Flow<List<WordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    @Update
    suspend fun updateWord(word: WordEntity)

    @Delete
    suspend fun deleteWord(word: WordEntity)

    @Query("DELETE FROM words WHERE id = :id")
    suspend fun deleteWordById(id: Long)

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int

    @Query("SELECT COUNT(*) FROM words WHERE level = :level")
    suspend fun getWordCountByLevel(level: String): Int

    @Query("DELETE FROM words")
    suspend fun deleteAllWords()
}
