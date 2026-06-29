package me.vishwas.androidexperimental.feature.news.presentation.bookmarks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.vishwas.androidexperimental.feature.news.domain.model.Article

@Composable
fun BookmarksScreen(
    onNavigateToDetail: (Article) -> Unit,
    viewModel: BookmarksViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BookmarksContent(
        uiState = uiState,
        onArticleClick = onNavigateToDetail,
        onRemoveBookmark = viewModel::removeBookmark,
    )
}
