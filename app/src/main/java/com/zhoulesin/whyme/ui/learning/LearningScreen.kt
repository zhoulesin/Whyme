package com.zhoulesin.whyme.ui.learning

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhoulesin.whyme.domain.model.LearningState
import com.zhoulesin.whyme.domain.model.ReviewResult
import com.zhoulesin.whyme.ui.components.MasteryButtons
import com.zhoulesin.whyme.ui.components.WordCard

@Composable
fun LearningScreen(
    onNavigateToWordDetail: (Long) -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LearningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }

    // 退出确认对话框
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("确认退出？") },
            text = { Text("退出后当前学习进度将不会保存") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetLearning()
                        showExitDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("确认退出")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("继续学习")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 顶部栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "学习中心",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // 非空闲状态显示退出按钮
            if (uiState.learningState !is LearningState.Idle) {
                IconButton(onClick = { showExitDialog = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "退出学习"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (val state = uiState.learningState) {
            is LearningState.Idle -> {
                // 空闲状态 - 显示学习选项
                LearningOptions(
                    newWordsCount = uiState.wordsToLearn.size,
                    reviewCount = uiState.wordsForReview.size,
                    onStartLearning = { viewModel.startLearning() },
                    onStartReview = { viewModel.startReview() },
                    onStartQuiz = onNavigateToQuiz,
                    onAddSampleData = { viewModel.addSampleWords() }
                )
            }

            is LearningState.Learning -> {
                // 学习状态 - 显示单词卡片
                LearningContent(
                    state = state,
                    isFlipped = uiState.isFlipped,
                    onFlip = { viewModel.flipCard() },
                    onMarkWord = { result -> viewModel.markWord(result) },
                    onToggleFavorite = { viewModel.toggleFavorite(state.currentWord.id) }
                )
            }

            is LearningState.Completed -> {
                // 完成状态
                LearningCompletedContent(
                    learned = state.learned,
                    reviewed = state.reviewed,
                    accuracy = state.accuracy,
                    onContinue = { viewModel.resetLearning() },
                    onGoHome = onNavigateBack
                )
            }

            is LearningState.Testing -> {
                // 测试状态（后续实现）
            }

            is LearningState.Error -> {
                // 错误状态
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
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

@Composable
private fun LearningContent(
    state: LearningState.Learning,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onMarkWord: (ReviewResult) -> Unit,
    onToggleFavorite: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 进度指示器
        LinearProgressIndicator(
            progress = { (state.index + 1).toFloat() / state.total },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )

        Text(
            text = "${state.index + 1} / ${state.total}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 单词卡片
        WordCard(
            word = state.currentWord,
            isFlipped = isFlipped,
            onFlip = onFlip,
            onFavoriteClick = onToggleFavorite,
            onSpeakClick = { /* TODO: TTS */ }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 记忆按钮（卡片翻转后才显示）
        if (isFlipped) {
            Text(
                text = "你觉得这个单词记得怎么样？",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            MasteryButtons(
                onAgain = { onMarkWord(ReviewResult.AGAIN) },
                onHard = { onMarkWord(ReviewResult.HARD) },
                onGood = { onMarkWord(ReviewResult.GOOD) },
                onEasy = { onMarkWord(ReviewResult.EASY) }
            )
        } else {
            Text(
                text = "点击卡片查看释义",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LearningCompletedContent(
    learned: Int,
    reviewed: Int,
    accuracy: Float,
    onContinue: () -> Unit,
    onGoHome: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎉",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "太棒了！",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "今日学习完成",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$learned",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text("新学", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$reviewed",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text("复习", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(accuracy * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text("正确率", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("继续学习")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("返回首页")
            }
        }
    }
}
