package me.vishwas.androidexperimental.util

import me.vishwas.androidexperimental.core.database.entity.BookmarkEntity
import me.vishwas.androidexperimental.feature.news.domain.model.Article

fun testArticle(
    url: String = "https://example.com/article",
    title: String = "Test Article Title",
    description: String? = "Short description.",
    imageUrl: String? = null,
    source: String = "Test Source",
    author: String? = "Jane Doe",
    publishedAt: String = "2024-06-15T12:00:00Z",
    content: String? = "Full article content.",
    category: String = "general",
): Article = Article(url, title, description, imageUrl, source, author, publishedAt, content, category)

fun testArticleWithoutImage(url: String = "https://example.com/article") =
    testArticle(url = url, imageUrl = null)

fun testBookmarkEntity(
    url: String = "https://example.com/article",
    title: String = "Test Article Title",
    description: String? = "Short description.",
    imageUrl: String? = null,
    source: String = "Test Source",
    author: String? = "Jane Doe",
    publishedAt: String = "2024-06-15T12:00:00Z",
    content: String? = "Full article content.",
    category: String = "general",
): BookmarkEntity = BookmarkEntity(url, title, description, imageUrl, source, author, publishedAt, content, category)
