package me.vishwas.androidexperimental.feature.news.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.vishwas.androidexperimental.feature.news.domain.model.Article

@Composable
fun HomeScreen(
    onNavigateToDetail: (Article) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val paneArticle by viewModel.paneArticle.collectAsStateWithLifecycle()
    val isPaneArticleBookmarked by viewModel.isPaneArticleBookmarked.collectAsStateWithLifecycle()

    HomePaneLayout(
        uiState = uiState,
        paneArticle = paneArticle,
        isPaneArticleBookmarked = isPaneArticleBookmarked,
        onCategorySelected = viewModel::selectCategory,
        onRefresh = viewModel::refresh,
        onArticleClickCompact = onNavigateToDetail,
        onArticleClickPane = viewModel::selectPaneArticle,
        onTogglePaneBookmark = viewModel::togglePaneBookmark,
    )
}
