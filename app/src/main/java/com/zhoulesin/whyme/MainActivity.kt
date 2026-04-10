package com.zhoulesin.whyme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.zhoulesin.whyme.data.datastore.UserManager
import com.zhoulesin.whyme.data.local.UserDatabaseManager
import com.zhoulesin.whyme.ui.home.MainScreen
import com.zhoulesin.whyme.ui.navigation.AppNavHost
import com.zhoulesin.whyme.ui.navigation.Screen
import com.zhoulesin.whyme.ui.theme.WhyMeEnglishTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userDatabaseManager: UserDatabaseManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userManager = UserManager.getInstance(this)
        userManager.setUserDatabaseManager(userDatabaseManager)

        scope.launch {
            userManager.restoreLoginState()
            val isLoggedIn = userManager.isLoggedIn.first()

            enableEdgeToEdge()
            setContent {
                WhyMeEnglishTheme {
                    val navController = rememberNavController()
                    if (isLoggedIn) {
                        MainScreen(
                            navController = navController,
                            userManager = userManager
                        )
                    } else {
                        AppNavHost(
                            navController = navController,
                            paddingValues = androidx.compose.foundation.layout.PaddingValues(),
                            userManager = userManager,
                            startDestination = Screen.Login.route
                        )
                    }
                }
            }
        }
    }
}
