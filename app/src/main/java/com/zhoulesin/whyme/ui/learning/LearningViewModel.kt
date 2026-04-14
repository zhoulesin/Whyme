package com.zhoulesin.whyme.ui.learning

import QuizState
import ReviewState
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.domain.model.*
import com.zhoulesin.whyme.domain.repository.WordRepository
import com.zhoulesin.whyme.domain.usecase.*
import com.zhoulesin.whyme.utils.TextToSpeechHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 测试来源枚举
 */
enum class QuizSource {
    TODAY_LEARNED,  // 今日学习的单词
    ALL_LEARNED,    // 所有已学的单词
    FAVORITES,      // 收藏的单词
    ALL             // 所有单词
}

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

    // 统计数据
    val masteredCount: Int = 0,
    val learningCount: Int = 0,
    val unknownCount: Int = 0,
    
    // 数据加载状态
    val isDataLoaded: Boolean = false,
    val currentLevel: WordLevel? = null
)

/**
 * 复习界面 UI 状态
 */
data class ReviewUiState(
    // 单词数据
    val wordsForReview: List<Word> = emptyList(),

    // 复习状态
    val isFlipped: Boolean = false,
    val reviewState: ReviewState = ReviewState.Idle,
    val sessionStats: SessionStats = SessionStats(),

    // 数据加载状态
    val isDataLoaded: Boolean = false,
    val currentLevel: WordLevel? = null
)

/**
 * 测试界面 UI 状态
 */
