package com.zhoulesin.whyme.ui.learning

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.domain.repository.WordRepository
import com.zhoulesin.whyme.domain.usecase.ToggleFavoriteUseCase
import com.zhoulesin.whyme.utils.TextToSpeechHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WordDetailUiState(
    val word: Word? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wordId: Long = savedStateHandle.get<Long>("wordId") ?: 0L

    private val _uiState = MutableStateFlow(WordDetailUiState())
    val uiState: StateFlow<WordDetailUiState> = _uiState.asStateFlow()

    private var ttsHelper: TextToSpeechHelper? = null

    init {
        loadWord()
    }

    private fun loadWord() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val word = wordRepository.getWordById(wordId)
                if (word != null) {
                    _uiState.update { 
                        it.copy(
                            word = word,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "单词不存在"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "加载失败"
                    )
                }
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            toggleFavoriteUseCase(wordId)
            // 重新加载单词以更新收藏状态
            loadWord()
        }
    }

    fun refresh() {
        loadWord()
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
