package me.vishwas.androidexperimental.util

import me.vishwas.androidexperimental.feature.news.domain.model.Article

/**
 * Factory helpers that build test instances of domain models with sensible defaults.
 * Override individual fields to set up specific scenarios.
 */
fun testArticle(
    url: String = "https://example.com/article",
    title: String = "Test Article Title",
    description: String? = "Short description of the article.",
    imageUrl: String? = "https://example.com/image.jpg",
    source: String = "Test Source",
    author: String? = "Jane Doe",
    publishedAt: String = "2024-06-15T12:00:00Z",
    content: String? = "Full article content goes here.",
    category: String = "general",
): Article = Article(
    url = url,
    title = title,
    description = description,
    imageUrl = imageUrl,
    source = source,
    author = author,
    publishedAt = publishedAt,
    content = content,
    category = category,
)

fun testArticleWithoutImage(url: String = "https://example.com/no-image") = testArticle(
    url = url,
    imageUrl = null,
)