data class QuizUiState(
    // 单词数据
    val allLearnedWords: List<Word> = emptyList(),
    val favoriteWords: List<Word> = emptyList(),

    // 测试状态
    val quizState: QuizState = QuizState.Idle,
    val selectedAnswer: String? = null,
    val isAnswerRevealed: Boolean = false,
    val quizStartTime: Long = 0,
    val sessionStats: SessionStats = SessionStats(),

    // 数据加载状态
    val isDataLoaded: Boolean = false,
    val currentLevel: WordLevel? = null
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

@HiltViewModel
class LearningViewModel @Inject constructor(
    private val getWordsForLearningUseCase: GetWordsForLearningUseCase,
    private val getWordsForReviewUseCase: GetWordsForReviewUseCase,
    private val getFavoriteWordsUseCase: GetFavoriteWordsUseCase,
    private val updateWordReviewUseCase: UpdateWordReviewUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val recordLearningSessionUseCase: RecordLearningSessionUseCase,
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    // TTS 助手
    private var ttsHelper: TextToSpeechHelper? = null

    // 当前学习的级别
    private var currentLevel: WordLevel? = null

    init {
        observeWordPools()
        loadStats()
    }

    private fun observeWordPools() {
        viewModelScope.launch {
            combine(
                getWordsForLearningUseCase(level = WordLevel.CET6),
                getWordsForReviewUseCase(level = WordLevel.CET6),
                getFavoriteWordsUseCase(),
                wordRepository.getAllWords()
            ) { toLearn, toReview, favorites, allWords ->
                val cet6Words = allWords.filter { it.level == WordLevel.CET6 }
                val allLearnedWords = cet6Words.filter { it.isLearned || it.reviewCount > 0 }
                // 兜底：若进度表未就绪导致用例返回空，则直接从 CET6 词库随机取一批可学习词
                val fallbackToLearn = cet6Words
                    .filter { !it.isLearned }
                    .shuffled()
                    .take(20)
                Quint(
                    WordLevel.CET6,
                    if (toLearn.isNotEmpty()) toLearn else fallbackToLearn,
                    toReview,
                    favorites.filter { it.level == WordLevel.CET6 },
                    allLearnedWords
                )
            }.collect { (level, toLearn, toReview, favorites, allLearnedWords) ->
                currentLevel = level
                _uiState.update { state ->
                    state.copy(
                        wordsToLearn = toLearn,
                        wordsForReview = toReview,
                        favoriteWords = favorites,
                        allLearnedWords = allLearnedWords,
                        isDataLoaded = true,
                        currentLevel = level
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
        // 单词池在 init 中持续订阅，这里只保留会话入口语义。
    }

    /**
     * 退出学习会话（返回入口页面时调用）
     */
    fun exitSession() {
        _uiState.update { state ->
            state.copy(
                learningState = LearningState.Idle,
                isFlipped = false,
                sessionStats = SessionStats()
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

    fun flipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun markWord(result: ReviewResult) {
        viewModelScope.launch {
            val currentState = _uiState.value.learningState
            
            // 处理学习模式
            if (currentState is LearningState.Learning) {
                val word = currentState.currentWord
                val masteryLevel = when (result) {
                    ReviewResult.EASY -> 5
                    ReviewResult.GOOD -> 4
                    ReviewResult.HARD -> 2
                    ReviewResult.AGAIN -> 1
                }
                
                // 更新单词复习信息
                updateWordReviewUseCase(word.id, result)
                
                val isNewWord = currentState.mode == LearningMode.NEW_WORD
                
                // 记录学习记录
                wordRepository.recordWordLearning(word.id, word.level.name, masteryLevel)
                
                _uiState.update { state ->
                    val newStats = state.sessionStats.copy(
                        wordsReviewed = if (isNewWord) state.sessionStats.wordsReviewed else state.sessionStats.wordsReviewed + 1,
                        wordsLearned = if (isNewWord) state.sessionStats.wordsLearned + 1 else state.sessionStats.wordsLearned,
                        correctCount = if (result == ReviewResult.GOOD || result == ReviewResult.EASY) {
                            state.sessionStats.correctCount + 1
                        } else state.sessionStats.correctCount
                    )

                    val nextIndex = currentState.index + 1
                    if (nextIndex >= currentState.total) {
                        // 学习完成 - 先记录会话，再更新状态
                        val totalDurationSeconds = (System.currentTimeMillis() - state.sessionStats.startTime) / 1000
                        val finalStats = newStats

                        // 同步记录会话
                        kotlinx.coroutines.runBlocking {
                            recordLearningSessionUseCase(
                                wordsLearned = finalStats.wordsLearned,
                                wordsReviewed = if (isNewWord) 0 else finalStats.wordsReviewed,
                                correctCount = finalStats.correctCount,
                                durationSeconds = totalDurationSeconds
                            )
                            
                            val todayStart = java.time.LocalDate.now().atStartOfDay().toEpochSecond(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())) * 1000

                            // 记录打卡
                            wordRepository.recordCheckIn(
                                date = todayStart,
                                learningMinutes = (totalDurationSeconds / 60).toInt(),
                                wordsLearned = finalStats.wordsLearned,
                                wordsReviewed = if (isNewWord) 0 else finalStats.wordsReviewed
                            )
                        }

                        state.copy(
                            learningState = LearningState.Completed(
                                learned = if (isNewWord) finalStats.wordsLearned else 0,
                                reviewed = if (isNewWord) 0 else finalStats.wordsReviewed
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
                                index = nextIndex,
                                startTime = System.currentTimeMillis()
                            ),
                            sessionStats = newStats,
                            isFlipped = false
                        )
                    }
                }
            }
        }
    }

    // ==================== 收藏功能 ====================

    fun toggleFavorite(wordId: Long) {
        viewModelScope.launch {
            // 执行数据库操作并获取实际结果
            val isFavorite = toggleFavoriteUseCase(wordId)

            // 手动更新 UI 状态中对应单词的收藏状态
            _uiState.update { state ->
                // 查找要更新的单词，优先从当前学习状态中查找
                val updatedWord = when (val currentState = state.learningState) {
                    is LearningState.Learning -> {
                        if (currentState.currentWord.id == wordId) {
                            currentState.currentWord
                        } else {
                            state.wordsToLearn.find { it.id == wordId }
                                ?: state.wordsForReview.find { it.id == wordId }
                        }
                    }
                    else -> {
                        state.wordsToLearn.find { it.id == wordId }
                            ?: state.wordsForReview.find { it.id == wordId }
                    }
                }

                val newWordWithFavorite = updatedWord?.copy(isFavorite = isFavorite)

                state.copy(
                    wordsToLearn = state.wordsToLearn.map {
                        if (it.id == wordId) it.copy(isFavorite = isFavorite) else it
                    },
                    wordsForReview = state.wordsForReview.map {
                        if (it.id == wordId) it.copy(isFavorite = isFavorite) else it
                    },
                    // 如果添加收藏，添加到收藏列表；如果取消收藏，从收藏列表移除
                    favoriteWords = if (isFavorite) {
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
                    translation = "你好",
                    level = currentLevel ?: WordLevel.DEFAULT
                ),
                Word(
                    word = "Beautiful",
                    phonetic = "/ˈbjuːtɪfəl/",
                    definition = "pleasing the senses aesthetically",
                    example = "What a beautiful day!",
                    translation = "美丽的，美好的",
                    level = currentLevel ?: WordLevel.DEFAULT
                ),
                Word(
                    word = "Knowledge",
                    phonetic = "/ˈnɑːlɪdʒ/",
                    definition = "facts, information, and skills acquired through experience or education",
                    example = "Knowledge is power.",
                    translation = "知识，学识",
                    level = currentLevel ?: WordLevel.DEFAULT
                ),
                Word(
                    word = "Journey",
                    phonetic = "/ˈdʒɜːrni/",
                    definition = "an act of traveling from one place to another",
                    example = "Life is a journey, not a destination.",
                    translation = "旅程，旅途",
                    level = currentLevel ?: WordLevel.DEFAULT
                ),
                Word(
                    word = "Challenge",
                    phonetic = "/ˈtʃælɪndʒ/",
                    definition = "a task or situation that tests someone's abilities",
                    example = "I love a good challenge.",
                    translation = "挑战，考验",
                    level = currentLevel ?: WordLevel.DEFAULT
                ),
                Word(
                    word = "Perseverance",
                    phonetic = "/ˌpɜːrsəˈvɪrəns/",
                    definition = "persistence in doing something despite difficulty",
                    example = "Success comes to those who have perseverance.",
                    translation = "坚持不懈，毅力",
                    level = currentLevel ?: WordLevel.DEFAULT
                ),
                Word(
                    word = "Innovation",
                    phonetic = "/ˌɪnəˈveɪʃən/",
                    definition = "a new method, idea, or product",
                    example = "Innovation is the key to progress.",
                    translation = "创新，改革",
                    level = currentLevel ?: WordLevel.DEFAULT
                ),
                Word(
                    word = "Serendipity",
                    phonetic = "/ˌserənˈdɪpəti/",
                    definition = "the occurrence of events by chance in a happy way",
                    example = "Finding this book was pure serendipity.",
                    translation = "意外发现美好的运气",
                    level = currentLevel ?: WordLevel.DEFAULT
                )
            )
            wordRepository.insertWords(sampleWords)
            loadStats()
        }
    }

    // ==================== TTS 功能 ====================

    /**
     * 初始化 TTS
     */
    fun initTTS(context: Context, callback: (Boolean) -> Unit) {
        ttsHelper = TextToSpeechHelper(context)
        ttsHelper?.initialize(callback)
    }

    /**
     * 播放单词发音
     */
    fun speakWord(word: String) {
        ttsHelper?.speak(word)
    }

    /**
     * 停止发音
     */
    fun stopSpeaking() {
        ttsHelper?.stop()
    }

    /**
     * 关闭 TTS
     */
    override fun onCleared() {
        super.onCleared()
        ttsHelper?.shutdown()
    }
}

/**
 * 五元组
 */
data class Quint<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
