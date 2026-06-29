package me.vishwas.androidexperimental.feature.news.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Article(
    val url: String,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val source: String,
    val author: String?,
    val publishedAt: String,
    val content: String?,
    val category: String = "general",
)
