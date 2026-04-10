package com.zhoulesin.whyme.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zhoulesin.whyme.data.datastore.UserManager
import kotlinx.coroutines.launch
import com.zhoulesin.whyme.ui.auth.LoginScreen
import com.zhoulesin.whyme.ui.home.HomeScreen
import com.zhoulesin.whyme.ui.learning.LearningScreen
import com.zhoulesin.whyme.ui.learning.NewWordLearningScreen
import com.zhoulesin.whyme.ui.learning.ReviewScreen
import com.zhoulesin.whyme.ui.learning.WordDetailScreen
import com.zhoulesin.whyme.ui.learning.QuizScreen
import com.zhoulesin.whyme.ui.profile.ProfileScreen
import com.zhoulesin.whyme.ui.favorites.FavoritesScreen
import com.zhoulesin.whyme.ui.search.SearchScreen
import com.zhoulesin.whyme.ui.statistics.StatisticsScreen

/**
 * 应用导航主机
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    userManager: UserManager,
    startDestination: String = Screen.Login.route,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.padding(paddingValues)
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                userManager = userManager,
                onLoginSuccess = {
                    // 登录成功后，UserManager 会更新 isLoggedIn 状态
                    // MainActivity 会根据状态自动切换到 MainScreen
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLearning = {
                    navController.navigate(Screen.LearningStudy.route)
                },
                onNavigateToReview = {
                    navController.navigate(Screen.LearningReview.route)
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }

        // 学习中心入口页面（底部 tab）
        composable(Screen.Learning.route) {
            LearningScreen(
                onNavigateToLearningSession = { mode ->
                    val route = when (mode) {
                        LearningModeType.LEARN -> Screen.LearningStudy.route
                        LearningModeType.REVIEW -> Screen.LearningReview.route
                        LearningModeType.QUIZ -> Screen.Quiz.route
                    }
                    navController.navigate(route)
                },
                onNavigateToWordDetail = { wordId ->
                    navController.navigate(Screen.WordDetail.createRoute(wordId))
                }
            )
        }

        composable(Screen.LearningStudy.route) {
            NewWordLearningScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToWordDetail = { wordId ->
                    navController.navigate(Screen.WordDetail.createRoute(wordId))
                }
            )
        }

        composable(Screen.LearningReview.route) {
            ReviewScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToWordDetail = { wordId ->
                    navController.navigate(Screen.WordDetail.createRoute(wordId))
                }
            )
        }

        composable(
            route = Screen.WordDetail.route,
            arguments = listOf(
                navArgument("wordId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val wordId = backStackEntry.arguments?.getLong("wordId") ?: 0L
            WordDetailScreen(
                wordId = wordId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Quiz.route) {
            QuizScreen(
                onNavigateBack = { navController.popBackStack() },
                onQuizComplete = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorites.route)
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.Statistics.route)
                },
                onLogout = {
                    coroutineScope.launch {
                        userManager.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWordDetail = { wordId ->
                    navController.navigate(Screen.WordDetail.createRoute(wordId))
                }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWordDetail = { wordId ->
                    navController.navigate(Screen.WordDetail.createRoute(wordId))
                }
            )
        }
    }
}
