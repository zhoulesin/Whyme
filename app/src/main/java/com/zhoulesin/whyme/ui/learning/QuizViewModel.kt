package com.zhoulesin.whyme.ui.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.domain.model.*
import com.zhoulesin.whyme.domain.usecase.GetFavoriteWordsUseCase
import com.zhoulesin.whyme.domain.usecase.RecordLearningSessionUseCase
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
    private val getFavoriteWordsUseCase: GetFavoriteWordsUseCase,
    private val recordLearningSessionUseCase: RecordLearningSessionUseCase
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
                        learningState = LearningState.Error("加载测试单词失败: ${e.message}"),
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
                    learningState = LearningState.Error("没有足够的单词进行测试")
                )
            }
            return
        }

        val firstWord = quizWordPool.first()
        _uiState.update { state ->
            state.copy(
                learningState = LearningState.Testing(
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
        val currentState = _uiState.value.learningState
        if (currentState is LearningState.Testing && !_uiState.value.isAnswerRevealed) {
            _uiState.update { it.copy(selectedAnswer = answer, isAnswerRevealed = true) }
        }
    }

    /**
     * 下一题
     */
    fun nextQuestion() {
        val currentState = _uiState.value.learningState
        if (currentState is LearningState.Testing) {
            val isCorrect = _uiState.value.selectedAnswer == when (currentState.questionType) {
                QuestionType.WORD_TO_CHINESE -> currentState.currentWord.translation
                QuestionType.CHINESE_TO_WORD -> currentState.currentWord.word
                QuestionType.SPELLING -> currentState.currentWord.word
            }

            _uiState.update { state ->
                val newStats = state.sessionStats.copy(
                    wordsReviewed = state.sessionStats.wordsReviewed + 1,
                    correctCount = if (isCorrect) state.sessionStats.correctCount + 1 else state.sessionStats.correctCount
                )

                val nextIndex = currentState.index + 1
                if (nextIndex >= quizWordPool.size) {
                    // 测试完成 - 先记录会话
                    val durationSeconds = (System.currentTimeMillis() - state.sessionStats.startTime) / 1000
                    val finalStats = newStats

                    // 同步记录会话
                    kotlinx.coroutines.runBlocking {
                        recordLearningSessionUseCase(
                            wordsLearned = 0, // 测试不计入新词学习
                            wordsReviewed = finalStats.wordsReviewed,
                            correctCount = finalStats.correctCount,
                            durationSeconds = durationSeconds
                        )
                    }

                    state.copy(
                        learningState = LearningState.QuizResult(
                            correctCount = finalStats.correctCount,
                            totalCount = quizWordPool.size,
                            accuracy = finalStats.correctCount.toFloat() / quizWordPool.size,
                            mode = LearningMode.QUIZ
                        ),
                        sessionStats = finalStats,
                        selectedAnswer = null,
                        isAnswerRevealed = false
                    )
                } else {
                    // 下一题
                    val nextWord = quizWordPool[nextIndex]
                    state.copy(
                        learningState = currentState.copy(
                            currentWord = nextWord,
                            index = nextIndex,
                            options = generateQuizOptions(nextWord, currentState.questionType)
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
                learningState = LearningState.Idle,
                selectedAnswer = null,
                isAnswerRevealed = false,
                sessionStats = SessionStats()
            )
        }
    }
}

/**
 * 测试界面 UI 状态
 */
data class QuizUiState(
    // 单词数据
    val allLearnedWords: List<Word> = emptyList(),
    val favoriteWords: List<Word> = emptyList(),

    // 学习状态
    val learningState: LearningState = LearningState.Idle,
    val sessionStats: SessionStats = SessionStats(),

    // 测试状态
    val selectedAnswer: String? = null,
    val isAnswerRevealed: Boolean = false,
    val quizStartTime: Long = 0,

    // 数据加载状态
    val isDataLoaded: Boolean = false
)
