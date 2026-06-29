package me.vishwas.androidexperimental.feature.news.domain.repository

import kotlinx.coroutines.flow.Flow
import me.vishwas.androidexperimental.core.network.NetworkResult
import me.vishwas.androidexperimental.feature.news.domain.model.Article

interface NewsRepository {
    suspend fun getTopHeadlines(country: String): NetworkResult<List<Article>>
    suspend fun getNewsByCategory(category: String, country: String): NetworkResult<List<Article>>
    suspend fun searchNews(query: String): NetworkResult<List<Article>>
    fun getBookmarks(): Flow<List<Article>>
    fun isBookmarked(url: String): Flow<Boolean>
    suspend fun toggleBookmark(article: Article)
}
