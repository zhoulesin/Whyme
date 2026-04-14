package com.zhoulesin.whyme.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zhoulesin.whyme.data.local.UserDatabaseManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.security.MessageDigest

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_auth_preferences")

/**
 * 用户管理类（单例）
 * 用于管理用户的登录状态、账号信息和成员ID
 * 提供登录、登出和恢复登录状态的方法
 * 与UserDatabaseManager配合使用，管理用户的本地数据库
 */
class UserManager private constructor(private val context: Context) {

    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ACCOUNT = stringPreferencesKey("user_account")
        val MEMBER_UID = stringPreferencesKey("member_uid")

        @Volatile
        private var instance: UserManager? = null

        fun getInstance(context: Context): UserManager {
            return instance ?: synchronized(this) {
                instance ?: UserManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    private var userDatabaseManager: UserDatabaseManager? = null
    
    // 当前用户状态
    var userId: String = ""
        private set
    
    var account: String = ""
        private set
    
    val isLoggedIn: Boolean
        get() = userId.isNotEmpty()

    fun setUserDatabaseManager(manager: UserDatabaseManager) {
        userDatabaseManager = manager
    }
    
    /**
     * 初始化用户管理器
     * 从DataStore加载用户信息
     */
    fun initialize() {
        runBlocking {
            val prefs = context.userDataStore.data.first()
            val loggedIn = prefs[IS_LOGGED_IN] ?: false
            if (loggedIn) {
                userId = prefs[MEMBER_UID] ?: ""
                account = prefs[USER_ACCOUNT] ?: ""
                userDatabaseManager?.switchUser(userId)
            }
        }
    }
    
    /**
     * 设置当前用户
     */
    private fun setUser(userId: String, account: String) {
        this.userId = userId
        this.account = account
    }
    
    /**
     * 清除当前用户
     */
    private fun clearUser() {
        userId = ""
        account = ""
    }

    val isLoggedInFlow: Flow<Boolean> = context.userDataStore.data
        .map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }

    val userAccountFlow: Flow<String?> = context.userDataStore.data
        .map { preferences ->
            preferences[USER_ACCOUNT]
        }

    val memberUidFlow: Flow<String?> = context.userDataStore.data
        .map { preferences ->
            preferences[MEMBER_UID]
        }

    suspend fun login(account: String, password: String): Boolean {
        val memberUid = generateMemberUid(account)

        context.userDataStore.edit {
            it[IS_LOGGED_IN] = true
            it[USER_ACCOUNT] = account
            it[MEMBER_UID] = memberUid
        }

        setUser(memberUid, account)
        userDatabaseManager?.switchUser(memberUid)

        return true
    }

    suspend fun logout() {
        context.userDataStore.edit {
            it[IS_LOGGED_IN] = false
            it[USER_ACCOUNT] = ""
            it[MEMBER_UID] = ""
        }

        clearUser()
        userDatabaseManager?.closeDatabase()
    }

    suspend fun restoreLoginState() {
        val prefs = context.userDataStore.data.first()
        val loggedIn = prefs[IS_LOGGED_IN] ?: false
        if (loggedIn) {
            val uid = prefs[MEMBER_UID] ?: ""
            val account = prefs[USER_ACCOUNT] ?: ""
            setUser(uid, account)
            userDatabaseManager?.switchUser(uid)
        }
    }

    private fun generateMemberUid(account: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(account.toByteArray())
        val hexString = hash.joinToString("") { "%02x".format(it) }
        return hexString.substring(0, 16)
    }
}
