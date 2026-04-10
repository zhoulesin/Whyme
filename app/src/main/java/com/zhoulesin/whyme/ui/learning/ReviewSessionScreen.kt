package com.zhoulesin.whyme.ui.learning

import androidx.compose.foundation.layout.*
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
import com.zhoulesin.whyme.domain.model.ReviewResult
import com.zhoulesin.whyme.ui.components.MasteryButtons
import com.zhoulesin.whyme.ui.components.WordCard
import com.zhoulesin.whyme.ui.theme.AccentViolet
import com.zhoulesin.whyme.ui.theme.CompactTopBar
import com.zhoulesin.whyme.ui.theme.Level3Surface
import com.zhoulesin.whyme.ui.theme.MarketingBlack
import com.zhoulesin.whyme.ui.theme.PrimaryText
import com.zhoulesin.whyme.ui.theme.TertiaryText

/**
 * 复习会话页面 - 专门处理复习功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewSessionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWordDetail: (Long) -> Unit
) {
    val viewModel: ReviewViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }
    var sessionStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 初始化复习会话
        viewModel.initSession()
    }

    // 进入页面后启动复习流程
    LaunchedEffect(uiState.wordsForReview, sessionStarted, uiState.isDataLoaded) {
        if (sessionStarted) return@LaunchedEffect
        if (!uiState.isDataLoaded) return@LaunchedEffect

        val hasWordsToReview = uiState.wordsForReview.isNotEmpty()
        val isIdle = uiState.reviewState is ReviewState.Idle

        if (!isIdle) return@LaunchedEffect

        if (hasWordsToReview) {
            viewModel.startReview()
            sessionStarted = true
        }
    }

    // 退出确认对话框
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("确认退出？", color = PrimaryText) },
            text = { Text("退出后当前复习进度将不会保存", color = TertiaryText) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.exitSession()
                        showExitDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AccentViolet
                    )
                ) {
                    Text("确认退出")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = TertiaryText
                    )
                ) {
                    Text("继续复习")
                }
            },
            containerColor = Level3Surface
        )
    }

    Scaffold(
        containerColor = MarketingBlack,
        topBar = {
            CompactTopBar(
                title = "复习旧词",
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "退出复习",
                            tint = TertiaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (val state = uiState.reviewState) {
                is ReviewState.Idle -> {
                    // 空闲状态 - 等待数据加载或显示空状态
                    if (!uiState.isDataLoaded) {
                        // 正在加载
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "正在加载复习单词...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // 数据加载完成但没有单词
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🎉",
                                style = MaterialTheme.typography.displayLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "太棒了！",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "当前没有待复习单词",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "继续保持，系统会在需要时自动安排复习。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            OutlinedButton(onClick = onNavigateBack) {
                                Text("返回")
                            }
                        }
                    }
                }

                is ReviewState.Reviewing -> {
                    // 复习状态 - 显示单词卡片
                    ReviewContent(
                        state = state,
                        isFlipped = uiState.isFlipped,
                        onFlip = { viewModel.flipCard() },
                        onMarkWord = { result -> viewModel.markWord(result) },
                        onToggleFavorite = { wordId -> viewModel.toggleFavorite(wordId) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is ReviewState.Completed -> {
                    // 复习完成状态
                    ReviewCompletedContent(
                        reviewed = state.reviewed,
                        correct = state.correct,
                        accuracy = state.accuracy,
                        onContinue = {
                            viewModel.exitSession()
                            onNavigateBack()
                        },
                        onGoHome = {
                            viewModel.exitSession()
                            onNavigateBack()
                        }
                    )
                }

                is ReviewState.Error -> {
                    // 错误状态
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = onNavigateBack) {
                            Text("返回")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewContent(
    state: ReviewState.Reviewing,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onMarkWord: (ReviewResult) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
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
private fun ReviewCompletedContent(
    reviewed: Int,
    correct: Int,
    accuracy: Float,
    onContinue: () -> Unit,
    onGoHome: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
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
                text = "复习完成！",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$reviewed",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "复习词汇",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(accuracy * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "正确率",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("继续学习")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("返回首页")
            }
        }
    }
}