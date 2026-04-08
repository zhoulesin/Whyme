package com.zhoulesin.whyme.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zhoulesin.whyme.data.local.dao.LearningRecordDao
import com.zhoulesin.whyme.data.local.dao.WordDao
import com.zhoulesin.whyme.data.local.dao.UserWordBankSettingsDao
import com.zhoulesin.whyme.data.local.dao.LevelProgressDao
import com.zhoulesin.whyme.data.local.entity.LearningRecordEntity
import com.zhoulesin.whyme.data.local.entity.WordEntity
import com.zhoulesin.whyme.data.local.entity.UserWordBankSettingsEntity
import com.zhoulesin.whyme.data.local.entity.LevelProgressEntity

/**
 * 应用数据库
 */
@Database(
    entities = [
        WordEntity::class,
        LearningRecordEntity::class,
        UserWordBankSettingsEntity::class,
        LevelProgressEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun learningRecordDao(): LearningRecordDao
    abstract fun userWordBankSettingsDao(): UserWordBankSettingsDao
    abstract fun levelProgressDao(): LevelProgressDao

    companion object {
        const val DATABASE_NAME = "whyme_english_db"
    }
}
