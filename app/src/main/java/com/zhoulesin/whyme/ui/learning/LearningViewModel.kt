package com.zhoulesin.whyme.ui.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.domain.model.*
import com.zhoulesin.whyme.domain.repository.WordRepository
import com.zhoulesin.whyme.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LearningUiState(
    val wordsToLearn: List<Word> = emptyList(),
    val wordsForReview: List<Word> = emptyList(),
    val favoriteWords: List<Word> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val learningState: LearningState = LearningState.Idle,
    val sessionStats: SessionStats = SessionStats()
)

data class SessionStats(
    val wordsLearned: Int = 0,
    val wordsReviewed: Int = 0,
    val correctCount: Int = 0,
    val startTime: Long = System.currentTimeMillis()
)

@HiltViewModel
class LearningViewModel @Inject constructor(
    private val getWordsForLearningUseCase: GetWordsForLearningUseCase,
    private val getWordsForReviewUseCase: GetWordsForReviewUseCase,
    private val getFavoriteWordsUseCase: GetFavoriteWordsUseCase,
    private val updateWordReviewUseCase: UpdateWordReviewUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    init {
        loadWords()
    }

    private fun loadWords() {
        viewModelScope.launch {
            combine(
                getWordsForLearningUseCase(),
                getWordsForReviewUseCase(),
                getFavoriteWordsUseCase()
            ) { toLearn, toReview, favorites ->
                Triple(toLearn, toReview, favorites)
            }.collect { (toLearn, toReview, favorites) ->
                _uiState.update { state ->
                    state.copy(
                        wordsToLearn = toLearn,
                        wordsForReview = toReview,
                        favoriteWords = favorites
                    )
                }
            }
        }
    }

    fun startLearning() {
        val words = _uiState.value.wordsToLearn
        if (words.isNotEmpty()) {
            _uiState.update { state ->
                state.copy(
                    learningState = LearningState.Learning(words.first(), 0, words.size),
                    sessionStats = SessionStats()
                )
            }
        }
    }

    fun startReview() {
        val words = _uiState.value.wordsForReview
        if (words.isNotEmpty()) {
            _uiState.update { state ->
                state.copy(
                    learningState = LearningState.Learning(words.first(), 0, words.size),
                    sessionStats = SessionStats()
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

                // 更新会话统计
                _uiState.update { state ->
                    val newStats = state.sessionStats.copy(
                        wordsReviewed = state.sessionStats.wordsReviewed + 1,
                        correctCount = if (result == ReviewResult.GOOD || result == ReviewResult.EASY) {
                            state.sessionStats.correctCount + 1
                        } else state.sessionStats.correctCount
                    )

                    // 移动到下一个单词
                    val nextIndex = currentState.index + 1
                    if (nextIndex >= currentState.total) {
                        // 学习完成
                        state.copy(
                            learningState = LearningState.Completed(
                                learned = newStats.wordsLearned,
                                reviewed = newStats.wordsReviewed,
                                accuracy = if (newStats.wordsReviewed > 0) {
                                    newStats.correctCount.toFloat() / newStats.wordsReviewed
                                } else 0f
                            ),
                            sessionStats = newStats,
                            isFlipped = false
                        )
                    } else {
                        // 获取下一个单词
                        val words = _uiState.value.wordsToLearn + _uiState.value.wordsForReview
                        val nextWord = if (nextIndex < words.size) words[nextIndex] else currentState.currentWord
                        state.copy(
                            learningState = LearningState.Learning(
                                currentWord = nextWord,
                                index = nextIndex,
                                total = currentState.total
                            ),
                            sessionStats = newStats,
                            isFlipped = false
                        )
                    }
                }
            }
        }
    }

    fun toggleFavorite(wordId: Long) {
        viewModelScope.launch {
            toggleFavoriteUseCase(wordId)
        }
    }

    fun resetLearning() {
        _uiState.update { it.copy(learningState = LearningState.Idle, isFlipped = false) }
        loadWords()
    }

    fun addSampleWords() {
        viewModelScope.launch {
            val sampleWords = listOf(
                Word(
                    word = "Hello",
                    phonetic = "/həˈloʊ/",
                    definition = "used as a greeting",
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
                )
            )
            wordRepository.insertWords(sampleWords)
            loadWords()
        }
    }
}
