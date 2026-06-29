package me.vishwas.androidexperimental.feature.news.domain.usecase

import me.vishwas.androidexperimental.core.domain.BaseUseCase
import me.vishwas.androidexperimental.core.domain.NoParams
import me.vishwas.androidexperimental.core.network.NetworkResult
import me.vishwas.androidexperimental.feature.news.domain.model.Article
import me.vishwas.androidexperimental.feature.news.domain.repository.NewsRepository
import javax.inject.Inject

class GetTopHeadlinesUseCase @Inject constructor(
    private val repository: NewsRepository,
) : BaseUseCase<NoParams, NetworkResult<List<Article>>>() {
    override suspend operator fun invoke(params: NoParams): NetworkResult<List<Article>> =
        repository.getTopHeadlines("")
}
