package com.zhoulesin.whyme.ui.wordbank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.domain.model.WordLevel
import com.zhoulesin.whyme.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class WordBankLibraryUiState(
    val words: List<Word> = emptyList(),
    val totalWords: Int = 0,
    val learnedWords: Int = 0,
    val masteredWords: Int = 0,
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WordBankLibraryViewModel @Inject constructor(
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val allCet6Words = wordRepository.getAllWords().map { words ->
        words.filter { it.level == WordLevel.CET6 }
    }

    val uiState: StateFlow<WordBankLibraryUiState> = combine(
        _searchQuery.flatMapLatest { q ->
            if (q.isBlank()) {
                wordRepository.getAllWords()
            } else {
                wordRepository.searchWords(q)
            }
        }.map { words ->
            words.filter { it.level == WordLevel.CET6 }.sortedBy { it.word.lowercase() }
        },
        allCet6Words
    ) { filteredWords, allWords ->
        val learnedWords = allWords.count { it.isLearned || it.reviewCount > 0 }
        val masteredWords = allWords.count { it.isMastered }
        WordBankLibraryUiState(
            words = filteredWords,
            totalWords = allWords.size,
            learnedWords = learnedWords,
            masteredWords = masteredWords,
            searchQuery = _searchQuery.value,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WordBankLibraryUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.update { query }
    }
}
