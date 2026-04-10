package com.zhoulesin.whyme.ui.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.domain.model.*
import com.zhoulesin.whyme.domain.usecase.GetFavoriteWordsUseCase
import com.zhoulesin.whyme.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 测试ViewModel - 专门处理测试逻辑
 */
@HiltViewModel
class QuizViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val getFavoriteWordsUseCase: GetFavoriteWordsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState

    // 测验使用的单词池
    private var quizWordPool: List<Word> = emptyList()

    /**
     * 初始化测试会话
     */
    fun initSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDataLoaded = false) }

            try {
                // 加载已学单词和收藏单词
                val allLearnedWords = wordRepository.getAllLearnedWords().first()
                val favoriteWords = getFavoriteWordsUseCase().first()

                _uiState.update {
                    it.copy(
                        allLearnedWords = allLearnedWords,
                        favoriteWords = favoriteWords,
                        isDataLoaded = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        quizState = QuizState.Error("加载测试单词失败: ${e.message}"),
                        isDataLoaded = true
                    )
                }
            }
        }
    }

    /**
     * 开始测试
     * @param config 测试配置
     */
    fun startQuiz(config: QuizConfig = QuizConfig()) {
        // 根据配置选择单词池
        quizWordPool = when (config.source) {
            QuizSource.TODAY_LEARNED -> _uiState.value.allLearnedWords
            QuizSource.ALL_LEARNED -> _uiState.value.allLearnedWords
            QuizSource.FAVORITES -> _uiState.value.favoriteWords
            QuizSource.ALL -> _uiState.value.allLearnedWords
        }.shuffled().take(config.questionCount)

        if (quizWordPool.isEmpty()) {
            _uiState.update {
                it.copy(
                    quizState = QuizState.Error("没有足够的单词进行测试")
                )
            }
            return
        }

        val firstWord = quizWordPool.first()
        _uiState.update { state ->
            state.copy(
                quizState = QuizState.Testing(
                    currentWord = firstWord,
                    questionType = config.questionType,
                    index = 0,
                    total = quizWordPool.size,
                    options = generateQuizOptions(firstWord, config.questionType)
                ),
                sessionStats = SessionStats(totalQuestions = quizWordPool.size),
                quizStartTime = System.currentTimeMillis(),
                selectedAnswer = null,
                isAnswerRevealed = false
            )
        }
    }

    /**
     * 生成测试选项
     */
    private fun generateQuizOptions(word: Word, questionType: QuestionType): List<QuizOption> {
        // 从所有单词中获取干扰选项
        val allTranslations = _uiState.value.allLearnedWords
            .map { it.translation }
            .filter { it != word.translation }
            .distinct()
            .shuffled()
            .take(3)

        val correctAnswer = when (questionType) {
            QuestionType.WORD_TO_CHINESE -> word.translation
            QuestionType.CHINESE_TO_WORD -> word.word
            QuestionType.SPELLING -> "" // 拼写题不需要选项
        }

        val options = if (questionType == QuestionType.SPELLING) {
            emptyList()
        } else {
            val correct = QuizOption(correctAnswer, true)
            val distractors = allTranslations.map { QuizOption(it, false) }
            (listOf(correct) + distractors).shuffled()
        }

        return options
    }

    /**
     * 选择答案
     */
    fun selectAnswer(answer: String) {
        val currentQuizState = _uiState.value.quizState
        if (currentQuizState is QuizState.Testing && !_uiState.value.isAnswerRevealed) {
            _uiState.update { it.copy(selectedAnswer = answer, isAnswerRevealed = true) }
        }
    }

    /**
     * 下一题
     */
    fun nextQuestion() {
        val currentQuizState = _uiState.value.quizState
        if (currentQuizState is QuizState.Testing) {
            val isCorrect = _uiState.value.selectedAnswer == when (currentQuizState.questionType) {
                QuestionType.WORD_TO_CHINESE -> currentQuizState.currentWord.translation
                QuestionType.CHINESE_TO_WORD -> currentQuizState.currentWord.word
                QuestionType.SPELLING -> currentQuizState.currentWord.word
            }

            _uiState.update { state ->
                val newStats = state.sessionStats.copy(
                    wordsReviewed = 0, // 测试不计入复习数量
                    correctCount = if (isCorrect) state.sessionStats.correctCount + 1 else state.sessionStats.correctCount
                )

                val nextIndex = currentQuizState.index + 1
                if (nextIndex >= quizWordPool.size) {
                    // 测试完成 - 记录测试记录
                    val durationSeconds = (System.currentTimeMillis() - state.sessionStats.startTime) / 1000
                    val finalStats = newStats
                    val accuracy = finalStats.correctCount.toFloat() / quizWordPool.size

                    // 同步记录测试记录
                    kotlinx.coroutines.runBlocking {
                        // 记录测试记录
                        wordRepository.recordTest(
                            testType = currentQuizState.questionType.name,
                            totalQuestions = quizWordPool.size,
                            correctCount = finalStats.correctCount,
                            accuracy = accuracy,
                            durationSeconds = durationSeconds.toInt(),
                            questionCount = quizWordPool.size,
                            source = currentQuizState.questionType.name // 使用测试类型作为来源
                        )
                    }

                    state.copy(
                        quizState = QuizState.Result(
                            correctCount = finalStats.correctCount,
                            totalCount = quizWordPool.size,
                            accuracy = accuracy
                        ),
                        sessionStats = finalStats,
                        selectedAnswer = null,
                        isAnswerRevealed = false
                    )
                } else {
                    // 下一题
                    val nextWord = quizWordPool[nextIndex]
                    state.copy(
                        quizState = currentQuizState.copy(
                            currentWord = nextWord,
                            index = nextIndex,
                            options = generateQuizOptions(nextWord, currentQuizState.questionType)
                        ),
                        sessionStats = newStats,
                        selectedAnswer = null,
                        isAnswerRevealed = false
                    )
                }
            }
        }
    }

    /**
     * 退出会话
     */
    fun exitSession() {
        _uiState.update {
            it.copy(
                quizState = QuizState.Idle,
                selectedAnswer = null,
                isAnswerRevealed = false,
                sessionStats = SessionStats()
            )
        }
    }
}
