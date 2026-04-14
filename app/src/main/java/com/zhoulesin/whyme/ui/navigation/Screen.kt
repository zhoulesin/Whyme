package com.zhoulesin.whyme.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 底部导航（方案 A：今日 / 六级库 / 我的）
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Today : BottomNavItem(
        route = Screen.Today.route,
        title = "今日",
        selectedIcon = Icons.Outlined.CalendarToday,
        unselectedIcon = Icons.Outlined.CalendarToday
    )

    data object Cet6Library : BottomNavItem(
        route = Screen.Cet6Library.route,
        title = "六级库",
        selectedIcon = Icons.AutoMirrored.Outlined.MenuBook,
        unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook
    )

    data object Profile : BottomNavItem(
        route = Screen.Profile.route,
        title = "我的",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    companion object {
        val items = listOf(Today, Cet6Library, Profile)
    }
}

/**
 * 应用屏幕路由
 */
sealed class Screen(val route: String) {
    data object Today : Screen("today")
    data object Cet6Library : Screen("cet6")
    data object LearningStudy : Screen("learning/study")
    data object LearningReview : Screen("learning/review")
    data object WordDetail : Screen("word/{wordId}") {
        fun createRoute(wordId: Long) = "word/$wordId"
    }

    data object Quiz : Screen("quiz")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object Favorites : Screen("favorites")
    data object Statistics : Screen("statistics")
    data object Login : Screen("login")
}

/**
 * 学习模式（从六级库进入复习 / 测验等二级流）
 */
enum class LearningModeType {
    LEARN,
    REVIEW,
    QUIZ
}
