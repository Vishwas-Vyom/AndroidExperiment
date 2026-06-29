@file:OptIn(ExperimentalMaterial3Api::class)

package me.vishwas.androidexperimental.feature.news.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import me.vishwas.androidexperimental.feature.news.domain.model.Article
import me.vishwas.androidexperimental.feature.news.domain.model.NewsCategory

@Composable
fun HomeContent(
    uiState: HomeUiState,
    categories: List<NewsCategory>,
    onCategorySelected: (NewsCategory) -> Unit,
    onRefresh: () -> Unit,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val isRefreshing = (uiState as? HomeUiState.Success)?.isRefreshing == true

    androidx.compose.material3.Scaffold(
        topBar = { NewsTopBar(scrollBehavior = scrollBehavior) },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
        ) {
            when (uiState) {
                HomeUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { NewsLoadingIndicator() }
                is HomeUiState.Error -> ErrorState(uiState.message, onRefresh, Modifier.fillMaxSize())
                is HomeUiState.Success -> HomeNewsList(uiState, categories, onCategorySelected, onArticleClick)
            }
        }
    }
}

@Composable
private fun HomeNewsList(
    uiState: HomeUiState.Success,
    categories: List<NewsCategory>,
    onCategorySelected: (NewsCategory) -> Unit,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item(key = "categories") {
                CategoryChipsRow(
                    categories = categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = onCategorySelected,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
            uiState.featuredArticle?.let { article ->
                item(key = "featured") {
                    FeaturedArticleCard(
                        article = article,
                        onClick = { onArticleClick(article) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
            if (uiState.regularArticles.isNotEmpty()) {
                item(key = "header") {
                    SectionHeader("Latest News", Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                }
                items(uiState.regularArticles, key = { it.url }) { article ->
                    NewsArticleCard(
                        article = article,
                        onClick = { onArticleClick(article) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}
