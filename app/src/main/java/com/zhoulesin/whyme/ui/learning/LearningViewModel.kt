package com.zhoulesin.whyme.ui.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.domain.model.*
import com.zhoulesin.whyme.domain.repository.WordRepository
import com.zhoulesin.whyme.domain.repository.WordBankRepository
import com.zhoulesin.whyme.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 学习界面 UI 状态
 */
data class LearningUiState(
    // 单词数据
    val wordsToLearn: List<Word> = emptyList(),
    val wordsForReview: List<Word> = emptyList(),
    val favoriteWords: List<Word> = emptyList(),
    val allLearnedWords: List<Word> = emptyList(),

    // 学习状态
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val learningState: LearningState = LearningState.Idle,
    val sessionStats: SessionStats = SessionStats(),

    // 测试状态
    val currentQuizOptions: List<QuizOption> = emptyList(),
    val selectedAnswer: String? = null,
    val isAnswerRevealed: Boolean = false,
    val quizStartTime: Long = 0,

    // 统计数据
    val masteredCount: Int = 0,
    val learningCount: Int = 0,
    val unknownCount: Int = 0,
    
    // 数据加载状态
    val isDataLoaded: Boolean = false
)

/**
 * 会话统计
 */
data class SessionStats(
    val wordsLearned: Int = 0,
    val wordsReviewed: Int = 0,
    val correctCount: Int = 0,
    val totalQuestions: Int = 0,
    val startTime: Long = System.currentTimeMillis()
)

/**
 * 测试配置
 */
data class QuizConfig(
    val questionCount: Int = 10,
    val questionType: QuestionType = QuestionType.WORD_TO_CHINESE,
    val source: QuizSource = QuizSource.ALL
)

enum class QuizSource {
    TODAY_LEARNED,    // 今日学习
    ALL_LEARNED,      // 全部已学
    FAVORITES,        // 生词本
    ALL               // 全部
}

