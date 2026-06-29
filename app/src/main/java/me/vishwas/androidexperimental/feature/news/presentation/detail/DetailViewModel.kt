package me.vishwas.androidexperimental.feature.news.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.vishwas.androidexperimental.core.common.ArticleCache
import me.vishwas.androidexperimental.feature.news.domain.model.Article
import me.vishwas.androidexperimental.feature.news.domain.repository.NewsRepository
import me.vishwas.androidexperimental.feature.news.domain.usecase.ToggleBookmarkUseCase
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val newsRepository: NewsRepository,
) : ViewModel() {

    val article: Article? = ArticleCache.selectedArticle

    val isBookmarked: StateFlow<Boolean> = article?.url?.let { url ->
        newsRepository.isBookmarked(url)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    } ?: MutableStateFlow(false)

    fun toggleBookmark() {
        val currentArticle = article ?: return
        viewModelScope.launch { toggleBookmarkUseCase(currentArticle) }
    }
}
