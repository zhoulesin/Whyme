package com.zhoulesin.whyme.ui.learning

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhoulesin.whyme.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onNavigateBack: () -> Unit,
    onQuizComplete: () -> Unit,
    viewModel: LearningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("单词测试") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetLearning()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState.learningState) {
                is LearningState.Idle -> {
                    // 开始测试
                    QuizStartContent(
                        wordCount = uiState.allLearnedWords.size,
                        onStartQuiz = { viewModel.startQuiz() }
                    )
                }

                is LearningState.Testing -> {
                    // 测试进行中
                    QuizQuestionContent(
                        state = state,
                        selectedAnswer = uiState.selectedAnswer,
                        isAnswerRevealed = uiState.isAnswerRevealed,
                        onSelectAnswer = { viewModel.selectAnswer(it) },
                        onNext = { viewModel.nextQuestion() }
                    )
                }

                is LearningState.QuizResult -> {
                    // 测试完成
                    QuizResultContent(
                        correctCount = state.correctCount,
                        totalCount = state.totalCount,
                        accuracy = state.accuracy,
                        onRetry = { viewModel.startQuiz() },
                        onFinish = {
                            viewModel.resetLearning()
                            onQuizComplete()
                        }
                    )
                }

                else -> {
                    // 其他状态，显示开始测试
                    QuizStartContent(
                        wordCount = uiState.allLearnedWords.size,
                        onStartQuiz = { viewModel.startQuiz() }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizStartContent(
    wordCount: Int,
    onStartQuiz: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "✍️",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "单词测试",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "共 $wordCount 个可测试单词",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 测试说明
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "测试规则",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• 从全部已学习单词中出题\n• 共 10 道选择题\n• 正确率影响后续复习安排",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStartQuiz,
            enabled = wordCount > 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "开始测试",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (wordCount == 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "请先学习一些单词再来测试吧",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun QuizQuestionContent(
    state: LearningState.Testing,
    selectedAnswer: String?,
    isAnswerRevealed: Boolean,
    onSelectAnswer: (String) -> Unit,
    onNext: () -> Unit
) {
    val correctAnswer = when (state.questionType) {
        QuestionType.WORD_TO_CHINESE -> state.currentWord.translation
        QuestionType.CHINESE_TO_WORD -> state.currentWord.word
        QuestionType.SPELLING -> state.currentWord.word
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 进度
        LinearProgressIndicator(
            progress = { (state.index + 1).toFloat() / state.total },
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = "${state.index + 1} / ${state.total}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 问题卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (state.questionType) {
                        QuestionType.WORD_TO_CHINESE -> "这个单词是什么意思？"
                        QuestionType.CHINESE_TO_WORD -> "这个中文对应的单词是？"
                        QuestionType.SPELLING -> "请拼写这个单词"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (state.questionType) {
                        QuestionType.WORD_TO_CHINESE -> state.currentWord.word
                        QuestionType.CHINESE_TO_WORD -> state.currentWord.translation
                        QuestionType.SPELLING -> state.currentWord.translation
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                if (state.questionType == QuestionType.WORD_TO_CHINESE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.currentWord.phonetic,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 答案选项
        if (state.questionType != QuestionType.SPELLING) {
            state.options.forEach { option ->
                val isSelected = selectedAnswer == option.text
                val backgroundColor = when {
                    isAnswerRevealed && option.isCorrect -> MaterialTheme.colorScheme.primaryContainer
                    isAnswerRevealed && isSelected && !option.isCorrect -> MaterialTheme.colorScheme.errorContainer
                    isSelected -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }

                OutlinedCard(
                    onClick = { if (!isAnswerRevealed) onSelectAnswer(option.text) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = backgroundColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.text,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (isAnswerRevealed) {
                            if (option.isCorrect) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "正确",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "错误",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 下一题按钮
        if (isAnswerRevealed) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (state.index + 1 >= state.total) "查看结果" else "下一题")
            }
        }
    }
}

@Composable
private fun QuizResultContent(
    correctCount: Int,
    totalCount: Int,
    accuracy: Float,
    onRetry: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when {
                accuracy >= 0.9f -> "🌟"
                accuracy >= 0.8f -> "🎉"
                accuracy >= 0.6f -> "👍"
                accuracy >= 0.4f -> "💪"
                else -> "📚"
            },
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = when {
                accuracy >= 0.9f -> "太厉害了！完美！"
                accuracy >= 0.8f -> "表现优秀！"
                accuracy >= 0.6f -> "不错的成绩！"
                accuracy >= 0.4f -> "继续加油！"
                else -> "还需要多练习哦"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(48.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$correctCount",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("正确", style = MaterialTheme.typography.bodyMedium)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${totalCount - correctCount}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text("错误", style = MaterialTheme.typography.bodyMedium)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(accuracy * 100).toInt()}%",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        accuracy >= 0.8f -> MaterialTheme.colorScheme.primary
                        accuracy >= 0.6f -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
                Text("正确率", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("再测一次")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("返回")
        }
    }
}
