package me.vishwas.androidexperimental.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit rule that replaces [Dispatchers.Main] with a [TestDispatcher] for the duration of each
 * test. Any ViewModel coroutines that run on [Dispatchers.Main] (via viewModelScope) will use
 * [UnconfinedTestDispatcher] by default, which executes them eagerly in the test thread.
 *
 * Usage:
 * ```
 * @get:Rule val mainDispatcherRule = MainDispatcherRule()
 * ```
 *
 * To control timing precisely (e.g., for debounce tests), pass a [StandardTestDispatcher]:
 * ```
 * private val dispatcher = StandardTestDispatcher()
 * @get:Rule val mainDispatcherRule = MainDispatcherRule(dispatcher)
 * // then use advanceTimeBy / advanceUntilIdle in tests
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
