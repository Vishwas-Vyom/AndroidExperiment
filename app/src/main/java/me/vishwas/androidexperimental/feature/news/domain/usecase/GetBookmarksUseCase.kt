package me.vishwas.androidexperimental.feature.news.domain.usecase

import kotlinx.coroutines.flow.Flow
import me.vishwas.androidexperimental.feature.news.domain.model.Article
import me.vishwas.androidexperimental.feature.news.domain.repository.NewsRepository
import javax.inject.Inject

class GetBookmarksUseCase @Inject constructor(
    private val repository: NewsRepository,
) {
    operator fun invoke(): Flow<List<Article>> = repository.getBookmarks()
}
