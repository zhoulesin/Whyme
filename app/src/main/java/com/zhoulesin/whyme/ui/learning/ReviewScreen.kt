package com.zhoulesin.whyme.ui.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhoulesin.whyme.domain.model.LearningState
import com.zhoulesin.whyme.domain.model.ReviewResult
import com.zhoulesin.whyme.ui.components.MasteryButtons
import com.zhoulesin.whyme.ui.components.WordCard
import com.zhoulesin.whyme.ui.theme.*

/**
 * 复习页面 - 专门用于复习旧词
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWordDetail: (Long) -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }
    var sessionStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 初始化复习会话
        viewModel.initSession()
    }

    // 进入页面后启动复习流程
    LaunchedEffect(uiState.wordsForReview, uiState.isDataLoaded, sessionStarted) {
        if (sessionStarted) return@LaunchedEffect
        if (!uiState.isDataLoaded) return@LaunchedEffect

        val hasWordsToReview = uiState.wordsForReview.isNotEmpty()
        val isIdle = uiState.learningState is LearningState.Idle

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
            TopAppBar(
                title = { Text("复习旧词", color = PrimaryText) },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "退出复习",
                            tint = TertiaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Level3Surface
                )
            )
        }
    ) {
        paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .background(MarketingBlack)
        ) {
            when (val state = uiState.learningState) {
                is LearningState.Idle -> {
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
                                color = TertiaryText
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
                                fontWeight = MaterialTheme.typography.headlineSmall.fontWeight,
                                color = PrimaryText
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "当前没有待复习单词",
                                style = MaterialTheme.typography.bodyLarge,
                                color = PrimaryText
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "继续保持，系统会在需要时自动安排复习。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TertiaryText
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            OutlinedButton(onClick = onNavigateBack) {
                                Text("返回")
                            }
                        }
                    }
                }

                is LearningState.Learning -> {
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

                is LearningState.Completed -> {
                    // 完成状态
                    ReviewCompletedContent(
                        reviewed = state.reviewed,
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

                is LearningState.Error -> {
                    // 错误状态
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                else -> {
                    // 其他状态（测试相关）不处理
                    Text("不支持的状态")
                }
            }
        }
    }
}

@Composable
private fun ReviewContent(
    state: LearningState.Learning,
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
            color = TertiaryText
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
            MasteryButtons(
                onAgain = { onMarkWord(ReviewResult.AGAIN) },
                onHard = { onMarkWord(ReviewResult.HARD) },
                onGood = { onMarkWord(ReviewResult.GOOD) },
                onEasy = { onMarkWord(ReviewResult.EASY) },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "点击卡片查看释义",
                style = MaterialTheme.typography.bodySmall,
                color = TertiaryText
            )
        }
    }
}

@Composable
private fun ReviewCompletedContent(
    reviewed: Int,
    accuracy: Float,
    onContinue: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "复习完成！",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = MaterialTheme.typography.headlineSmall.fontWeight,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "今日复习：$reviewed 词",
            style = MaterialTheme.typography.bodyLarge,
            color = PrimaryText
        )
        Text(
            text = "准确率：${String.format("%.1f", accuracy * 100)}%",
            style = MaterialTheme.typography.bodyLarge,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(onClick = onContinue) {
                    Text("继续复习")
                }
                Button(onClick = onGoHome) {
                    Text("返回主页")
                }
            }
    }
}
