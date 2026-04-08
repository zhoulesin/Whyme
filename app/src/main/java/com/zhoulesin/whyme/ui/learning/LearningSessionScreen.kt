package com.zhoulesin.whyme.ui.learning

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
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

/**
 * 学习会话页面 - 实际的学习/测试界面（二级页面）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningSessionScreen(
    mode: String = "LEARN",
    onNavigateBack: () -> Unit,
    onNavigateToWordDetail: (Long) -> Unit,
    viewModel: LearningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }
    var sessionStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 初始化学习会话
        viewModel.initSession()
    }

    // 进入对应页面后启动固定流程
    LaunchedEffect(uiState.wordsToLearn, uiState.wordsForReview, mode, sessionStarted, uiState.isDataLoaded) {
        if (sessionStarted) return@LaunchedEffect
        if (!uiState.isDataLoaded) return@LaunchedEffect

        val hasWordsToReview = uiState.wordsForReview.isNotEmpty()
        val hasWordsToLearn = uiState.wordsToLearn.isNotEmpty()
        val isIdle = uiState.learningState is LearningState.Idle

        if (!isIdle) return@LaunchedEffect

        when (mode.uppercase()) {
            "LEARN" -> {
                if (hasWordsToLearn) {
                    viewModel.startLearning()
                    sessionStarted = true
                }
            }
            "REVIEW" -> {
                if (hasWordsToReview) {
                    viewModel.startReview()
                    sessionStarted = true
                }
            }
            "QUIZ" -> {
                if (uiState.allLearnedWords.isNotEmpty()) {
                    viewModel.startQuiz()
                    sessionStarted = true
                }
            }
        }
    }

    // 退出确认对话框
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("确认退出？") },
            text = { Text("退出后当前学习进度将不会保存") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.exitSession()
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

    // 根据模式获取标题
    val title = when (mode.uppercase()) {
        "LEARN" -> "学习新词"
        "REVIEW" -> "复习旧词"
        "QUIZ" -> "单词测试"
        else -> "学习中"
    }

    val idleTitle = when (mode.uppercase()) {
        "LEARN" -> "当前级别暂无可学新词"
        "REVIEW" -> "当前没有待复习单词"
        "QUIZ" -> "当前没有可测试的已学单词"
        else -> "当前没有学习内容"
    }

    val idleDescription = when (mode.uppercase()) {
        "LEARN" -> "可以切换词库级别，或前往学习中心查看其他学习入口。"
        "REVIEW" -> "继续保持，系统会在需要时自动安排复习。"
        "QUIZ" -> "请先完成一些学习或复习，再回来测试。"
        else -> "请返回上一页重新选择学习模式。"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "退出学习"
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
                                text = "正在加载单词...",
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
                                text = idleTitle,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = idleDescription,
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

                is LearningState.Learning -> {
                    // 学习状态 - 显示单词卡片
                    LearningContent(
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
                    LearningCompletedContent(
                        learned = state.learned,
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

                is LearningState.Testing -> {
                    // 测试状态
                    QuizContent(
                        state = state,
                        selectedAnswer = uiState.selectedAnswer,
                        isAnswerRevealed = uiState.isAnswerRevealed,
                        onSelectAnswer = { viewModel.selectAnswer(it) },
                        onNextQuestion = { viewModel.nextQuestion() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is LearningState.QuizResult -> {
                    // 测试结果状态
                    QuizResultContent(
                        correctCount = state.correctCount,
                        totalCount = state.totalCount,
                        accuracy = state.accuracy,
                        onContinue = { viewModel.exitSession(); onNavigateBack() },
                        onGoHome = { viewModel.exitSession(); onNavigateBack() }
                    )
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
}

@Composable
private fun LearningContent(
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 单词卡片
        com.zhoulesin.whyme.ui.components.WordCard(
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
private fun QuizContent(
    state: LearningState.Testing,
    selectedAnswer: String?,
    isAnswerRevealed: Boolean,
    onSelectAnswer: (String) -> Unit,
    onNextQuestion: () -> Unit,
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

        // 题目
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (state.questionType) {
                        com.zhoulesin.whyme.domain.model.QuestionType.WORD_TO_CHINESE -> "请选择中文释义"
                        com.zhoulesin.whyme.domain.model.QuestionType.CHINESE_TO_WORD -> "请选择英文单词"
                        com.zhoulesin.whyme.domain.model.QuestionType.SPELLING -> "请拼写单词"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = when (state.questionType) {
                        com.zhoulesin.whyme.domain.model.QuestionType.WORD_TO_CHINESE,
                        com.zhoulesin.whyme.domain.model.QuestionType.SPELLING -> state.currentWord.word
                        com.zhoulesin.whyme.domain.model.QuestionType.CHINESE_TO_WORD -> state.currentWord.translation
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 选项
        if (state.options.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.options.forEach { option ->
                    val isSelected = selectedAnswer == option.text
                    val isCorrect = option.isCorrect

                    val containerColor = when {
                        isAnswerRevealed && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                        isAnswerRevealed && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                        isSelected -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    }

                    Card(
                        onClick = { if (!isAnswerRevealed) onSelectAnswer(option.text) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = containerColor)
                    ) {
                        Text(
                            text = option.text,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 下一题按钮
        if (isAnswerRevealed) {
            Button(
                onClick = onNextQuestion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.index < state.total - 1) "下一题" else "查看结果")
            }
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
                text = "学习完成！",
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
                        text = "$learned",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "新词学习",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
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
                        text = "掌握率",
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

@Composable
private fun QuizResultContent(
    correctCount: Int,
    totalCount: Int,
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
                text = "测试完成！",
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
                        text = "$correctCount / $totalCount",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "正确数",
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
