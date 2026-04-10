package com.zhoulesin.whyme.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zhoulesin.whyme.data.local.dao.FavoriteDao
import com.zhoulesin.whyme.data.local.dao.UserWordBankSettingsDao
import com.zhoulesin.whyme.data.local.dao.UserWordProgressDao
import com.zhoulesin.whyme.data.local.dao.WordDao
import com.zhoulesin.whyme.data.local.entity.FavoriteEntity
import com.zhoulesin.whyme.data.local.entity.UserWordBankSettingsEntity
import com.zhoulesin.whyme.data.local.entity.UserWordProgressEntity
import com.zhoulesin.whyme.data.local.entity.WordEntity

@Database(
    entities = [
        WordEntity::class,
        UserWordProgressEntity::class,
        FavoriteEntity::class,
        UserWordBankSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun userWordProgressDao(): UserWordProgressDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun userWordBankSettingsDao(): UserWordBankSettingsDao

    companion object {
        fun databaseName(userId: String): String = "whyme_${userId}.db"
    }
}
