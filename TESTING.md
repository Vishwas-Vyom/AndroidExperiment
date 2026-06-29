# Testing Guide

This project has three tiers of tests. Each tier has a specific purpose, toolset, and execution environment.

| Tier | Purpose | Needs device? | Command |
|---|---|---|---|
| Unit | ViewModel logic, use cases, domain models | No | `./gradlew test` |
| Snapshot | Composable pixel correctness | No (Robolectric) | `./gradlew test` |
| Instrumented | Compose UI interactions, Room DAO | Yes | `./gradlew connectedAndroidTest` |

---

## 1 — Unit Tests

**Location:** `app/src/test/java/`

**Libraries:** JUnit 4, MockK, Turbine, kotlinx-coroutines-test

Unit tests run on the JVM without an Android device. They cover ViewModels, use cases, and domain model logic.

### Tools

| Library | Purpose |
|---|---|
| [MockK](https://mockk.io/) | Kotlin-first mocking — `mockk()`, `coEvery`, `coVerify` |
| [Turbine](https://github.com/cashapp/turbine) | `Flow` assertion helpers — `test { awaitItem() }` |
| kotlinx-coroutines-test | `runTest`, `UnconfinedTestDispatcher` for eager coroutine execution |
| `MainDispatcherRule` | JUnit rule that replaces `Dispatchers.Main` before each test |

### MainDispatcherRule

Every ViewModel test must include this rule to avoid `IllegalStateException: Module with the Main dispatcher had failed to initialize` and to make coroutines run eagerly inside `runTest`:

```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule()
```

Source: `app/src/test/java/.../util/MainDispatcherRule.kt`

### ViewModel Tests

Each ViewModel test class:
1. Declares MockK mocks for all injected use cases / repositories
2. Sets up a default stub in `@Before` for any flows that the ViewModel subscribes to on init
3. Creates a **fresh ViewModel instance per test** via a `createViewModel()` helper — never reuses a shared instance
4. Asserts on `viewModel.uiState.value` directly (works because `UnconfinedTestDispatcher` runs coroutines synchronously)

**Example — HomeViewModelTest:**

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getTopHeadlinesUseCase: GetTopHeadlinesUseCase = mockk()
    private val newsRepository: NewsRepository = mockk()

    @Before
    fun setup() {
        every { newsRepository.isBookmarked(any()) } returns flowOf(false)
    }

    private fun createViewModel() = HomeViewModel(
        getTopHeadlinesUseCase, getNewsByCategoryUseCase, toggleBookmarkUseCase, newsRepository
    )

    @Test
    fun `initial state transitions from Loading to Success`() = runTest {
        coEvery { getTopHeadlinesUseCase(NoParams) } returns NetworkResult.Success(listOf(testArticle()))

        val state = createViewModel().uiState.value

        assertIs<HomeUiState.Success>(state)
        assertEquals(1, state.articles.size)
    }
}
```

### Use Case Tests

Use cases are thin wrappers over the repository. Their tests verify:
- The correct repository method is called with the correct arguments (`coVerify`)
- The result is passed back unchanged (`assertIs<NetworkResult.Success<*>>`)

No `MainDispatcherRule` is needed here because use case tests do not create a ViewModel or touch `Dispatchers.Main`.

### Test Fixtures

**Source:** `app/src/test/java/.../util/TestFixtures.kt`

Factory helpers that build domain model instances with sensible defaults. Pass only the fields relevant to the scenario being tested:

```kotlin
// Full article with all fields
fun testArticle(
    url: String = "https://example.com/article",
    title: String = "Test Article Title",
    imageUrl: String? = "https://example.com/image.jpg",
    category: String = "general",
    // ... other fields
): Article

// Shortcut for articles with no image
fun testArticleWithoutImage(url: String = "https://example.com/no-image"): Article
```

Both the unit test source set and the instrumented test source set have a `TestFixtures.kt`. The `testBookmarkEntity()` helper in the instrumented source set mirrors the same pattern for `BookmarkEntity`.

### Running Unit Tests

```bash
# All unit tests
./gradlew test

# Single test class
./gradlew test --tests "me.vishwas.androidexperimental.feature.news.presentation.home.HomeViewModelTest"

# With continuous mode (re-runs on file change)
./gradlew test --continuous
```

---

## 2 — Snapshot Tests

**Location:** `app/src/test/java/.../snapshot/`

**Libraries:** Roborazzi 1.56.0, Robolectric 4.13

Snapshot tests render Compose composables on the JVM using Robolectric (no device needed) and compare the output pixel-by-pixel against a stored golden PNG. They catch unintended visual regressions.

### How They Work

1. On the **first run** (or after running `recordRoborazziDebug`), Roborazzi saves a PNG golden image.
2. On **subsequent runs**, Roborazzi renders the composable again and diffs it against the stored golden. Any pixel difference fails the test.

### Annotations

Every snapshot test class requires two annotations:

```kotlin
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)  // hardware-accelerated rendering via Robolectric
@Config(sdk = [33])                      // matches robolectric.properties
class HomeComponentsSnapshotTest { ... }
```

`robolectric.properties` at `app/src/test/resources/robolectric.properties` pins the SDK globally:

```properties
sdk=33
```

### Capturing a Snapshot

```kotlin
@Test
fun newsArticleCard_withImage() {
    composeTestRule.setContent {
        AndroidExperimentalTheme {
            NewsArticleCard(article = testArticle(), onClick = {})
        }
    }
    composeTestRule.onRoot().captureRoboImage()
}
```

Always wrap composables in `AndroidExperimentalTheme` so goldens include the correct colors and typography.

### Network Images

Coil does not load network images in a Robolectric environment. Pass `imageUrl = null` for snapshot tests — this also covers the "no image" placeholder state which is a valid layout case.

### Golden Image Location

```
app/src/test/snapshots/images/
```

Commit golden images to source control. They are the source of truth for visual correctness and must be updated intentionally (see below).

### Gradle Tasks

| Task | Description |
|---|---|
| `./gradlew test` | Verifies composables against existing goldens; fails on any pixel diff |
| `./gradlew recordRoborazziDebug` | Re-renders all composables and overwrites the goldens |
| `./gradlew verifyRoborazziDebug` | Explicit verify — same as running the tests, no recording |
| `./gradlew compareRoborazziDebug` | Generates a diff image without failing the build |

### Updating Goldens

After an intentional UI change, record new goldens:

```bash
./gradlew recordRoborazziDebug
```

Review the updated PNGs in `app/src/test/snapshots/images/`, commit them, and include them in the PR so reviewers can inspect the visual change.

### Existing Snapshot Test Files

| File | What it covers |
|---|---|
| `HomeComponentsSnapshotTest` | `NewsArticleCard`, `FeaturedArticleCard`, `CategoryChipsRow`, `SectionHeader`, `SourceBadge`, `NewsLoadingIndicator`, `ErrorState` |
| `SearchContentSnapshotTest` | `SearchContent` composable in loading/empty/error/results states |
| `BookmarksContentSnapshotTest` | `BookmarksContent` composable in empty and populated states |
| `DetailContentSnapshotTest` | `DetailContent` composable with full and minimal article data |

---

## 3 — Instrumented Tests

**Location:** `app/src/androidTest/java/`

**Libraries:** Compose UI Test (junit4), Espresso, Turbine, Room testing

Instrumented tests run on a real Android device or emulator and interact with the rendered UI through the Compose semantic tree.

### Compose UI Tests

UI tests use `createComposeRule()` and interact exclusively through semantics — text, roles, selection state, progress indicators:

```kotlin
@RunWith(AndroidJUnit4::class)
class HomeContentUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun errorState_retry_button_invokes_onRefresh_callback() {
        var refreshCalled = false
        composeTestRule.setContent {
            AndroidExperimentalTheme {
                HomeContent(
                    uiState = HomeUiState.Error("Error"),
                    onRefresh = { refreshCalled = true },
                    // ...
                )
            }
        }

