package com.zhoulesin.whyme.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zhoulesin.whyme.data.local.dao.FavoriteDao
import com.zhoulesin.whyme.data.local.dao.UserWordBankSettingsDao
import com.zhoulesin.whyme.data.local.dao.UserWordProgressDao
import com.zhoulesin.whyme.data.local.dao.WordDao
import com.zhoulesin.whyme.data.local.dao.LearningRecordDao
import com.zhoulesin.whyme.data.local.dao.DailyLearningRecordDao
import com.zhoulesin.whyme.data.local.dao.ReviewRecordDao
import com.zhoulesin.whyme.data.local.dao.TestRecordDao
import com.zhoulesin.whyme.data.local.dao.CheckInRecordDao
import com.zhoulesin.whyme.data.local.entity.FavoriteEntity
import com.zhoulesin.whyme.data.local.entity.UserWordBankSettingsEntity
import com.zhoulesin.whyme.data.local.entity.UserWordProgressEntity
import com.zhoulesin.whyme.data.local.entity.WordEntity
import com.zhoulesin.whyme.data.local.entity.LearningRecordEntity
import com.zhoulesin.whyme.data.local.entity.DailyLearningRecordEntity
import com.zhoulesin.whyme.data.local.entity.ReviewRecordEntity
import com.zhoulesin.whyme.data.local.entity.TestRecordEntity
import com.zhoulesin.whyme.data.local.entity.CheckInRecordEntity

@Database(
    entities = [
        WordEntity::class,
        UserWordProgressEntity::class,
        FavoriteEntity::class,
        UserWordBankSettingsEntity::class,
        LearningRecordEntity::class,
        DailyLearningRecordEntity::class,
        ReviewRecordEntity::class,
        TestRecordEntity::class,
        CheckInRecordEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun userWordProgressDao(): UserWordProgressDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun userWordBankSettingsDao(): UserWordBankSettingsDao
    abstract fun learningRecordDao(): LearningRecordDao
    abstract fun dailyLearningRecordDao(): DailyLearningRecordDao
    abstract fun reviewRecordDao(): ReviewRecordDao
    abstract fun testRecordDao(): TestRecordDao
    abstract fun checkInRecordDao(): CheckInRecordDao

    companion object {
        fun databaseName(userId: String): String = "whyme_${userId}.db"
    }
}
