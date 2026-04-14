package com.zhoulesin.whyme.ui.home

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.zhoulesin.whyme.data.datastore.UserManager
import com.zhoulesin.whyme.ui.navigation.AppNavHost
import com.zhoulesin.whyme.ui.navigation.BottomNavItem
import com.zhoulesin.whyme.ui.navigation.Screen
import com.zhoulesin.whyme.ui.theme.BrandIndigo
import com.zhoulesin.whyme.ui.theme.MarketingBlack
import com.zhoulesin.whyme.ui.theme.PrimaryText
import com.zhoulesin.whyme.ui.theme.TertiaryText

@Composable
fun MainScreen(
    navController: NavHostController,
    userManager: UserManager
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 方案 A：底部仅在 今日/六级库/我的 三个根页面显示
    val showBottomBar = currentRoute in listOf(
        Screen.Today.route,
        Screen.Cet6Library.route,
        Screen.Profile.route
    )

    Scaffold(
        containerColor = MarketingBlack,
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        AppNavHost(
            navController = navController,
            paddingValues = paddingValues,
            userManager = userManager,
            startDestination = Screen.Today.route
        )
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavHostController
) {
    NavigationBar(
        containerColor = MarketingBlack,
        contentColor = PrimaryText,
        tonalElevation = 0.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        BottomNavItem.items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = if (currentRoute == item.route) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(text = item.title)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandIndigo,
                    selectedTextColor = BrandIndigo,
                    unselectedIconColor = TertiaryText,
                    unselectedTextColor = TertiaryText,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}