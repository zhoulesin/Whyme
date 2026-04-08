package com.zhoulesin.whyme.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Composable
fun HomeScreen(
    onNavigateToLearning: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 顶部问候语
        Text(
            text = "欢迎回来！",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "今天也要继续加油哦～",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 今日学习进度
        TodayProgressCard(
            learned = uiState.userStats.todayWordsLearned,
            reviewTotal = uiState.dailyGoal.reviewPerDay,
            reviewDone = uiState.userStats.todayWordsReviewed,
            streak = uiState.userStats.currentStreak,
            onStartLearning = onNavigateToLearning
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 统计数据行
        StatsRow(
            totalWords = uiState.userStats.totalWordsLearned,
            totalMinutes = uiState.userStats.totalLearningMinutes,
            accuracy = uiState.userStats.todayAccuracy
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 待复习提醒
        if (uiState.wordsForReview.isNotEmpty()) {
            ReviewReminderCard(
                count = uiState.wordsForReview.size,
                onReview = onNavigateToLearning
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 每日一句
        DailySentenceCard(sentence = uiState.dailySentence)
    }
}

@Composable
private fun TodayProgressCard(
    learned: Int,
    reviewTotal: Int,
    reviewDone: Int,
    streak: Int,
    onStartLearning: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
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
                // 左侧进度环
                val totalGoal = learned + reviewTotal
                val progress = if (totalGoal > 0) (learned + reviewDone).toFloat() / totalGoal else 0f

                CircularProgressRing(
                    progress = progress,
                    size = 140.dp,
                    strokeWidth = 14.dp,
                    progressColor = MaterialTheme.colorScheme.primary,
                    centerContent = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${((progress * 100).toInt())}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "今日进度",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                )

                // 右侧信息
                Column(
                    modifier = Modifier.padding(start = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatColumn(label = "新学单词", value = "$learned")
                    StatColumn(label = "复习完成", value = "$reviewDone / $reviewTotal")
                    StatColumn(label = "连续打卡", value = "$streak 天")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 开始学习按钮
            Button(
                onClick = onStartLearning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "开始学习",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
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
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
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
                    text = "📚 待复习",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "你有 $count 个单词需要复习",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            FilledTonalButton(onClick = onReview) {
                Text("复习")
            }
        }
    }
}

@Composable
private fun DailySentenceCard(sentence: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "每日一句",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sentence,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
