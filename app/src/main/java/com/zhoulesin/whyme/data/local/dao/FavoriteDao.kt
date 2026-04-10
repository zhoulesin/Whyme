package com.zhoulesin.whyme.data.local.dao

import androidx.room.*
import com.zhoulesin.whyme.data.local.entity.FavoriteEntity
import com.zhoulesin.whyme.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("""
        SELECT w.* FROM words w
        INNER JOIN favorites f ON w.id = f.wordId
        WHERE f.userId = :userId
        ORDER BY f.createdAt DESC
    """)
    fun getFavoriteWords(userId: String): Flow<List<WordEntity>>

    @Query("SELECT wordId FROM favorites WHERE userId = :userId ORDER BY createdAt DESC")
    fun getFavoriteWordIds(userId: String): Flow<List<Long>>

    @Query("SELECT wordId FROM favorites WHERE userId = :userId AND wordId IN (:wordIds)")
    suspend fun getFavoriteWordIdsIn(userId: String, wordIds: List<Long>): List<Long>

    @Query("SELECT COUNT(*) > 0 FROM favorites WHERE userId = :userId AND wordId = :wordId")
    suspend fun isFavorite(userId: String, wordId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: FavoriteEntity): Long

    @Query("DELETE FROM favorites WHERE userId = :userId AND wordId = :wordId")
    suspend fun removeFavorite(userId: String, wordId: Long): Int

    @Transaction
    suspend fun toggleFavorite(userId: String, wordId: Long): Boolean {
        return if (isFavorite(userId, wordId)) {
            removeFavorite(userId, wordId)
            false
        } else {
            addFavorite(FavoriteEntity(userId = userId, wordId = wordId))
            true
        }
    }

    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId")
    suspend fun getFavoriteCount(userId: String): Int

    @Query("""
        SELECT w.* FROM words w
        INNER JOIN favorites f ON w.id = f.wordId
        LEFT JOIN user_word_progress p ON w.id = p.wordId AND p.userId = :userId
        WHERE f.userId = :userId
        AND (p.masteryLevel IS NULL OR p.masteryLevel < 4)
        ORDER BY p.masteryLevel ASC, f.createdAt DESC
    """)
    fun getNewWordsBook(userId: String): Flow<List<WordEntity>>

    @Query("DELETE FROM favorites WHERE userId = :userId")
    suspend fun clearAllFavorites(userId: String)
}
