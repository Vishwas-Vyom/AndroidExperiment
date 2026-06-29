# Jetpack Compose Rules

## File Organization

- Never put all composables for a screen in a single file — split by responsibility
- Each screen's `presentation/` package should contain multiple files:
  - `{Feature}Screen.kt` — stateful root composable only (wires ViewModel → stateless screen)
  - `{Feature}Content.kt` — stateless main content composable
  - `{Feature}Components.kt` — smaller, reusable composables used within this feature
  - `{Feature}ViewModel.kt` — ViewModel + `UiState` sealed class
- If any composable file exceeds ~150 lines, split it further

## Stateless / Stateful Split

- **Stateful composables** (connected to ViewModel): one per screen, named `{Feature}Screen`. Collect state here and pass data down — do nothing else
- **Stateless composables**: receive only plain data + lambda callbacks, hold no `remember` state, and contain all the actual UI logic
- Never call `viewModel()` or `collectAsState()` below the root screen composable
- Prefer hoisting state up; use `remember` only when state is genuinely local to a single composable (e.g., a dropdown open/close)

## Composable Size & Decomposition

- A composable should do one thing — if it handles layout AND content AND behavior, split it
- If a `@Composable` function exceeds ~40–50 lines, extract sub-composables
- Name extracted composables clearly by what they render, not where they're used (e.g., `UserAvatarRow`, not `TopPartOfProfileScreen`)
- Avoid deep nesting — flatten with extracted composables instead of indented blocks

## File Templates

**`{Feature}Screen.kt`** — stateful root only
```kotlin
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileContent(
        uiState = uiState,
        onRetry = viewModel::onRetry,
        onNavigateBack = onNavigateBack,
    )
}
```

**`{Feature}Content.kt`** — stateless, all real UI here
```kotlin
@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is ProfileUiState.Loading -> ProfileLoadingIndicator(modifier)
        is ProfileUiState.Error   -> ProfileErrorView(uiState.message, onRetry, modifier)
        is ProfileUiState.Success -> ProfileSuccessLayout(uiState.data, onNavigateBack, modifier)
    }
}
```

**`{Feature}Components.kt`** — small, single-purpose composables
```kotlin
@Composable
fun UserAvatarRow(
    name: String,
    avatarUrl: String,
    modifier: Modifier = Modifier,
) { /* ... */ }

@Composable
fun ProfileStatBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) { /* ... */ }
```

**`{Feature}ViewModel.kt`** — UiState sealed class lives here
```kotlin
sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class  Success(val data: ProfileData) : ProfileUiState()
    data class  Error(val message: String) : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    // ...
}
```

## Core Compose Principles

### Unidirectional Data Flow (UDF)
- State flows down as parameters, events flow up as lambda callbacks — never the reverse
- A composable never mutates state it doesn't own; it calls a lambda and lets the owner decide
- `UiState` is the single source of truth; the UI is a pure function of it

### Recomposition Scope
- Keep recomposition scopes small — extract a sub-composable rather than reading state in a large parent
- Use `derivedStateOf` for any value computed from other state to avoid redundant recompositions:
  ```kotlin
  val isButtonEnabled by remember { derivedStateOf { name.isNotBlank() && email.isNotBlank() } }
  ```
- Use `key()` inside `LazyColumn`/`LazyRow` to give each item a stable identity:
  ```kotlin
  items(list, key = { it.id }) { item -> ItemRow(item) }
  ```

### Stability — Prevent Unnecessary Recomposition
- All data classes passed to composables must use only stable/immutable types (`String`, `Int`, `Boolean`, primitives, `ImmutableList`, etc.)
- Annotate data classes that Compose cannot infer as stable with `@Immutable` or `@Stable`
- Never pass unstable types (`List<T>`, `Map<K,V>`, mutable objects) directly into composables — wrap with `kotlinx.collections.immutable` or annotate
- Lambdas passed as parameters must be stable — wrap in `remember` if they capture local variables that change:
  ```kotlin
  val onClick = remember(itemId) { { onItemClick(itemId) } }
  ```

### State Hoisting
- Hoist state to the lowest common ancestor that needs it — no higher, no lower
- A composable that only displays data should accept value + callback, never own the state
- Use `rememberSaveable` instead of `remember` for UI state that must survive configuration changes

### Side Effects
- `LaunchedEffect(key)` — coroutines tied to composition; re-launches when key changes
- `DisposableEffect(key)` — for setup/teardown of non-Compose resources (listeners, subscriptions)
- `SideEffect` — sync Compose state to non-Compose code on every successful recomposition
- `rememberCoroutineScope` — only for coroutines triggered by user events (button clicks), never for initial data loading
- Never start coroutines or perform I/O directly in a composable body

### Modifier Rules
- Always accept `modifier: Modifier = Modifier` as the first optional parameter — apply it to the root layout element only, never to children
- Order matters: `padding` before `background` clips the background; `background` before `padding` does not
- Never hardcode `fillMaxSize()` or fixed sizes inside a component — let the caller decide via Modifier
- Chain modifiers in this order: size/layout → drawing (background, border) → padding → click/semantics

### Accessibility & Semantics
- Every interactive element must have a `contentDescription` or `semantics { }` block
- Use `Modifier.semantics { role = Role.Button }` for custom clickable elements
- Ensure touch targets are at least 48.dp × 48.dp

### Previews
- Every stateless composable must have a `@Preview` — use `@PreviewLightDark` for theme coverage
- Never preview the stateful `Screen` composable — preview the stateless `Content` or component variants
- Use `@PreviewParameter` for composables with complex data to cover loading/error/success states:
  ```kotlin
  @Preview @Composable
  private fun ProfileContentPreview(
      @PreviewParameter(ProfileUiStateProvider::class) uiState: ProfileUiState,
  ) { ProfileContent(uiState = uiState, onRetry = {}, onNavigateBack = {}) }
  ```

### General
- Never hardcode colors or text styles — always use `MaterialTheme.colorScheme` and `MaterialTheme.typography`
- Use `LazyColumn`/`LazyRow` for any list that could exceed a handful of items
- Avoid deeply nested composables — flatten with extracted composables instead of indented blocks
- Do not use `GlobalScope` — always use `viewModelScope` or `rememberCoroutineScope`
