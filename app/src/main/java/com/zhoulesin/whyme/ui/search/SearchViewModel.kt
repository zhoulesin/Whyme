package com.zhoulesin.whyme.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.domain.usecase.SearchWordsUseCase
import com.zhoulesin.whyme.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchResultsState(
    val results: List<Word> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchWordsUseCase: SearchWordsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResultsState: StateFlow<SearchResultsState> = _searchQuery
        .debounce(200)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(SearchResultsState(hasSearched = false))
            } else {
                searchWordsUseCase(query).map { results ->
                    SearchResultsState(
                        results = results,
                        isSearching = false,
                        hasSearched = true
                    )
                }.onStart {
                    SearchResultsState(isSearching = true, hasSearched = true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchResultsState()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun toggleFavorite(wordId: Long) {
        viewModelScope.launch {
            toggleFavoriteUseCase(wordId)
        }
    }
}
