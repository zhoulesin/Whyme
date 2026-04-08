package com.zhoulesin.whyme.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zhoulesin.whyme.ui.home.HomeScreen
import com.zhoulesin.whyme.ui.learning.LearningScreen
import com.zhoulesin.whyme.ui.learning.ReviewSessionScreen
import com.zhoulesin.whyme.ui.learning.StudySessionScreen
import com.zhoulesin.whyme.ui.learning.WordDetailScreen
import com.zhoulesin.whyme.ui.learning.QuizScreen
import com.zhoulesin.whyme.ui.profile.ProfileScreen
import com.zhoulesin.whyme.ui.favorites.FavoritesScreen
import com.zhoulesin.whyme.ui.statistics.StatisticsScreen

/**
 * 应用导航主机
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier.padding(paddingValues)
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLearning = {
                    navController.navigate(Screen.LearningStudy.route)
                },
                onNavigateToReview = {
                    navController.navigate(Screen.LearningReview.route)
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
            StudySessionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToWordDetail = { wordId ->
                    navController.navigate(Screen.WordDetail.createRoute(wordId))
                }
            )
        }

        composable(Screen.LearningReview.route) {
            ReviewSessionScreen(
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
    }
}
