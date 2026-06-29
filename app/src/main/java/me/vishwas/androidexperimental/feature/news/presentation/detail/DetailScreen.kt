package me.vishwas.androidexperimental.feature.news.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val isBookmarked by viewModel.isBookmarked.collectAsStateWithLifecycle()
    val article = viewModel.article

    if (article == null) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    DetailContent(
        article = article,
        isBookmarked = isBookmarked,
        onNavigateBack = onNavigateBack,
        onToggleBookmark = viewModel::toggleBookmark,
    )
}
