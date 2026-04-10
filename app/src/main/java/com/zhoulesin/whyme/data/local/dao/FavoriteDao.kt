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
        ORDER BY f.createdAt DESC
    """)
    fun getFavoriteWords(): Flow<List<WordEntity>>

    @Query("SELECT wordId FROM favorites ORDER BY createdAt DESC")
    fun getFavoriteWordIds(): Flow<List<Long>>

    @Query("SELECT wordId FROM favorites WHERE wordId IN (:wordIds)")
    suspend fun getFavoriteWordIdsIn(wordIds: List<Long>): List<Long>

    @Query("SELECT COUNT(*) > 0 FROM favorites WHERE wordId = :wordId")
    suspend fun isFavorite(wordId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: FavoriteEntity): Long

    @Query("DELETE FROM favorites WHERE wordId = :wordId")
    suspend fun removeFavorite(wordId: Long): Int

    @Transaction
    suspend fun toggleFavorite(wordId: Long): Boolean {
        return if (isFavorite(wordId)) {
            removeFavorite(wordId)
            false
        } else {
            addFavorite(FavoriteEntity(wordId = wordId))
            true
        }
    }

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoriteCount(): Int

    @Query("""
        SELECT w.* FROM words w
        INNER JOIN favorites f ON w.id = f.wordId
        LEFT JOIN user_word_progress p ON w.id = p.wordId
        WHERE (p.masteryLevel IS NULL OR p.masteryLevel < 4)
        ORDER BY p.masteryLevel ASC, f.createdAt DESC
    """)
    fun getNewWordsBook(): Flow<List<WordEntity>>

    @Query("DELETE FROM favorites")
    suspend fun clearAllFavorites()
}
