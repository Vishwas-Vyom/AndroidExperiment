package me.vishwas.androidexperimental.feature.news.domain.usecase

import me.vishwas.androidexperimental.feature.news.domain.model.Article
import me.vishwas.androidexperimental.feature.news.domain.repository.NewsRepository
import javax.inject.Inject

class ToggleBookmarkUseCase @Inject constructor(
    private val repository: NewsRepository,
) {
    suspend operator fun invoke(article: Article) = repository.toggleBookmark(article)
}
