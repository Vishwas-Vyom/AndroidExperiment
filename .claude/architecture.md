# Project Architecture

Single-module Android app (`app/`) using 100% Jetpack Compose with Material3. No XML layouts.

- **Entry point:** `MainActivity.kt` — single Activity with edge-to-edge enabled, hosts the root composable
- **Navigation:** `AppNavHost.kt` — central nav graph
- **Theme:** `ui/theme/` — Material3 theme with dynamic color disabled, custom plum+amber palette
- **Package:** `me.vishwas.androidexperimental`
- **Min SDK:** 24 / Target SDK: 36

## Package Structure

```
me.vishwas.androidexperimental/
├── core/
│   ├── common/     → Extensions, base classes, utils
│   ├── ui/         → Theme, reusable Composables
│   ├── network/    → Retrofit client, interceptors, NetworkResult
│   ├── database/   → Room DB, AppDatabase
│   ├── di/         → AppModule, NetworkModule, DatabaseModule (app-wide only)
│   └── domain/     → BaseUseCase<P,R>, base models
│
├── feature/
│   └── {feature}/  → Each feature contains:
│       ├── data/         → RepositoryImpl, API service, DTOs
│       ├── di/           → Hilt module for this feature
│       ├── domain/       → UseCase(s), Repository interface, models
│       └── presentation/ → ViewModel, Composable screens, UiState
│
└── App.kt, AppNavHost.kt, MainActivity.kt
```

## Architecture Rules

- Every new feature **must** follow the `data/di/domain/presentation` layout
- Business logic belongs in UseCases only — never in ViewModels
- API/DB calls belong in `RepositoryImpl` (`data/`) only — never in UseCases
- Repository interface lives in `domain/`, implementation in `data/`
- All UI is Jetpack Compose only — no XML layouts
- Hilt for all DI: `@HiltViewModel`, `@Module`, `@InstallIn`
- Every ViewModel uses `StateFlow` + a `UiState` sealed class
- Feature-specific Hilt modules live in that feature's `di/` package; `core/di/` is for app-wide deps (Retrofit, Room, etc.)

## Key Stack Versions

Managed via version catalog at `gradle/libs.versions.toml`:
- Kotlin 2.2.10, AGP 9.2.1, Gradle 9.4.1
- Compose BOM 2026.02.01, Material3, Activity Compose 1.13.0
- Lifecycle Runtime KTX 2.10.0
- `kotlin.android` plugin is applied transitively via the Hilt plugin — do **not** add it explicitly or the build fails with "Cannot add extension with name 'kotlin'"