        composeTestRule.onNodeWithText("Try Again").performClick()

        assert(refreshCalled)
    }
}
```

UI tests target the **stateless `*Content` composable**, not the stateful `*Screen` composable. This keeps the ViewModel out of the test and lets you supply any `UiState` directly.

### What Is Tested

| Class | Scenarios covered |
|---|---|
| `HomeContentUiTest` | Loading spinner, error message + retry click, category chip selection, article list display, article click callback |
| `SearchContentUiTest` | Search bar interaction, results list, empty state |
| `BookmarksContentUiTest` | Empty bookmark state, populated list, remove action |
| `DetailContentUiTest` | Article title/content display, bookmark toggle button |

### Room DAO Tests

**Class:** `BookmarkDaoTest`

Rule: **never mock the database**. Every DAO test uses a real in-memory Room instance:

```kotlin
@Before
fun setup() {
    database = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        AppDatabase::class.java,
    ).allowMainThreadQueries().build()
    dao = database.bookmarkDao()
}

@After
fun teardown() {
    database.close()
}
```

A fresh database is created before each test and closed afterwards — no shared state between tests.

**Scenarios covered:**

- Insert and retrieve a bookmark
- `OnConflictStrategy.REPLACE` — re-inserting the same URL overwrites the row
- `getBookmarks()` returns results ordered by `publishedAt` descending (newest first)
- `isBookmarked()` returns true/false and updates reactively after deletion
- `delete()` is idempotent for unknown URLs

### Running Instrumented Tests

```bash
# All instrumented tests (requires connected device or running emulator)
./gradlew connectedAndroidTest

# Single test class
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=me.vishwas.androidexperimental.core.database.BookmarkDaoTest
```

---

## Project Testing Rules

1. **Never mock the database** in instrumented tests — always use a real in-memory Room instance
2. **Never create a shared ViewModel** across unit tests — use a `createViewModel()` factory to get a fresh instance per test
3. **Never preview the stateful `*Screen` composable** — snapshot and UI tests target stateless `*Content` composables
4. **Always wrap composables in `AndroidExperimentalTheme`** in both snapshot and UI tests
5. **Test files are off-limits during code cleanup** — the after-task cleanup pass must not modify test files
6. **Preview composables (`@Preview`) are not tests** — they live in the same file as the composable, not under `src/test/`
