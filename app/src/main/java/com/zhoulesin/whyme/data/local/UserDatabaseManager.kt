package com.zhoulesin.whyme.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zhoulesin.whyme.data.local.dao.FavoriteDao
import com.zhoulesin.whyme.data.local.dao.UserWordBankSettingsDao
import com.zhoulesin.whyme.data.local.dao.UserWordProgressDao
import com.zhoulesin.whyme.data.local.dao.WordDao
import com.zhoulesin.whyme.data.local.dao.LearningRecordDao
import com.zhoulesin.whyme.data.local.dao.ReviewRecordDao
import com.zhoulesin.whyme.data.local.dao.TestRecordDao
import com.zhoulesin.whyme.data.local.dao.CheckInRecordDao
import com.zhoulesin.whyme.data.datastore.UserManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDatabaseManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var currentDatabase: AppDatabase? = null

    @Volatile
    private var currentUserId: String? = null

    @Synchronized
    fun getDatabase(): AppDatabase {
        val userId = UserManager.getInstance(context).userId
        val db = currentDatabase
        if (db != null && currentUserId == userId && db.isOpen) {
            return db
        }
        currentDatabase?.close()
        val newDb = createDatabase(userId)
        currentDatabase = newDb
        currentUserId = userId
        return newDb
    }

    private fun createDatabase(userId: String): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            AppDatabase.databaseName(userId)
        )
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    scope.launch {
                        val initializer = DatabaseInitializer(context.applicationContext)
                        val wordDao = getDatabase().wordDao()
                        initializer.initializeWordDatabase(wordDao)
                    }
                }
            })
            .build()
    }

    fun getWordDao(): WordDao = getDatabase().wordDao()
    fun getUserWordProgressDao(): UserWordProgressDao = getDatabase().userWordProgressDao()
    fun getFavoriteDao(): FavoriteDao = getDatabase().favoriteDao()
    fun getUserWordBankSettingsDao(): UserWordBankSettingsDao = getDatabase().userWordBankSettingsDao()
    fun getLearningRecordDao(): LearningRecordDao = getDatabase().learningRecordDao()
    fun getReviewRecordDao(): ReviewRecordDao = getDatabase().reviewRecordDao()
    fun getTestRecordDao(): TestRecordDao = getDatabase().testRecordDao()
    fun getCheckInRecordDao(): CheckInRecordDao = getDatabase().checkInRecordDao()

    @Synchronized
    fun switchUser(userId: String) {
        if (currentUserId == userId && currentDatabase?.isOpen == true) return
        currentDatabase?.close()
        currentDatabase = null
        currentUserId = null
        if (userId.isNotEmpty()) {
            getDatabase()
        }
    }

    @Synchronized
    fun closeDatabase() {
        currentDatabase?.close()
        currentDatabase = null
        currentUserId = null
    }
}
