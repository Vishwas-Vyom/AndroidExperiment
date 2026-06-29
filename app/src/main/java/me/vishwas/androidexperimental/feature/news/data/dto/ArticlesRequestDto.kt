package me.vishwas.androidexperimental.feature.news.data.dto

import com.google.gson.annotations.SerializedName

data class ArticlesRequestDto(
    @SerializedName("apiKey") val apiKey: String,
    @SerializedName("articlesPage") val articlesPage: Int = 1,
    @SerializedName("articlesCount") val articlesCount: Int = 20,
    @SerializedName("articlesSortBy") val articlesSortBy: String = "date",
    @SerializedName("articlesSortByAsc") val articlesSortByAsc: Boolean = false,
    @SerializedName("lang") val lang: String = "eng",
    @SerializedName("dataType") val dataType: List<String> = listOf("news"),
    @SerializedName("categoryUri") val categoryUri: String? = null,
    @SerializedName("keyword") val keyword: String? = null,
    @SerializedName("includeArticleBody") val includeArticleBody: Boolean = true,
    @SerializedName("includeArticleImage") val includeArticleImage: Boolean = true,
    @SerializedName("includeArticleAuthor") val includeArticleAuthor: Boolean = true,
    @SerializedName("includeArticleConcepts") val includeArticleConcepts: Boolean = false,
    @SerializedName("includeArticleCategories") val includeArticleCategories: Boolean = false,
)
