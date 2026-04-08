package com.zhoulesin.whyme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zhoulesin.whyme.ui.navigation.AppNavHost
import com.zhoulesin.whyme.ui.navigation.BottomNavItem
import com.zhoulesin.whyme.ui.navigation.Screen
import com.zhoulesin.whyme.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WhyMeEnglishTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 判断是否显示底部导航（二级页面不显示）
    val showBottomBar = currentDestination?.route in listOf(
        Screen.Home.route,
        Screen.Learning.route,
        Screen.Profile.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MarketingBlack,
        contentColor = PrimaryText,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = PanelDark,
                    contentColor = SecondaryText,
                    tonalElevation = 0.dp
                ) {
                    BottomNavItem.items.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    // 避免在同一个目的地时创建多个实例
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    tint = if (selected) AccentViolet else SecondaryText
                                )
                            },
                            label = {
                                Text(
                                    item.title,
                                    color = if (selected) AccentViolet else SecondaryText
                                )
                            }
                        )
                    }
                }
            }
        }
    ) {
        AppNavHost(
            navController = navController,
            paddingValues = it
        )
    }
}
