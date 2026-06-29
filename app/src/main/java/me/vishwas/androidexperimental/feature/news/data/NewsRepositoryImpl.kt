package me.vishwas.androidexperimental.feature.news.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.vishwas.androidexperimental.core.common.Constants
import me.vishwas.androidexperimental.core.database.dao.BookmarkDao
import me.vishwas.androidexperimental.core.database.entity.BookmarkEntity
import me.vishwas.androidexperimental.core.network.NetworkResult
import me.vishwas.androidexperimental.feature.news.data.dto.ArticleDto
import me.vishwas.androidexperimental.feature.news.data.dto.ArticlesRequestDto
import me.vishwas.androidexperimental.feature.news.data.remote.NewsApiService
import me.vishwas.androidexperimental.feature.news.domain.model.Article
import me.vishwas.androidexperimental.feature.news.domain.repository.NewsRepository
import javax.inject.Inject

private const val TAG = "NewsRepository"

class NewsRepositoryImpl @Inject constructor(
    private val apiService: NewsApiService,
    private val bookmarkDao: BookmarkDao,
) : NewsRepository {

    override suspend fun getTopHeadlines(country: String): NetworkResult<List<Article>> {
        Log.d(TAG, "getTopHeadlines")
        return safeApiCall(
            ArticlesRequestDto(
                apiKey = Constants.NEWS_API_KEY,
                articlesCount = Constants.PAGE_SIZE,
                lang = Constants.DEFAULT_LANGUAGE,
            )
        )
    }

    override suspend fun getNewsByCategory(category: String, country: String): NetworkResult<List<Article>> {
        Log.d(TAG, "getNewsByCategory → category=$category")
        return safeApiCall(
            ArticlesRequestDto(
                apiKey = Constants.NEWS_API_KEY,
                articlesCount = Constants.PAGE_SIZE,
                lang = Constants.DEFAULT_LANGUAGE,
                categoryUri = category.toEventRegistryCategoryUri(),
            )
        )
    }

    override suspend fun searchNews(query: String): NetworkResult<List<Article>> {
        Log.d(TAG, "searchNews → query=$query")
        return safeApiCall(
            ArticlesRequestDto(
                apiKey = Constants.NEWS_API_KEY,
                articlesCount = Constants.PAGE_SIZE,
                lang = Constants.DEFAULT_LANGUAGE,
                keyword = query,
            )
        )
    }

    override fun getBookmarks(): Flow<List<Article>> =
        bookmarkDao.getBookmarks().map { list -> list.map { it.toArticle() } }

    override fun isBookmarked(url: String): Flow<Boolean> =
        bookmarkDao.isBookmarked(url)

    override suspend fun toggleBookmark(article: Article) {
        if (bookmarkDao.countBookmarked(article.url) > 0) {
            bookmarkDao.delete(article.url)
        } else {
            bookmarkDao.insert(article.toBookmarkEntity())
        }
    }

    private suspend fun safeApiCall(request: ArticlesRequestDto): NetworkResult<List<Article>> =
        try {
            val response = apiService.getArticles(request)
            val body = response.body()
            Log.d(TAG, "Response: code=${response.code()} error=${body?.error} info=${body?.info}")
            when {
                body?.error != null -> {
                    Log.e(TAG, "API error ${body.error}: ${body.info}")
                    NetworkResult.Error(body.info ?: "API error ${body.error}")
                }
                response.isSuccessful && body?.articles != null -> {
                    val articles = body.articles.results
                        ?.filter { it.title != null && it.url != null }
                        ?.map { it.toArticle() }
                        ?: emptyList()
                    Log.d(TAG, "Success — ${articles.size} articles returned")
                    NetworkResult.Success(articles)
                }
                else -> {
                    Log.e(TAG, "Unexpected response: code=${response.code()}")
                    NetworkResult.Error("Error ${response.code()}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception: ${e.javaClass.simpleName} — ${e.localizedMessage}", e)
            NetworkResult.Error(e.localizedMessage ?: "Network error. Check your connection.")
        }
}

private fun String.toEventRegistryCategoryUri(): String? = when (this) {
    "general" -> null
    "business" -> "dmoz/Business"
    "technology" -> "dmoz/Computers"
    "sports" -> "dmoz/Sports"
    "entertainment" -> "dmoz/Arts"
    "health" -> "dmoz/Health"
    "science" -> "dmoz/Science"
    else -> null
}

private fun ArticleDto.toArticle() = Article(
    url = url ?: uri ?: "",
    title = title ?: "",
    description = body?.take(200)?.takeIf { it.isNotBlank() },
    imageUrl = image?.takeIf { it.isNotBlank() },
    source = source?.title ?: source?.uri ?: "Unknown",
    author = authors?.firstOrNull()?.name?.takeIf { it.isNotBlank() },
    publishedAt = dateTimePub ?: dateTime ?: "",
    content = body?.takeIf { it.isNotBlank() },
)

private fun BookmarkEntity.toArticle() = Article(
    url = url, title = title, description = description,
    imageUrl = imageUrl, source = source, author = author,
    publishedAt = publishedAt, content = content, category = category,
)

private fun Article.toBookmarkEntity() = BookmarkEntity(
    url = url, title = title, description = description,
    imageUrl = imageUrl, source = source, author = author,
    publishedAt = publishedAt, content = content, category = category,
)
