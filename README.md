# Android Experimental

A modern Android app built entirely with Jetpack Compose and Material 3. The project serves two purposes at once:

1. An **architectural reference** — a News feature built with Clean Architecture, Hilt, Retrofit, Room, and adaptive layouts that work across phones, foldables, and tablets.
2. A **Compose/Android concepts sandbox** — a Notes feature whose code comments walk through specific Compose runtime behavior (recomposition scope, state phases, side effects, one-off UI events) as numbered "Topics" built up incrementally.

---

## Features

### News (`feature/news`)

- **News Feed** — Top headlines grouped by category (General, Technology, Business, Sports, Entertainment, Science, Health) with pull-to-refresh
- **Featured Article Card** — The first article with an image is promoted to a large hero card
- **Category Filtering** — Horizontally scrollable chip row; switching categories fetches a fresh batch from the API
- **Search** — Full-text search across all news articles
- **Bookmarks** — Save articles to a local Room database; bookmarks persist across sessions
- **Article Detail** — Full-body article view with bookmark toggle
- **Adaptive Layout** — Two-pane list/detail on tablets and unfolded foldables; single-pane navigation on phones

**What you'll learn:** how to structure a full Clean Architecture feature end-to-end — Retrofit + DTOs → Repository → UseCase → `StateFlow<UiState>` ViewModel → stateless Compose screen — and how to make a list/detail flow adaptive with `ListDetailPaneScaffold` instead of hand-rolling window-size breakpoints.

### Notes (`feature/notes`)

