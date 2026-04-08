package com.zhoulesin.whyme.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zhoulesin.whyme.data.local.dao.FavoriteDao
import com.zhoulesin.whyme.data.local.dao.LearningRecordDao
import com.zhoulesin.whyme.data.local.dao.UserWordBankSettingsDao
import com.zhoulesin.whyme.data.local.dao.UserWordProgressDao
import com.zhoulesin.whyme.data.local.dao.WordDao
import com.zhoulesin.whyme.data.local.entity.FavoriteEntity
import com.zhoulesin.whyme.data.local.entity.LevelProgressEntity
import com.zhoulesin.whyme.data.local.entity.LearningRecordEntity
import com.zhoulesin.whyme.data.local.entity.UserWordBankSettingsEntity
import com.zhoulesin.whyme.data.local.entity.UserWordProgressEntity
import com.zhoulesin.whyme.data.local.entity.WordEntity

/**
 * 应用数据库
 * 注意：修改表结构后需要清除数据重新安装
 */
@Database(
    entities = [
        WordEntity::class,              // 单词基础信息表
        UserWordProgressEntity::class,  // 用户学习进度表
        FavoriteEntity::class,          // 收藏表
        LearningRecordEntity::class,    // 学习记录表
        UserWordBankSettingsEntity::class,  // 用户词库设置表
        LevelProgressEntity::class      // 级别学习进度表
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun userWordProgressDao(): UserWordProgressDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun learningRecordDao(): LearningRecordDao
    abstract fun userWordBankSettingsDao(): UserWordBankSettingsDao
    abstract fun levelProgressDao(): com.zhoulesin.whyme.data.local.dao.LevelProgressDao

    companion object {
        const val DATABASE_NAME = "whyme_english_db"
    }
}
