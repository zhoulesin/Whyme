package com.zhoulesin.whyme.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zhoulesin.whyme.data.local.dao.LearningRecordDao
import com.zhoulesin.whyme.data.local.dao.WordDao
import com.zhoulesin.whyme.data.local.entity.LearningRecordEntity
import com.zhoulesin.whyme.data.local.entity.WordEntity

/**
 * 应用数据库
 */
@Database(
    entities = [
        WordEntity::class,
        LearningRecordEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun learningRecordDao(): LearningRecordDao

    companion object {
        const val DATABASE_NAME = "whyme_english_db"
    }
}