- **Create & List** — A FAB opens an inline compose form (title + content); saved notes persist to Room and appear at the top of the list
- **Live Search** — Filtering the list re-runs reactively via a `combine()` of the notes `Flow` and the search query
- **Adaptive List/Detail** — Same `ListDetailPaneScaffold` pattern as News: two-pane on tablets/unfolded foldables, single-pane push-navigation on phones
- **One-off UI Events** — Save success/failure surfaces as a Snackbar via a `SharedFlow` of `NotesUiEvent`, collected once at the screen root
- Doubles as a live demonstration of `rememberSaveable` drafts, `DisposableEffect`/`SideEffect` analytics hooks, and `WhileSubscribed(5_000)` flow sharing (see [Concepts Demonstrated](#concepts-demonstrated) below)

**What you'll learn:** where each kind of state should actually live — ViewModel vs. `rememberSaveable` vs. plain `remember` — and how to model a "did the save succeed" one-off event with `SharedFlow` instead of leaking that decision into the form composable. Full breakdown in [Concepts Demonstrated](#concepts-demonstrated).

---

## Tech Stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose |
| Adaptive layouts | `material3-adaptive-navigation-suite`, `adaptive-layout`, `adaptive-navigation`, `androidx.window` |
| State management | `StateFlow` + `UiState` sealed class per screen |
| Dependency injection | Hilt |
| Networking | Retrofit 2 + OkHttp + Gson |
| Image loading | Coil 3 |
| Local database | Room |
| Collections | `kotlinx-collections-immutable` |
| Language | Kotlin 2.2, Coroutines, KSP |
| Build | AGP 9.2.1, Gradle 9.4.1 |

---

## Architecture

The project follows **Clean Architecture** with a strict separation of concerns across three layers inside each feature:

```
presentation  →  domain  →  data
```

- **`presentation/`** — Compose screens, ViewModels, UiState. ViewModels expose `StateFlow<UiState>` and accept events via plain functions. All UI is stateless except the root `*Screen` composable.
- **`domain/`** — Use cases, repository interfaces, domain models. Business logic lives exclusively here.
- **`data/`** — `RepositoryImpl`, Retrofit service, Room DAOs, DTOs. No business logic.

Hilt wires everything together. Feature-specific modules live in `feature/{name}/di/`; app-wide dependencies (Retrofit, Room) live in `core/di/`.

### Package Structure

```
me.vishwas.androidexperimental/
├── core/
│   ├── common/       # Extensions, utilities, ArticleCache, NoteCache
│   ├── database/     # AppDatabase, dao/ (BookmarkDao, NoteDao), entity/ (BookmarkEntity, NoteEntity)
│   ├── di/           # NetworkModule, DatabaseModule
│   ├── domain/       # BaseUseCase, NoParams
│   └── network/      # NetworkResult sealed class
│
├── feature/
│   ├── news/
│   │   ├── data/         # NewsRepositoryImpl, NewsApiService, DTOs
│   │   ├── di/           # NewsModule
│   │   ├── domain/       # UseCases, NewsRepository interface, Article model
│   │   └── presentation/
│   │       ├── home/     # HomeScreen, HomeContent, HomeComponents, HomeViewModel
│   │       ├── search/   # SearchScreen, SearchContent, SearchViewModel
│   │       ├── bookmarks/# BookmarksScreen, BookmarksContent, BookmarksViewModel
│   │       └── detail/   # DetailScreen, DetailContent, DetailViewModel
│   │
│   └── notes/
│       ├── data/         # NotesRepositoryImpl
│       ├── di/           # NotesModule
│       ├── domain/       # AddNoteUseCase, GetNotesUseCase, NotesRepository interface, Note model
│       └── presentation/ # NotesListScreen/Content, NoteComposeForm, NoteListItem, NoteDetailScreen/Content,
│                         # NotesPaneLayout, NotesEmptyState, NotesAnalytics, NotesViewModel
│
├── ui/theme/         # Material 3 theme, custom plum+amber palette
├── App.kt            # @HiltAndroidApp
├── AppNavHost.kt     # Root nav graph + NavigationSuiteScaffold
└── MainActivity.kt   # Single activity, edge-to-edge
```

### Adaptive Navigation

`NavigationSuiteScaffold` in `AppNavHost.kt` automatically switches navigation chrome based on window size:

| Window size | Navigation |
|---|---|
| Compact < 600 dp | Bottom bar |
| Medium 600–840 dp | Navigation rail |
| Expanded > 840 dp | Permanent navigation drawer |

Both the Home (News) and Notes features use `ListDetailPaneScaffold` to display detail content inline on large screens.

---

## App Flow

`AppNavHost.kt` hosts a single `NavHost` with four top-level destinations plus two push-only detail destinations, wrapped in one `NavigationSuiteScaffold`:

```
Home ──┐
Search ─┼── NavigationSuiteScaffold (bottom bar / rail / drawer, by window size)
Bookmarks│
Notes ──┘
  │
  ├─ Home/Search/Bookmarks → Detail        (Screen.Detail)
  └─ Notes                 → Note Detail   (Screen.NoteDetail)
```

- **Tab switches** (`Home`, `Search`, `Bookmarks`, `Notes`) use `popUpTo(startDestination) { saveState = true }` + `restoreState = true`, so each tab keeps its own scroll position and `ViewModel` state when you switch away and back.
- **List → Detail** does not pass data through the nav route. `NavController.navigate()` only carries a `String` route, so the selected item is stashed in a plain in-memory object (`ArticleCache.selectedArticle` / `NoteCache.selectedNote`) immediately before navigating, then read back by the destination composable. Both `DetailScreen` and `NoteDetailScreen` guard against a `null` cached value (e.g. after process death) by navigating back immediately instead of crashing.
- **On wide screens** (tablet, unfolded foldable), Home and Notes skip the push-navigation route entirely — `ListDetailPaneScaffoldNavigator` swaps the detail pane in place next to the list, and the bottom bar/rail never has to hide. On phones, the detail screen replaces the nav chrome (`NavigationSuiteType.None`) so the article/note gets the full screen.
- **Hilt** wires the graph: `App.kt` is the `@HiltAndroidApp` entry point, `MainActivity` is a single `@AndroidEntryPoint` activity, and every screen composable obtains its `@HiltViewModel` via `hiltViewModel()` at the point it's declared in `AppNavHost`.

---

## Concepts Demonstrated

The Notes feature was built incrementally as a teaching sequence — code comments reference specific numbered "Topics" (e.g. `Topic 8`, `Topic 10`, `Topic 13`, `Topic 20`, `Topic 24`). Each row below is a distinct thing to learn, anchored to the file that teaches it:

| Concept — what you'll learn | Where | How it's shown |
|---|---|---|
| ViewModel-owned state vs. `rememberSaveable` | `NotesViewModel.kt` | Search query lives in the ViewModel (survives rotation for free) instead of Compose `rememberSaveable` state, with the tradeoff (no process-death survival without `SavedStateHandle`) called out explicitly |
| Combining multiple flows into one `UiState` | `NotesViewModel.kt` | `combine(getNotesUseCase(), _query) { ... }` re-runs on either upstream emission, producing one downstream update instead of two to reconcile |
| `SharingStarted.WhileSubscribed(5_000)` | `NotesViewModel.kt` | Keeps the underlying Room `Flow` alive for 5s after the last collector disappears, so a config change doesn't restart the DB query from scratch |
| One-off UI events via `SharedFlow` | `NotesViewModel.kt`, `NoteComposeForm.kt`, `NotesListScreen.kt` | Save success/failure is modeled as a `SharedFlow<NotesUiEvent>`, collected once at the screen root via `LaunchedEffect` + `collectLatest`, to drive a Snackbar without the form knowing whether the save actually succeeded |
| `rememberSaveable` for in-progress drafts | `NoteComposeForm.kt` | Title/content survive a rotation mid-typing; `remember` alone would silently wipe an unsaved draft |
| `DisposableEffect` / `SideEffect` | `NotesListScreen.kt` | `DisposableEffect` fires an analytics "screen viewed"/"screen left" pair tied to the composable's lifetime; `SideEffect` syncs a note-count breadcrumb after every successful recomposition |
| Stable collections for skippability | `NotesListModels.kt`, `NotesListContent.kt` | `ImmutableList`/`persistentListOf` from `kotlinx-collections-immutable` so list params are stable and `remember` keys can compare correctly (see the `remember(notes.size, query)` comment on why `notes` itself can't be the key) |
| Object hand-off across `NavController.navigate()` | `ArticleCache.kt`, `NoteCache.kt` | Since a nav route is just a `String`, the selected item is stashed in a plain object before navigating and read back by the destination — same pattern used for both News and Notes |
| Two-pane vs. single-pane navigation in one composable | `NotesPaneLayout.kt`, `HomePaneLayout.kt` | `ListDetailPaneScaffoldNavigator.scaffoldDirective.maxHorizontalPartitions` decides, per click, whether to update an adjacent pane in place or push a real `NavController` destination |

---

## Getting Started

### Prerequisites

- Android Studio Meerkat or newer
- JDK 11 (bundled with Android Studio)
- Android SDK 37

### Clone & Open

```bash
git clone https://github.com/Vishwas-Vyom/AndroidExperiment.git
cd AndroidExperiment
```

Open the project root in Android Studio. Gradle sync runs automatically.

### Build & Run

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

> On Windows use `gradlew.bat` or run `./gradlew` from Git Bash.

### Lint

```bash
./gradlew lint
```

---

## Testing

The project has three tiers of tests — unit, snapshot, and instrumented. See [TESTING.md](TESTING.md) for the full guide including how to record and verify snapshot golden images.

```bash
# Unit + snapshot tests (no device needed)
./gradlew test

# Instrumented tests (requires connected device or emulator)
./gradlew connectedAndroidTest
```

---

## Versions

Managed via `gradle/libs.versions.toml`:

| Tool | Version |
|---|---|
| Kotlin | 2.2.10 |
| AGP | 9.2.1 |
| Compose BOM | 2026.02.01 |
| Hilt | 2.59 |
| Room | 2.7.1 |
| Roborazzi | 1.56.0 |
