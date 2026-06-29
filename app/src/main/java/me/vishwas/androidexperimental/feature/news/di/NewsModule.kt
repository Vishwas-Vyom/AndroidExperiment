package me.vishwas.androidexperimental.feature.news.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.vishwas.androidexperimental.core.database.dao.BookmarkDao
import me.vishwas.androidexperimental.feature.news.data.NewsRepositoryImpl
import me.vishwas.androidexperimental.feature.news.data.remote.NewsApiService
import me.vishwas.androidexperimental.feature.news.domain.repository.NewsRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NewsModule {

    @Provides
    @Singleton
    fun provideNewsApiService(retrofit: Retrofit): NewsApiService =
        retrofit.create(NewsApiService::class.java)

    @Provides
    @Singleton
    fun provideNewsRepository(
        apiService: NewsApiService,
        bookmarkDao: BookmarkDao,
    ): NewsRepository = NewsRepositoryImpl(apiService, bookmarkDao)
}
