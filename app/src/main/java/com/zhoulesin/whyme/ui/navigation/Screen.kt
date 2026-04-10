package com.zhoulesin.whyme.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 底部导航路由
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : BottomNavItem(
        route = Screen.Home.route,
        title = "首页",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    data object Learning : BottomNavItem(
        route = Screen.Learning.route,
        title = "学习",
        selectedIcon = Icons.Filled.School,
        unselectedIcon = Icons.Outlined.School
    )

    data object Profile : BottomNavItem(
        route = Screen.Profile.route,
        title = "我的",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    companion object {
        val items = listOf(Home, Learning, Profile)
    }
}

/**
 * 应用屏幕路由
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Learning : Screen("learning")
    data object LearningStudy : Screen("learning/study")
    data object LearningReview : Screen("learning/review")
    data object WordDetail : Screen("word/{wordId}") {
        fun createRoute(wordId: Long) = "word/$wordId"
    }
    data object Quiz : Screen("quiz")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object Speaking : Screen("speaking")
    data object Favorites : Screen("favorites")
    data object Statistics : Screen("statistics")
    data object Login : Screen("login")
    data object Search : Screen("search")
}

/**
 * 学习模式
 */
enum class LearningModeType {
    LEARN,      // 学习新词
    REVIEW,     // 复习
    QUIZ        // 测试
}
