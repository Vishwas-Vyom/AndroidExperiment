package me.vishwas.androidexperimental.feature.news.presentation.home

import androidx.activity.compose.BackHandler
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import me.vishwas.androidexperimental.feature.news.domain.model.Article
import me.vishwas.androidexperimental.feature.news.domain.model.NewsCategory

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun HomePaneLayout(
    uiState: HomeUiState,
    paneArticle: Article?,
    isPaneArticleBookmarked: Boolean,
    onCategorySelected: (NewsCategory) -> Unit,
    onRefresh: () -> Unit,
    onArticleClickCompact: (Article) -> Unit,
    onArticleClickPane: (Article) -> Unit,
    onTogglePaneBookmark: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()

    BackHandler(navigator.canNavigateBack()) {
        scope.launch { navigator.navigateBack() }
    }

    ListDetailPaneScaffold(
        modifier = modifier,
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                HomeContent(
                    uiState = uiState,
                    categories = NewsCategory.entries.toList(),
                    onCategorySelected = onCategorySelected,
                    onRefresh = onRefresh,
                    onArticleClick = { article ->
                        if (navigator.scaffoldDirective.maxHorizontalPartitions > 1) {
                            onArticleClickPane(article)
                            scope.launch {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    contentKey = article.url ?: "",
                                )
                            }
                        } else {
                            onArticleClickCompact(article)
                        }
                    },
                )
            }
        },
        detailPane = {
            AnimatedPane {
                if (paneArticle != null) {
                    ArticleDetailPanel(
                        article = paneArticle,
                        isBookmarked = isPaneArticleBookmarked,
                        onToggleBookmark = onTogglePaneBookmark,
                        onClose = { scope.launch { navigator.navigateBack() } },
                    )
                } else {
                    DetailPanePlaceholder()
                }
            }
        },
    )
}
