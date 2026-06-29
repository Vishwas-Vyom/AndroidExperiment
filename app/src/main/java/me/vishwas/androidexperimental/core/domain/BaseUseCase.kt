package me.vishwas.androidexperimental.core.domain

abstract class BaseUseCase<in P, out R> {
    abstract suspend operator fun invoke(params: P): R
}

object NoParams
