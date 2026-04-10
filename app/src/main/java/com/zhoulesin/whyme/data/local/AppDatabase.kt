package com.zhoulesin.whyme.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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

@Database(
    entities = [
        WordEntity::class,
        UserWordProgressEntity::class,
        FavoriteEntity::class,
        LearningRecordEntity::class,
        UserWordBankSettingsEntity::class,
        LevelProgressEntity::class
    ],
    version = 2,
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
