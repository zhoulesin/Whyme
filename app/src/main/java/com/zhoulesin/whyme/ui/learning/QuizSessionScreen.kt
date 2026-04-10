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
import com.zhoulesin.whyme.domain.model.QuestionType
import com.zhoulesin.whyme.ui.theme.AccentViolet
import com.zhoulesin.whyme.ui.theme.CompactTopBar
import com.zhoulesin.whyme.ui.theme.Level3Surface
import com.zhoulesin.whyme.ui.theme.MarketingBlack
import com.zhoulesin.whyme.ui.theme.PrimaryText
import com.zhoulesin.whyme.ui.theme.TertiaryText

/**
 * 测试会话页面 - 专门处理测试功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizSessionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWordDetail: (Long) -> Unit
) {
    val viewModel: QuizViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }
    var sessionStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 初始化测试会话
        viewModel.initSession()
    }

    // 进入页面后启动测试流程
    LaunchedEffect(uiState.allLearnedWords, sessionStarted, uiState.isDataLoaded) {
        if (sessionStarted) return@LaunchedEffect
        if (!uiState.isDataLoaded) return@LaunchedEffect

        val hasWordsToTest = uiState.allLearnedWords.isNotEmpty()
        val isIdle = uiState.quizState is QuizState.Idle

        if (!isIdle) return@LaunchedEffect

        if (hasWordsToTest) {
            viewModel.startQuiz()
            sessionStarted = true
        }
    }

    // 退出确认对话框
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("确认退出？", color = PrimaryText) },
            text = { Text("退出后当前测试进度将不会保存", color = TertiaryText) },
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
                    Text("继续测试")
                }
            },
            containerColor = Level3Surface
        )
    }

    Scaffold(
        containerColor = MarketingBlack,
        topBar = {
            CompactTopBar(
                title = "单词测试",
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "退出测试",
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
            when (val state = uiState.quizState) {
                is QuizState.Idle -> {
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
                                text = "正在加载测试单词...",
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
                                text = "当前没有可测试的已学单词",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "请先完成一些学习或复习，再回来测试。",
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

                is QuizState.Testing -> {
                    // 测试状态 - 显示测试题目
                    QuizContent(
                        state = state,
                        selectedAnswer = uiState.selectedAnswer,
                        isAnswerRevealed = uiState.isAnswerRevealed,
                        onSelectAnswer = { viewModel.selectAnswer(it) },
                        onNextQuestion = { viewModel.nextQuestion() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is QuizState.Result -> {
                    // 测试结果状态
                    QuizResultContent(
                        correctCount = state.correctCount,
                        totalCount = state.totalCount,
                        accuracy = state.accuracy,
                        onContinue = { viewModel.exitSession(); onNavigateBack() },
                        onGoHome = { viewModel.exitSession(); onNavigateBack() }
                    )
                }

                is QuizState.Error -> {
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
private fun QuizContent(
    state: QuizState.Testing,
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
                        QuestionType.WORD_TO_CHINESE -> "请选择中文释义"
                        QuestionType.CHINESE_TO_WORD -> "请选择英文单词"
                        QuestionType.SPELLING -> "请拼写单词"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = when (state.questionType) {
                        QuestionType.WORD_TO_CHINESE,
                        QuestionType.SPELLING -> state.currentWord.word
                        QuestionType.CHINESE_TO_WORD -> state.currentWord.translation
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
private fun QuizResultContent(
    correctCount: Int,
    totalCount: Int,
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