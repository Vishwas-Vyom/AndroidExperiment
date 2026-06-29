package me.vishwas.androidexperimental

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import me.vishwas.androidexperimental.core.common.ArticleCache
import me.vishwas.androidexperimental.feature.news.domain.model.Article
import me.vishwas.androidexperimental.feature.news.presentation.bookmarks.BookmarksScreen
import me.vishwas.androidexperimental.feature.news.presentation.detail.DetailScreen
import me.vishwas.androidexperimental.feature.news.presentation.home.HomeScreen
import me.vishwas.androidexperimental.feature.news.presentation.search.SearchScreen
import me.vishwas.androidexperimental.navigation.Screen

private data class NavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val navItems = listOf(
    NavItem(Screen.Home, "Home", Icons.Rounded.Home, Icons.Rounded.Home),
    NavItem(Screen.Search, "Search", Icons.Rounded.Search, Icons.Rounded.Search),
    NavItem(Screen.Bookmarks, "Saved", Icons.Rounded.Bookmark, Icons.Rounded.BookmarkBorder),
)

@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute by remember { derivedStateOf { backStackEntry?.destination?.route } }
    val isDetailScreen by remember { derivedStateOf { currentRoute == Screen.Detail.route } }

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val defaultLayoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)

    // On phones the bottom bar hides on the detail screen; on tablets/foldables the
    // nav rail stays visible because it doesn't compete with the content area.
    val layoutType = if (isDetailScreen && defaultLayoutType == NavigationSuiteType.NavigationBar) {
        NavigationSuiteType.None
    } else {
        defaultLayoutType
    }

    NavigationSuiteScaffold(
        modifier = modifier.fillMaxSize(),
        layoutType = layoutType,
        navigationSuiteItems = {
            navItems.forEach { item ->
                val selected = currentRoute == item.screen.route
                item(
                    selected = selected,
                    onClick = {
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                        )
                    },
                    label = { Text(item.label) },
                )
            }
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Screen.Home.route) {
                HomeScreen(onNavigateToDetail = { navigateToDetail(navController, it) })
            }
            composable(Screen.Search.route) {
                SearchScreen(onNavigateToDetail = { navigateToDetail(navController, it) })
            }
            composable(Screen.Bookmarks.route) {
                BookmarksScreen(onNavigateToDetail = { navigateToDetail(navController, it) })
            }
            composable(Screen.Detail.route) {
                DetailScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

private fun navigateToDetail(navController: NavController, article: Article) {
    ArticleCache.selectedArticle = article
    navController.navigate(Screen.Detail.route)
}
