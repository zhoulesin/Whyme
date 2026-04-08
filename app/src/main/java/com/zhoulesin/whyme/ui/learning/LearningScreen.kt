package com.zhoulesin.whyme.ui.learning

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.zhoulesin.whyme.ui.theme.*

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
            .background(MarketingBlack)
    ) {
        // 顶部栏
        Text(
            text = "学习中心",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight(510),
            color = PrimaryText,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 学习选项
        LearningOptions(
            newWordsCount = uiState.wordsToLearn.size,
            reviewCount = uiState.wordsForReview.size,
            quizCount = uiState.allLearnedWords.size,
            onStartLearning = {
                onNavigateToLearningSession(LearningModeType.LEARN)
            },
            onStartReview = {
                onNavigateToLearningSession(LearningModeType.REVIEW)
            },
            onStartQuiz = {
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
    quizCount: Int,
    onStartLearning: () -> Unit,
    onStartReview: () -> Unit,
    onStartQuiz: () -> Unit,
    onAddSampleData: () -> Unit
) {
    // 添加示例数据按钮（仅用于演示）
    if (newWordsCount == 0 && reviewCount == 0) {
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
                    text = "还没有学习数据",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onAddSampleData,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentViolet,
                        contentColor = PrimaryText
                    )
                ) {
                    Text("添加示例单词")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    // 新词学习卡片
    LearningOptionCard(
        title = "📖 学习新词",
        subtitle = "当前级别生成 $newWordsCount 个学习单词",
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
        subtitle = "从已学习单词中测试 $quizCount 个词",
        buttonText = "开始测试",
        enabled = quizCount > 0,
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Level3Surface,
        border = BorderStroke(1.dp, BorderStandard)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight(510),
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TertiaryText
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandIndigo,
                    contentColor = PrimaryText
                )
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
