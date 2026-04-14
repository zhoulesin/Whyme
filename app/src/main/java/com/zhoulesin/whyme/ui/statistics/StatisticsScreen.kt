package com.zhoulesin.whyme.ui.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhoulesin.whyme.ui.theme.*

/**
 * 学习统计页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MarketingBlack,
        topBar = {
            CompactTopBar(
                title = "学习统计",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = TertiaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .background(MarketingBlack)
            ) {
                // 今日概览
                TodayOverviewCard(
                    wordsLearned = uiState.todayWordsLearned,
                    wordsReviewed = uiState.todayWordsReviewed,
                    accuracy = uiState.todayAccuracy,
                    goalProgress = uiState.goalProgress,
                    dailyGoal = uiState.dailyGoal
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 本周趋势
                WeeklyTrendCard(weeklyRecords = uiState.weeklyRecords)

                Spacer(modifier = Modifier.height(16.dp))

                // 学习总览
                LearningOverviewCard(
                    totalWordsLearned = uiState.totalWordsLearned,
                    totalWordsReviewed = uiState.totalWordsReviewed,
                    totalMinutes = uiState.totalMinutes,
                    currentStreak = uiState.currentStreak,
                    longestStreak = uiState.longestStreak
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 单词掌握情况
                WordMasteryCard(
                    totalWords = uiState.totalWords,
                    masteredWords = uiState.masteredWords,
                    learningWords = uiState.learningWords,
                    unknownWords = uiState.unknownWords,
                    masteryRate = uiState.masteryRate
                )
            }
        }
    }
}

@Composable
private fun TodayOverviewCard(
    wordsLearned: Int,
    wordsReviewed: Int,
    accuracy: Float,
    goalProgress: Float,
    dailyGoal: com.zhoulesin.whyme.domain.model.DailyGoal
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Level3Surface,
        border = BorderStroke(1.dp, BorderStandard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "今日学习",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight(510),
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 目标进度
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "每日目标",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryText
                    )
                    Text(
                        text = "目标: ${dailyGoal.wordsPerDay}学习 + ${dailyGoal.reviewPerDay}复习 + ${dailyGoal.testsPerDay}测试",
                        style = MaterialTheme.typography.bodySmall,
                        color = TertiaryText
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { goalProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (goalProgress >= 1f) Color(0xFF4CAF50) else BrandIndigo,
                    trackColor = BorderStandard,
                )
                Text(
                    text = "${(goalProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = TertiaryText,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 今日数据
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.School,
                    value = "$wordsLearned",
                    label = "新词"
                )
                StatItem(
                    icon = Icons.Default.Refresh,
                    value = "$wordsReviewed",
                    label = "复习"
                )
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    value = "${(accuracy * 100).toInt()}%",
                    label = "正确率"
                )
            }
        }
    }
}

@Composable
private fun WeeklyTrendCard(weeklyRecords: List<DailyRecord>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Level3Surface,
        border = BorderStroke(1.dp, BorderStandard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "本周趋势",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight(510),
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 柱状图
            if (weeklyRecords.isNotEmpty()) {
                val maxValue = weeklyRecords.maxOfOrNull { it.wordsLearned + it.wordsReviewed } ?: 1
                val chartHeight = 120.dp

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyRecords.forEach { record ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val total = record.wordsLearned + record.wordsReviewed
                            val barHeight = if (maxValue > 0) {
                                (total.toFloat() / maxValue * chartHeight.value).dp
                            } else 4.dp

                            // 新词柱
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .height(barHeight * (record.wordsLearned.coerceAtLeast(1).toFloat() / total.coerceAtLeast(1)))
                                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                    .background(BrandIndigo)
                            )

                            // 复习柱
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .height(barHeight * (record.wordsReviewed.coerceAtLeast(1).toFloat() / total.coerceAtLeast(1)))
                                    .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                                    .background(AccentViolet.copy(alpha = 0.6f))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 日期标签
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    weeklyRecords.forEach { record ->
                        Text(
                            text = record.dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = TertiaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 图例
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    LegendItem(color = BrandIndigo, label = "新词")
                    Spacer(modifier = Modifier.width(16.dp))
                    LegendItem(
                        color = AccentViolet.copy(alpha = 0.6f),
                        label = "复习"
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无本周数据",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TertiaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun LearningOverviewCard(
    totalWordsLearned: Int,
    totalWordsReviewed: Int,
    totalMinutes: Long,
    currentStreak: Int,
    longestStreak: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Level3Surface,
        border = BorderStroke(1.dp, BorderStandard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "学习总览",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight(510),
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // 左侧 - 学习数据
                Column(modifier = Modifier.weight(1f)) {
                    OverviewItem(
                        icon = Icons.Default.School,
                        value = "$totalWordsLearned",
                        label = "累计新词"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OverviewItem(
                        icon = Icons.Default.Refresh,
                        value = "$totalWordsReviewed",
                        label = "累计复习"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OverviewItem(
                        icon = Icons.Default.Timer,
                        value = "${totalMinutes}分钟",
                        label = "学习时长"
                    )
                }

                // 右侧 - 打卡数据
                Column(modifier = Modifier.weight(1f)) {
                    OverviewItem(
                        icon = Icons.Default.LocalFireDepartment,
                        value = "$currentStreak 天",
                        label = "当前连续"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OverviewItem(
                        icon = Icons.Default.EmojiEvents,
                        value = "$longestStreak 天",
                        label = "最长连续"
                    )
                }
            }
        }
    }
}

@Composable
private fun WordMasteryCard(
    totalWords: Int,
    masteredWords: Int,
    learningWords: Int,
    unknownWords: Int,
    masteryRate: Float
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Level3Surface,
        border = BorderStroke(1.dp, BorderStandard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "单词掌握情况",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight(510),
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 饼图
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 12.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val center = Offset(size.width / 2, size.height / 2)

                        // 背景圆
                        drawCircle(
                            color = BorderStandard,
                            radius = radius,
                            center = center,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // 掌握部分
                        val sweepAngle = masteryRate * 360f
                        if (sweepAngle > 0) {
                            drawArc(
                                color = BrandIndigo,
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }
                    Text(
                        text = "${(masteryRate * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight(590),
                        color = PrimaryText
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // 右侧 - 分布
                Column {
                    MasteryDistributionItem(
                        color = BrandIndigo,
                        label = "已掌握",
                        count = masteredWords,
                        total = totalWords
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MasteryDistributionItem(
                        color = Color(0xFFFFC107),
                        label = "学习中",
                        count = learningWords,
                        total = totalWords
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MasteryDistributionItem(
                        color = Color(0xFFE0E0E0),
                        label = "未学习",
                        count = unknownWords,
                        total = totalWords
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 总体统计
            Text(
                text = "词库总量: $totalWords 个单词",
                style = MaterialTheme.typography.bodyMedium,
                color = TertiaryText
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentViolet,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight(590),
            color = PrimaryText
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TertiaryText
        )
    }
}

@Composable
private fun OverviewItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentViolet,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight(590),
                color = PrimaryText
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TertiaryText
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TertiaryText
        )
    }
}

@Composable
private fun MasteryDistributionItem(
    color: Color,
    label: String,
    count: Int,
    total: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryText
            )
        }
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight(590),
            color = PrimaryText
        )
    }
}
