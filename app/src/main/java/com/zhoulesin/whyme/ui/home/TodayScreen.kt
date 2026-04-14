package com.zhoulesin.whyme.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhoulesin.whyme.ui.components.CircularProgressRing
import com.zhoulesin.whyme.ui.components.StatItem
import com.zhoulesin.whyme.ui.theme.*

/**
 * 「今日」Tab：短会话入口与节奏反馈（design/linear：MarketingBlack、Level3Surface、8dp 圆角卡片）
 */
@Composable
fun TodayScreen(
    onStartTodaySession: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToStudySettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .background(MarketingBlack)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "今日",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight(510),
                    color = PrimaryText,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            IconButton(onClick = onNavigateToStudySettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "学习设置",
                    tint = SecondaryText
                )
            }
        }

        TodayProgressCard(
            learned = uiState.userStats.todayWordsLearned,
            reviewTotal = uiState.dailyGoal.reviewPerDay,
            reviewDone = uiState.userStats.todayWordsReviewed,
            streak = uiState.userStats.currentStreak,
            onStartSession = onStartTodaySession
        )

        Spacer(modifier = Modifier.height(24.dp))

        StatsRow(
            totalWords = uiState.userStats.totalWordsLearned,
            totalMinutes = uiState.userStats.totalLearningMinutes,
            accuracy = uiState.userStats.todayAccuracy
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.wordsForReview.isNotEmpty()) {
            ReviewReminderCard(
                count = uiState.wordsForReview.size,
                onReview = onNavigateToReview
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        DailySentenceCard(sentence = uiState.dailySentence)
    }
}

@Composable
private fun TodayProgressCard(
    learned: Int,
    reviewTotal: Int,
    reviewDone: Int,
    streak: Int,
    onStartSession: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Level3Surface,
        border = BorderStroke(1.dp, BorderStandard)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val totalGoal = learned + reviewTotal
                val progress = if (totalGoal > 0) (learned + reviewDone).toFloat() / totalGoal else 0f

                CircularProgressRing(
                    progress = progress,
                    size = 140.dp,
                    strokeWidth = 14.dp,
                    progressColor = AccentViolet,
                    centerContent = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${((progress * 100).toInt())}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight(510),
                                color = PrimaryText
                            )
                            Text(
                                text = "今日进度",
                                style = MaterialTheme.typography.bodySmall,
                                color = TertiaryText
                            )
                        }
                    }
                )

                Column(
                    modifier = Modifier.padding(start = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatColumn(label = "新学单词", value = "$learned")
                    StatColumn(label = "复习完成", value = "$reviewDone / $reviewTotal")
                    StatColumn(label = "连续接触", value = "$streak 天")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onStartSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandIndigo,
                    contentColor = PrimaryText
                )
            ) {
                Text(
                    text = "开始今日",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight(510)
                )
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight(590),
            color = PrimaryText
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TertiaryText
        )
    }
}

@Composable
private fun StatsRow(
    totalWords: Int,
    totalMinutes: Long,
    accuracy: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(value = "$totalWords", label = "累计单词")
        StatItem(value = "$totalMinutes", label = "学习分钟")
        StatItem(value = "${(accuracy * 100).toInt()}%", label = "正确率")
    }
}

@Composable
private fun ReviewReminderCard(
    count: Int,
    onReview: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Level3Surface,
        border = BorderStroke(1.dp, BorderStandard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "待复习",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight(590),
                    color = PrimaryText
                )
                Text(
                    text = "有 $count 个词待巩固",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText
                )
            }
            Button(
                onClick = onReview,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentViolet,
                    contentColor = PrimaryText
                )
            ) {
                Text(
                    text = "去复习",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight(510)
                )
            }
        }
    }
}

@Composable
private fun DailySentenceCard(sentence: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Level3Surface,
        border = BorderStroke(1.dp, BorderStandard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "每日一句",
                style = MaterialTheme.typography.labelMedium,
                color = TertiaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sentence,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = SecondaryText
            )
        }
    }
}
