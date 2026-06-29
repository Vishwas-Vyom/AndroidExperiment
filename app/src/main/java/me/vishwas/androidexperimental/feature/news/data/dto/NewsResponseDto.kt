package me.vishwas.androidexperimental.feature.news.data.dto

import com.google.gson.annotations.SerializedName

data class NewsResponseDto(
    @SerializedName("articles") val articles: ArticlesPageDto?,
    @SerializedName("error") val error: Int?,
    @SerializedName("info") val info: String?,
)

data class ArticlesPageDto(
    @SerializedName("results") val results: List<ArticleDto>?,
    @SerializedName("totalResults") val totalResults: Int?,
    @SerializedName("page") val page: Int?,
    @SerializedName("count") val count: Int?,
)
