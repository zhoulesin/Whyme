package com.zhoulesin.whyme

import android.app.Application
import com.zhoulesin.whyme.data.datastore.CurrentUser
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WhyMeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化 CurrentUser，从 DataStore 加载用户信息
        CurrentUser.initialize(this)
    }
}
