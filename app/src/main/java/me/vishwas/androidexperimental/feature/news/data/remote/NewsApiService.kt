package me.vishwas.androidexperimental.feature.news.data.remote

import me.vishwas.androidexperimental.feature.news.data.dto.ArticlesRequestDto
import me.vishwas.androidexperimental.feature.news.data.dto.NewsResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NewsApiService {

    @POST("article/getArticles")
    suspend fun getArticles(@Body request: ArticlesRequestDto): Response<NewsResponseDto>
}
