package com.zhoulesin.whyme

import android.app.Application
import com.zhoulesin.whyme.data.datastore.UserManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WhyMeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化 UserManager，从 DataStore 加载用户信息
        UserManager.getInstance(this).initialize()
    }
}
