# Android Experimental

A modern Android news app built entirely with Jetpack Compose and Material 3. The project serves as an architectural reference for production-grade Android apps using the latest Jetpack libraries, Clean Architecture, and adaptive layouts that work across phones, foldables, and tablets.

---

## Features

- **News Feed** — Top headlines grouped by category (General, Technology, Business, Sports, Entertainment, Science, Health) with pull-to-refresh
- **Featured Article Card** — The first article with an image is promoted to a large hero card
- **Category Filtering** — Horizontally scrollable chip row; switching categories fetches a fresh batch from the API
- **Search** — Full-text search across all news articles
- **Bookmarks** — Save articles to a local Room database; bookmarks persist across sessions
- **Article Detail** — Full-body article view with bookmark toggle
- **Adaptive Layout** — Two-pane list/detail on tablets and unfolded foldables; single-pane navigation on phones

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
│   ├── common/       # Extensions, utilities, ArticleCache
│   ├── database/     # AppDatabase, BookmarkDao, BookmarkEntity
│   ├── di/           # NetworkModule, DatabaseModule
│   ├── domain/       # BaseUseCase, NoParams
│   └── network/      # NetworkResult sealed class
│
├── feature/
│   └── news/
│       ├── data/         # NewsRepositoryImpl, NewsApiService, DTOs
│       ├── di/           # NewsModule
│       ├── domain/       # UseCases, NewsRepository interface, Article model
│       └── presentation/
│           ├── home/     # HomeScreen, HomeContent, HomeComponents, HomeViewModel
│           ├── search/   # SearchScreen, SearchContent, SearchViewModel
│           ├── bookmarks/# BookmarksScreen, BookmarksContent, BookmarksViewModel
│           └── detail/   # DetailScreen, DetailContent, DetailViewModel
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

The Home feature uses `ListDetailPaneScaffold` to display article detail inline on large screens.

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
