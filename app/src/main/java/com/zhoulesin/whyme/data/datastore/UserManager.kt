package com.zhoulesin.whyme.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_auth_preferences")

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

    val isLoggedIn: Flow<Boolean> = context.userDataStore.data
        .map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }

    val userAccount: Flow<String?> = context.userDataStore.data
        .map { preferences ->
            preferences[USER_ACCOUNT]
        }

    val memberUid: Flow<String?> = context.userDataStore.data
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

        CurrentUser.set(memberUid, account)

        return true
    }

    suspend fun logout() {
        context.userDataStore.edit {
            it[IS_LOGGED_IN] = false
            it[USER_ACCOUNT] = ""
            it[MEMBER_UID] = ""
        }

        CurrentUser.clear()
    }

    suspend fun restoreLoginState() {
        val prefs = context.userDataStore.data.first()
        val loggedIn = prefs[IS_LOGGED_IN] ?: false
        if (loggedIn) {
            val uid = prefs[MEMBER_UID] ?: ""
            val account = prefs[USER_ACCOUNT] ?: ""
            CurrentUser.set(uid, account)
        }
    }

    private fun generateMemberUid(account: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(account.toByteArray())
        val hexString = hash.joinToString("") { "%02x".format(it) }
        return hexString.substring(0, 16)
    }
}
