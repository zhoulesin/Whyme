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
import com.zhoulesin.whyme.ui.learning.WordDetailScreen
import com.zhoulesin.whyme.ui.learning.QuizScreen
import com.zhoulesin.whyme.ui.profile.ProfileScreen

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
                    navController.navigate(Screen.Learning.route)
                }
            )
        }

        composable(Screen.Learning.route) {
            LearningScreen(
                onNavigateToWordDetail = { wordId ->
                    navController.navigate(Screen.WordDetail.createRoute(wordId))
                },
                onNavigateToQuiz = {
                    navController.navigate(Screen.Quiz.route)
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
    }
}
