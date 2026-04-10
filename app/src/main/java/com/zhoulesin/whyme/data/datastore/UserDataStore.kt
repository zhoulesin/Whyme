package com.zhoulesin.whyme.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.userDataStore by preferencesDataStore(name = "user_preferences")

class UserDataStore(private val context: Context) {
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val ACCOUNT_KEY = stringPreferencesKey("account")

    suspend fun saveUser(userId: String, account: String) {
        context.userDataStore.edit {
            it[USER_ID_KEY] = userId
            it[ACCOUNT_KEY] = account
        }
    }

    suspend fun getUser(): Pair<String, String> {
        val preferences = context.userDataStore.data.first()
        return Pair(
            preferences[USER_ID_KEY] ?: "",
            preferences[ACCOUNT_KEY] ?: ""
        )
    }

    suspend fun clearUser() {
        context.userDataStore.edit {
            it.remove(USER_ID_KEY)
            it.remove(ACCOUNT_KEY)
        }
    }

    // 同步方法，用于初始化时调用
    fun getUserIdSync(): String {
        return runBlocking { 
            context.userDataStore.data.map { it[USER_ID_KEY] ?: "" }.first()
        }
    }

    fun getAccountSync(): String {
        return runBlocking { 
            context.userDataStore.data.map { it[ACCOUNT_KEY] ?: "" }.first()
        }
    }
}