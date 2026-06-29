package me.vishwas.androidexperimental.feature.news.presentation.search

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.vishwas.androidexperimental.core.network.NetworkResult
import me.vishwas.androidexperimental.feature.news.domain.model.Article
import me.vishwas.androidexperimental.feature.news.domain.usecase.SearchNewsUseCase
import javax.inject.Inject

sealed class SearchUiState {
    data object Idle : SearchUiState()
    data class Loading(val query: String) : SearchUiState()

    @Immutable
    data class Success(val articles: ImmutableList<Article>, val query: String) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchNewsUseCase: SearchNewsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun search(query: String) {
        if (query.isBlank()) { clearSearch(); return }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            _uiState.value = SearchUiState.Loading(query)
            _uiState.value = when (val result = searchNewsUseCase(query)) {
                is NetworkResult.Success -> SearchUiState.Success(result.data.toImmutableList(), query)
                is NetworkResult.Error -> SearchUiState.Error(result.message)
                else -> SearchUiState.Idle
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _uiState.value = SearchUiState.Idle
    }
}
