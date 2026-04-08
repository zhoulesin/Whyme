package com.zhoulesin.whyme.data.local.dao

import androidx.room.*
import com.zhoulesin.whyme.data.local.entity.FavoriteEntity
import com.zhoulesin.whyme.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 收藏数据访问对象
 */
@Dao
interface FavoriteDao {

    /**
     * 获取所有收藏的单词（关联查询）
     */
    @Query("""
        SELECT w.* FROM words w
        INNER JOIN favorites f ON w.id = f.wordId
        ORDER BY f.createdAt DESC
    """)
    fun getFavoriteWords(): Flow<List<WordEntity>>

    /**
     * 获取收藏的单词ID列表
     */
    @Query("SELECT wordId FROM favorites ORDER BY createdAt DESC")
    fun getFavoriteWordIds(): Flow<List<Long>>

    /**
     * 检查单词是否已收藏
     */
    @Query("SELECT COUNT(*) > 0 FROM favorites WHERE wordId = :wordId")
    suspend fun isFavorite(wordId: Long): Boolean

    /**
     * 添加收藏
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: FavoriteEntity): Long

    /**
     * 移除收藏
     */
    @Query("DELETE FROM favorites WHERE wordId = :wordId")
    suspend fun removeFavorite(wordId: Long): Int

    /**
     * 切换收藏状态
     * @return true 表示已收藏，false 表示已取消收藏
     */
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

    /**
     * 获取收藏数量
     */
    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoriteCount(): Int

    /**
     * 获取收藏的未掌握单词（生词本）
     * 需要关联 user_word_progress 表
     */
    @Query("""
        SELECT w.* FROM words w
        INNER JOIN favorites f ON w.id = f.wordId
        LEFT JOIN user_word_progress p ON w.id = p.wordId
        WHERE p.masteryLevel IS NULL OR p.masteryLevel < 4
        ORDER BY p.masteryLevel ASC, f.createdAt DESC
    """)
    fun getNewWordsBook(): Flow<List<WordEntity>>

    /**
     * 删除所有收藏
     */
    @Query("DELETE FROM favorites")
    suspend fun clearAllFavorites()
}
