package com.zhoulesin.whyme.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.domain.repository.WordRepository
import com.zhoulesin.whyme.domain.usecase.GetFavoriteWordsUseCase
import com.zhoulesin.whyme.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val favoriteWords: List<Word> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = ""
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoriteWordsUseCase: GetFavoriteWordsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<FavoritesUiState> = combine(
        getFavoriteWordsUseCase(),
        _searchQuery
    ) { words, query ->
        val filteredWords = if (query.isBlank()) {
            words
        } else {
            words.filter {
                it.word.contains(query, ignoreCase = true) ||
                it.translation.contains(query, ignoreCase = true)
            }
        }
        FavoritesUiState(
            favoriteWords = filteredWords,
            isLoading = false,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FavoritesUiState()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(wordId: Long) {
        viewModelScope.launch {
            toggleFavoriteUseCase(wordId)
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }
}