@HiltViewModel
class LearningViewModel @Inject constructor(
    private val getWordsForLearningUseCase: GetWordsForLearningUseCase,
    private val getWordsForReviewUseCase: GetWordsForReviewUseCase,
    private val getFavoriteWordsUseCase: GetFavoriteWordsUseCase,
    private val updateWordReviewUseCase: UpdateWordReviewUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val recordLearningSessionUseCase: RecordLearningSessionUseCase,
    private val wordRepository: WordRepository,
    private val wordBankRepository: WordBankRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    // 测验使用的单词池
    private var quizWordPool: List<Word> = emptyList()

    // 当前学习的级别
    private var currentLevel: WordLevel? = null

    init {
        loadCurrentLevel()
        loadStats()
    }

    private fun loadCurrentLevel() {
        viewModelScope.launch {
            wordBankRepository.getCurrentLevel().collect { level ->
                currentLevel = level
                loadWords(level)
            }
        }
    }

    private fun loadWords(level: WordLevel? = null) {
        viewModelScope.launch {
            // 订阅数据变化
            combine(
                getWordsForLearningUseCase(level = level),
                getWordsForReviewUseCase(level = level),
                getFavoriteWordsUseCase(level = level),
                wordRepository.getAllWords()
            ) { toLearn, toReview, favorites, all ->
                // 过滤已学习的单词用于测试（按级别筛选）
                val filteredAll = if (level != null) {
                    all.filter { it.level == level }
                } else all
                val learnedWords = filteredAll.filter { it.isLearned || it.reviewCount > 0 }
                Quad(toLearn, toReview, favorites, learnedWords)
            }.collect { (toLearn, toReview, favorites, learnedWords) ->
                _uiState.update { state ->
                    state.copy(
                        wordsToLearn = toLearn,
                        wordsForReview = toReview,
                        favoriteWords = favorites,
                        allLearnedWords = learnedWords,
                        isDataLoaded = true
                    )
                }
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            val mastered = wordRepository.getMasteredWordCount()
            val learning = wordRepository.getLearningWordCount()
            val unknown = wordRepository.getUnknownWordCount()
            _uiState.update { it.copy(masteredCount = mastered, learningCount = learning, unknownCount = unknown) }
        }
    }

    // ==================== 会话管理 ====================

    /**
     * 初始化学习会话（进入会话页面时调用）
     */
    fun initSession() {
        // 刷新单词数据
        loadWords()
    }

    /**
     * 退出学习会话（返回入口页面时调用）
     */
    fun exitSession() {
        _uiState.update { state ->
            state.copy(
                learningState = LearningState.Idle,
                isFlipped = false,
                sessionStats = SessionStats(),
                selectedAnswer = null,
                isAnswerRevealed = false
            )
        }
    }

    // ==================== 学习模式 ====================

    fun startLearning() {
        val words = _uiState.value.wordsToLearn
        if (words.isNotEmpty()) {
            _uiState.update { state ->
                state.copy(
                    learningState = LearningState.Learning(
                        currentWord = words.first(),
                        index = 0,
                        total = words.size,
                        mode = LearningMode.NEW_WORD
                    ),
                    sessionStats = SessionStats(),
                    isFlipped = false
                )
            }
        }
    }

    fun startReview() {
        val words = _uiState.value.wordsForReview
        if (words.isNotEmpty()) {
            _uiState.update { state ->
                state.copy(
                    learningState = LearningState.Learning(
                        currentWord = words.first(),
                        index = 0,
                        total = words.size,
                        mode = LearningMode.REVIEW
                    ),
                    sessionStats = SessionStats(),
                    isFlipped = false
                )
            }
        }
    }

    fun flipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun markWord(result: ReviewResult) {
        viewModelScope.launch {
            val currentState = _uiState.value.learningState
            if (currentState is LearningState.Learning) {
                updateWordReviewUseCase(currentState.currentWord.id, result)

                val isNewWord = currentState.mode == LearningMode.NEW_WORD

                _uiState.update { state ->
                    val newStats = state.sessionStats.copy(
                        wordsReviewed = state.sessionStats.wordsReviewed + 1,
                        wordsLearned = if (isNewWord) state.sessionStats.wordsLearned + 1 else state.sessionStats.wordsLearned,
                        correctCount = if (result == ReviewResult.GOOD || result == ReviewResult.EASY) {
                            state.sessionStats.correctCount + 1
                        } else state.sessionStats.correctCount
                    )

                    val nextIndex = currentState.index + 1
                    if (nextIndex >= currentState.total) {
                        // 学习完成 - 先记录会话，再更新状态
                        val durationSeconds = (System.currentTimeMillis() - state.sessionStats.startTime) / 1000
                        val finalStats = newStats

                        // 同步记录会话
                        kotlinx.coroutines.runBlocking {
                            recordLearningSessionUseCase(
                                wordsLearned = finalStats.wordsLearned,
                                wordsReviewed = finalStats.wordsReviewed,
                                correctCount = finalStats.correctCount,
                                durationSeconds = durationSeconds
                            )
                        }

                        state.copy(
                            learningState = LearningState.Completed(
                                learned = if (isNewWord) finalStats.wordsLearned else 0,
                                reviewed = finalStats.wordsReviewed,
                                accuracy = if (finalStats.wordsReviewed > 0) {
                                    finalStats.correctCount.toFloat() / finalStats.wordsReviewed
                                } else 0f
                            ),
                            sessionStats = finalStats,
                            isFlipped = false
                        )
                    } else {
                        // 下一个单词
                        val words = if (isNewWord) {
                            state.wordsToLearn
                        } else {
                            state.wordsForReview
                        }
                        val nextWord = words.getOrNull(nextIndex) ?: currentState.currentWord
                        state.copy(
                            learningState = currentState.copy(
                                currentWord = nextWord,
                                index = nextIndex
                            ),
                            sessionStats = newStats,
                            isFlipped = false
                        )
                    }
                }
            }
        }
    }

    // ==================== 测试模式 ====================

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
            QuizSource.ALL -> _uiState.value.wordsToLearn + _uiState.value.allLearnedWords
        }.shuffled().take(config.questionCount)

        if (quizWordPool.isNotEmpty()) {
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
    }

    /**
     * 生成测试选项
     */
    private fun generateQuizOptions(word: Word, questionType: QuestionType): List<QuizOption> {
        // 从所有单词中获取干扰选项
        val allTranslations = _uiState.value.wordsToLearn
            .plus(_uiState.value.allLearnedWords)
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

    // ==================== 收藏功能 ====================

    fun toggleFavorite(wordId: Long) {
        viewModelScope.launch {
            // 先获取当前单词的收藏状态
            val currentWord = _uiState.value.wordsToLearn.find { it.id == wordId }
                ?: _uiState.value.wordsForReview.find { it.id == wordId }
            val willBeFavorite = !(currentWord?.isFavorite ?: false)

            toggleFavoriteUseCase(wordId)

            // 手动更新 UI 状态中对应单词的收藏状态
            _uiState.update { state ->
                val updatedWord = state.wordsToLearn.find { it.id == wordId }
                    ?: state.wordsForReview.find { it.id == wordId }

                val newWordWithFavorite = updatedWord?.copy(isFavorite = willBeFavorite)

                state.copy(
                    wordsToLearn = state.wordsToLearn.map {
                        if (it.id == wordId) it.copy(isFavorite = willBeFavorite) else it
                    },
                    wordsForReview = state.wordsForReview.map {
                        if (it.id == wordId) it.copy(isFavorite = willBeFavorite) else it
                    },
                    // 如果取消收藏，从 favoriteWords 中移除；如果是添加收藏，添加进去
                    favoriteWords = if (willBeFavorite) {
                        // 添加到收藏列表
                        val wordToAdd = newWordWithFavorite ?: return@update state
                        state.favoriteWords + wordToAdd
                    } else {
                        // 从收藏列表移除
                        state.favoriteWords.filter { it.id != wordId }
                    },
                    // 同时更新 learningState 中的 currentWord
                    learningState = when (val currentState = state.learningState) {
                        is LearningState.Learning -> {
                            if (currentState.currentWord.id == wordId && newWordWithFavorite != null) {
                                currentState.copy(currentWord = newWordWithFavorite)
                            } else {
                                currentState
                            }
                        }
                        else -> currentState
                    }
                )
            }
        }
    }

    // ==================== 状态重置 ====================

    fun resetLearning() {
        _uiState.update { it.copy(learningState = LearningState.Idle, isFlipped = false) }
        loadWords()
    }

    /**
     * 添加示例单词（用于演示）
     */
    fun addSampleWords() {
        viewModelScope.launch {
            val sampleWords = listOf(
                Word(
                    word = "Hello",
                    phonetic = "/həˈloʊ/",
                    definition = "used as a greeting when meeting someone",
                    example = "Hello, how are you?",
                    translation = "你好"
                ),
                Word(
                    word = "Beautiful",
                    phonetic = "/ˈbjuːtɪfəl/",
                    definition = "pleasing the senses aesthetically",
                    example = "What a beautiful day!",
                    translation = "美丽的，美好的"
                ),
                Word(
                    word = "Knowledge",
                    phonetic = "/ˈnɑːlɪdʒ/",
                    definition = "facts, information, and skills acquired through experience or education",
                    example = "Knowledge is power.",
                    translation = "知识，学识"
                ),
                Word(
                    word = "Journey",
                    phonetic = "/ˈdʒɜːrni/",
                    definition = "an act of traveling from one place to another",
                    example = "Life is a journey, not a destination.",
                    translation = "旅程，旅途"
                ),
                Word(
                    word = "Challenge",
                    phonetic = "/ˈtʃælɪndʒ/",
                    definition = "a task or situation that tests someone's abilities",
                    example = "I love a good challenge.",
                    translation = "挑战，考验"
                ),
                Word(
                    word = "Perseverance",
                    phonetic = "/ˌpɜːrsəˈvɪrəns/",
                    definition = "persistence in doing something despite difficulty",
                    example = "Success comes to those who have perseverance.",
                    translation = "坚持不懈，毅力"
                ),
                Word(
                    word = "Innovation",
                    phonetic = "/ˌɪnəˈveɪʃən/",
                    definition = "a new method, idea, or product",
                    example = "Innovation is the key to progress.",
                    translation = "创新，改革"
                ),
                Word(
                    word = "Serendipity",
                    phonetic = "/ˌserənˈdɪpəti/",
                    definition = "the occurrence of events by chance in a happy way",
                    example = "Finding this book was pure serendipity.",
                    translation = "意外发现美好的运气"
                )
            )
            wordRepository.insertWords(sampleWords)
            loadWords()
            loadStats()
        }
    }
}

/**
 * 四元组
 */
data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
