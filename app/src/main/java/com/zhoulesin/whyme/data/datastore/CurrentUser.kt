package com.zhoulesin.whyme.data.datastore

import android.content.Context

object CurrentUser {
    private lateinit var dataStore: UserDataStore
    
    var userId: String = ""
        private set

    var account: String = ""
        private set

    val isLoggedIn: Boolean
        get() = userId.isNotEmpty()

    fun initialize(context: Context) {
        dataStore = UserDataStore(context)
        // 从 DataStore 加载用户信息
        userId = dataStore.getUserIdSync()
        account = dataStore.getAccountSync()
    }

    fun set(userId: String, account: String) {
        this.userId = userId
        this.account = account
        // 持久化存储
        kotlinx.coroutines.runBlocking {
            dataStore.saveUser(userId, account)
        }
    }

    fun clear() {
        userId = ""
        account = ""
        // 清除存储
        kotlinx.coroutines.runBlocking {
            dataStore.clearUser()
        }
    }
}
