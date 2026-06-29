package me.vishwas.androidexperimental.feature.news.presentation.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.vishwas.androidexperimental.feature.news.domain.model.Article

@Composable
fun SearchScreen(
    onNavigateToDetail: (Article) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }

    SearchContent(
        uiState = uiState,
        query = query,
        onQueryChange = { query = it; viewModel.search(it) },
        onClear = { query = ""; viewModel.clearSearch() },
        onArticleClick = onNavigateToDetail,
    )
}
