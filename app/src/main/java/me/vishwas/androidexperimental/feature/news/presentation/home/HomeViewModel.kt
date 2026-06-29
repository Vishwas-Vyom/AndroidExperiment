package me.vishwas.androidexperimental.feature.news.presentation.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.vishwas.androidexperimental.core.domain.NoParams
import me.vishwas.androidexperimental.core.network.NetworkResult
import me.vishwas.androidexperimental.feature.news.domain.model.Article
import me.vishwas.androidexperimental.feature.news.domain.model.NewsCategory
import me.vishwas.androidexperimental.feature.news.domain.repository.NewsRepository
import me.vishwas.androidexperimental.feature.news.domain.usecase.GetNewsByCategoryUseCase
import me.vishwas.androidexperimental.feature.news.domain.usecase.GetTopHeadlinesUseCase
import me.vishwas.androidexperimental.feature.news.domain.usecase.ToggleBookmarkUseCase
import javax.inject.Inject

sealed class HomeUiState {
    data object Loading : HomeUiState()

    @Immutable
    data class Success(
        val articles: ImmutableList<Article>,
        val selectedCategory: NewsCategory,
        val isRefreshing: Boolean = false,
    ) : HomeUiState() {
        val featuredArticle: Article? = articles.firstOrNull { it.imageUrl != null }
        val regularArticles: ImmutableList<Article> = articles.filter { it != featuredArticle }.toImmutableList()
    }

    data class Error(val message: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTopHeadlinesUseCase: GetTopHeadlinesUseCase,
    private val getNewsByCategoryUseCase: GetNewsByCategoryUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val newsRepository: NewsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Article shown in the detail pane on medium/expanded screens.
    private val _paneArticle = MutableStateFlow<Article?>(null)
    val paneArticle: StateFlow<Article?> = _paneArticle.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val isPaneArticleBookmarked: StateFlow<Boolean> = _paneArticle
        .flatMapLatest { article ->
            if (article?.url != null) newsRepository.isBookmarked(article.url)
            else flowOf(false)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        loadNews(NewsCategory.GENERAL)
    }

    fun selectCategory(category: NewsCategory) {
        val current = _uiState.value
        if (current is HomeUiState.Success && current.selectedCategory == category) return
        loadNews(category)
    }

    fun refresh() {
        val current = _uiState.value as? HomeUiState.Success ?: run { loadNews(NewsCategory.GENERAL); return }
        _uiState.value = current.copy(isRefreshing = true)
        viewModelScope.launch {
            val result = fetchArticles(current.selectedCategory)
            _uiState.value = when (result) {
                is NetworkResult.Success -> HomeUiState.Success(result.data.toImmutableList(), current.selectedCategory)
                is NetworkResult.Error -> current.copy(isRefreshing = false)
                else -> current
            }
        }
    }

    fun selectPaneArticle(article: Article) {
        _paneArticle.value = article
    }

    fun togglePaneBookmark() {
        viewModelScope.launch {
            _paneArticle.value?.let { toggleBookmarkUseCase(it) }
        }
    }

    private fun loadNews(category: NewsCategory) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val result = fetchArticles(category)
            _uiState.value = when (result) {
                is NetworkResult.Success -> HomeUiState.Success(result.data.toImmutableList(), category)
                is NetworkResult.Error -> HomeUiState.Error(result.message)
                else -> HomeUiState.Loading
            }
        }
    }

    private suspend fun fetchArticles(category: NewsCategory): NetworkResult<List<Article>> =
        if (category == NewsCategory.GENERAL) getTopHeadlinesUseCase(NoParams)
        else getNewsByCategoryUseCase(category.value)
}
