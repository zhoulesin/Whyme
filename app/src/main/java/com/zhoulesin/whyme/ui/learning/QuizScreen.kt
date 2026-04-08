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
import com.zhoulesin.whyme.domain.model.LearningState
import com.zhoulesin.whyme.domain.model.QuestionType
import com.zhoulesin.whyme.domain.model.ReviewResult
import com.zhoulesin.whyme.ui.components.MasteryButtons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onNavigateBack: () -> Unit,
    onQuizComplete: () -> Unit,
    viewModel: LearningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var isAnswerCorrect by remember { mutableStateOf<Boolean?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("单词测试") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                        wordCount = uiState.wordsToLearn.size + uiState.wordsForReview.size,
                        onStart = { viewModel.startLearning() }
                    )
                }

                is LearningState.Learning -> {
                    // 测试进行中
                    QuizContent(
                        word = state.currentWord,
                        questionNumber = state.index + 1,
                        totalQuestions = state.total,
                        selectedAnswer = selectedAnswer,
                        isAnswerCorrect = isAnswerCorrect,
                        onSelectAnswer = { answer ->
                            selectedAnswer = answer
                            isAnswerCorrect = answer == state.currentWord.translation
                        },
                        onNext = {
                            selectedAnswer = null
                            isAnswerCorrect = null
                            val result = if (isAnswerCorrect == true) ReviewResult.GOOD else ReviewResult.AGAIN
                            viewModel.markWord(result)
                        }
                    )
                }

                is LearningState.Completed -> {
                    // 测试完成
                    QuizCompletedContent(
                        correctCount = state.learned + state.reviewed,
                        totalCount = state.learned + state.reviewed,
                        accuracy = state.accuracy,
                        onFinish = {
                            viewModel.resetLearning()
                            onQuizComplete()
                        }
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun QuizStartContent(
    wordCount: Int,
    onStart: () -> Unit
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
            text = "共 $wordCount 个单词",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStart,
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
    }
}

@Composable
private fun QuizContent(
    word: com.zhoulesin.whyme.domain.model.Word,
    questionNumber: Int,
    totalQuestions: Int,
    selectedAnswer: String?,
    isAnswerCorrect: Boolean?,
    onSelectAnswer: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 进度
        LinearProgressIndicator(
            progress = { questionNumber.toFloat() / totalQuestions },
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = "$questionNumber / $totalQuestions",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 问题
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
                    text = "这个单词是什么意思？",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = word.word,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = word.phonetic,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 答案选项
        val options = remember { generateOptions(word.translation) }

        options.forEach { option ->
            val isSelected = selectedAnswer == option
            val backgroundColor = when {
                isAnswerCorrect != null && option == word.translation -> MaterialTheme.colorScheme.primaryContainer
                isAnswerCorrect == true && isSelected -> MaterialTheme.colorScheme.primaryContainer
                isAnswerCorrect == false && isSelected -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            }

            OutlinedCard(
                onClick = { if (isAnswerCorrect == null) onSelectAnswer(option) },
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
                        text = option,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    if (isAnswerCorrect != null) {
                        if (option == word.translation) {
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

        Spacer(modifier = Modifier.weight(1f))

        // 下一题按钮
        if (isAnswerCorrect != null) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("下一题")
            }
        }
    }
}

@Composable
private fun QuizCompletedContent(
    correctCount: Int,
    totalCount: Int,
    accuracy: Float,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (accuracy >= 0.8f) "🎉" else if (accuracy >= 0.5f) "👍" else "💪",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "测试完成！",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$correctCount / $totalCount",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text("正确数", style = MaterialTheme.typography.bodySmall)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(accuracy * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        accuracy >= 0.8f -> MaterialTheme.colorScheme.primary
                        accuracy >= 0.5f -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
                Text("正确率", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("完成", style = MaterialTheme.typography.titleMedium)
        }
    }
}

// 生成选项（简单实现）
private fun generateOptions(correctAnswer: String): List<String> {
    val options = mutableListOf(correctAnswer)
    val fakeAnswers = listOf(
        "好的", "学习", "工作", "生活", "时间", "世界", "朋友", "家庭",
        "快乐", "成功", "努力", "坚持", "梦想", "未来", "希望", "勇气"
    )
    while (options.size < 4) {
        val fake = fakeAnswers.filter { it != correctAnswer }.randomOrNull() ?: continue
        if (!options.contains(fake)) {
            options.add(fake)
        }
    }
    return options.shuffled()
}
