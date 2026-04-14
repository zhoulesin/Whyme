package com.zhoulesin.whyme.ui.learning

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.domain.model.*
import com.zhoulesin.whyme.domain.usecase.*
import com.zhoulesin.whyme.utils.TextToSpeechHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 复习ViewModel - 专门处理复习逻辑
 */
@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val getWordsForReviewUseCase: GetWordsForReviewUseCase,
    private val updateWordReviewUseCase: UpdateWordReviewUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState

    private var currentLevel: WordLevel = WordLevel.CET6
    private var ttsHelper: TextToSpeechHelper? = null

    /**
     * 初始化复习会话
     */
    fun initSession() {
        viewModelScope.launch {
            // 当前产品词域固定为 CET6，避免默认级别导致复习列表为空。
            _uiState.update { it.copy(currentLevel = currentLevel) }
            loadReviewWords()
        }
    }

    /**
     * 加载复习单词
     */
    private fun loadReviewWords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDataLoaded = false) }

            try {
                getWordsForReviewUseCase(20, currentLevel).collect { words ->
                    _uiState.update {state ->
                        state.copy(
                            wordsForReview = words,
                            isDataLoaded = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        reviewState = ReviewState.Error("加载复习单词失败: ${e.message}"),
                        isDataLoaded = true
                    )
                }
            }
        }
    }

    /**
     * 开始复习
     */
    fun startReview() {
        val words = _uiState.value.wordsForReview
        if (words.isEmpty()) {
            _uiState.update {
                it.copy(
                    reviewState = ReviewState.Error("没有待复习的单词")
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                reviewState = ReviewState.Reviewing(
                    currentWord = words[0],
                    index = 0,
                    total = words.size
                ),
                sessionStats = SessionStats(
                    startTime = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * 标记单词复习结果
     */
    fun markWord(result: ReviewResult) {
        viewModelScope.launch {
            val currentState = _uiState.value.reviewState
            if (currentState is ReviewState.Reviewing) {
                updateWordReviewUseCase(currentState.currentWord.id, result)

                _uiState.update { state ->
                    val newStats = state.sessionStats.copy(
                        wordsReviewed = state.sessionStats.wordsReviewed + 1,
                        correctCount = if (result == ReviewResult.GOOD || result == ReviewResult.EASY) {
                            state.sessionStats.correctCount + 1
                        } else state.sessionStats.correctCount
                    )

                    val nextIndex = currentState.index + 1
                    if (nextIndex >= currentState.total) {
                        // 复习完成
                        val finalStats = newStats

                        state.copy(
                            reviewState = ReviewState.Completed(
                                reviewed = finalStats.wordsReviewed,
                                correct = finalStats.correctCount,
                                accuracy = if (finalStats.wordsReviewed > 0) {
                                    finalStats.correctCount.toFloat() / finalStats.wordsReviewed
                                } else 0f
                            ),
                            sessionStats = finalStats,
                            isFlipped = false
                        )
                    } else {
                        // 下一个单词
                        val nextWord = state.wordsForReview.getOrNull(nextIndex) ?: currentState.currentWord
                        state.copy(
                            reviewState = currentState.copy(
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

    /**
     * 切换卡片翻转状态
     */
    fun flipCard() {
        _uiState.update {
            it.copy(isFlipped = !it.isFlipped)
        }
    }

    /**
     * 切换单词收藏状态
     */
    fun toggleFavorite(wordId: Long) {
        viewModelScope.launch {
            // 执行数据库操作并获取实际结果
            val isFavorite = toggleFavoriteUseCase(wordId)

            // 手动更新 UI 状态中对应单词的收藏状态
            _uiState.update { state ->
                // 查找要更新的单词，优先从当前复习状态中查找
                val updatedWord = when (val currentState = state.reviewState) {
                    is ReviewState.Reviewing -> {
                        if (currentState.currentWord.id == wordId) {
                            currentState.currentWord
                        } else {
                            state.wordsForReview.find { it.id == wordId }
                        }
                    }
                    else -> {
                        state.wordsForReview.find { it.id == wordId }
                    }
                }

                val newWordWithFavorite = updatedWord?.copy(isFavorite = isFavorite)

                state.copy(
                    wordsForReview = state.wordsForReview.map {
                        if (it.id == wordId) it.copy(isFavorite = isFavorite) else it
                    },
                    // 同时更新 reviewState 中的 currentWord
                    reviewState = when (val currentState = state.reviewState) {
                        is ReviewState.Reviewing -> {
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

    /**
     * 退出会话
     */
    fun exitSession() {
        _uiState.update {
            it.copy(
                reviewState = ReviewState.Idle,
                isFlipped = false,
                sessionStats = SessionStats()
            )
        }
    }

    fun initTTS(context: Context, callback: (Boolean) -> Unit) {
        ttsHelper = TextToSpeechHelper(context)
        ttsHelper?.initialize(callback)
    }

    fun speakWord(word: String) {
        ttsHelper?.speak(word)
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper?.shutdown()
    }
}
