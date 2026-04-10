package com.zhoulesin.whyme.data.datastore

object CurrentUser {
    var userId: String = ""
        private set

    var account: String = ""
        private set

    val isLoggedIn: Boolean
        get() = userId.isNotEmpty()

    fun set(userId: String, account: String) {
        this.userId = userId
        this.account = account
    }

    fun clear() {
        userId = ""
        account = ""
    }
}
