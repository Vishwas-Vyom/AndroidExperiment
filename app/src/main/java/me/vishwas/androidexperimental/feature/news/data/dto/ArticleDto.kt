package me.vishwas.androidexperimental.feature.news.data.dto

import com.google.gson.annotations.SerializedName

data class ArticleDto(
    @SerializedName("uri") val uri: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("body") val body: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("dateTime") val dateTime: String?,
    @SerializedName("dateTimePub") val dateTimePub: String?,
    @SerializedName("source") val source: SourceDto?,
    @SerializedName("authors") val authors: List<AuthorDto>?,
)

data class SourceDto(
    @SerializedName("uri") val uri: String?,
    @SerializedName("title") val title: String?,
)

data class AuthorDto(
    @SerializedName("uri") val uri: String?,
    @SerializedName("name") val name: String?,
)
