package com.zhoulesin.whyme.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.zhoulesin.whyme.R
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
    userManager: com.zhoulesin.whyme.data.datastore.UserManager
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // 只在主页面显示底部导航
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Learning.route,
        Screen.Profile.route
    )
    
    Scaffold(
        containerColor = MarketingBlack,
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    userManager = userManager
                )
            }
        }
    ) {
        AppNavHost(
            navController = navController,
            paddingValues = it,
            userManager = userManager,
            startDestination = Screen.Home.route
        )
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
    userManager: com.zhoulesin.whyme.data.datastore.UserManager
) {
    NavigationBar(
        containerColor = MarketingBlack,
        contentColor = PrimaryText,
        tonalElevation = 0.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        BottomNavItem.items.forEach {
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (currentRoute == it.route) {
                            it.selectedIcon
                        } else {
                            it.unselectedIcon
                        },
                        contentDescription = it.title
                    )
                },
                label = {
                    Text(
                        text = it.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = currentRoute == it.route,
                onClick = {
                    navController.navigate(it.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
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