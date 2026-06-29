package me.vishwas.androidexperimental.feature.news.presentation.bookmarks

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.vishwas.androidexperimental.feature.news.domain.model.Article
import me.vishwas.androidexperimental.feature.news.domain.usecase.GetBookmarksUseCase
import me.vishwas.androidexperimental.feature.news.domain.usecase.ToggleBookmarkUseCase
import javax.inject.Inject

sealed class BookmarksUiState {
    data object Loading : BookmarksUiState()
    data object Empty : BookmarksUiState()

    @Immutable
    data class Success(val articles: ImmutableList<Article>) : BookmarksUiState()
}

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
) : ViewModel() {

    val uiState: StateFlow<BookmarksUiState> = getBookmarksUseCase()
        .map { articles ->
            if (articles.isEmpty()) BookmarksUiState.Empty
            else BookmarksUiState.Success(articles.toImmutableList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BookmarksUiState.Loading)

    fun removeBookmark(article: Article) {
        viewModelScope.launch { toggleBookmarkUseCase(article) }
    }
}
