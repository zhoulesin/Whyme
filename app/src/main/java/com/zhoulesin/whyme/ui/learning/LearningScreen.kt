package com.zhoulesin.whyme.ui.learning

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhoulesin.whyme.ui.navigation.LearningModeType

/**
 * 学习中心入口页面（底部 tab 页面）
 */
@Composable
fun LearningScreen(
    onNavigateToLearningSession: (LearningModeType) -> Unit,
    onNavigateToWordDetail: (Long) -> Unit,
    viewModel: LearningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 顶部栏
        Text(
            text = "学习中心",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 学习选项
        LearningOptions(
            newWordsCount = uiState.wordsToLearn.size,
            reviewCount = uiState.wordsForReview.size,
            onStartLearning = {
                viewModel.startLearning()
                onNavigateToLearningSession(LearningModeType.LEARN)
            },
            onStartReview = {
                viewModel.startReview()
                onNavigateToLearningSession(LearningModeType.REVIEW)
            },
            onStartQuiz = {
                viewModel.startQuiz()
                onNavigateToLearningSession(LearningModeType.QUIZ)
            },
            onAddSampleData = { viewModel.addSampleWords() }
        )
    }
}

@Composable
private fun LearningOptions(
    newWordsCount: Int,
    reviewCount: Int,
    onStartLearning: () -> Unit,
    onStartReview: () -> Unit,
    onStartQuiz: () -> Unit,
    onAddSampleData: () -> Unit
) {
    // 添加示例数据按钮（仅用于演示）
    if (newWordsCount == 0 && reviewCount == 0) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "还没有学习数据",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onAddSampleData) {
                    Text("添加示例单词")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    // 新词学习卡片
    LearningOptionCard(
        title = "📖 学习新词",
        subtitle = "今日可学习 $newWordsCount 个新词",
        buttonText = "开始学习",
        enabled = newWordsCount > 0,
        onClick = onStartLearning
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 复习卡片
    LearningOptionCard(
        title = "🔄 复习旧词",
        subtitle = "待复习 $reviewCount 个单词",
        buttonText = "开始复习",
        enabled = reviewCount > 0,
        onClick = onStartReview
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 测试卡片
    LearningOptionCard(
        title = "✍️ 单词测试",
        subtitle = "检验学习成果",
        buttonText = "开始测试",
        enabled = newWordsCount + reviewCount > 0,
        onClick = onStartQuiz
    )
}

@Composable
private fun LearningOptionCard(
    title: String,
    subtitle: String,
    buttonText: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
