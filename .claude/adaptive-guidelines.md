# Adaptive Layout Guidelines (Tablet & Foldable)

Every new feature must be built with three window-size classes in mind:
- **Compact** (< 600 dp) — phones, foldable closed
- **Medium** (600–840 dp) — foldable unfolded portrait, landscape phone, small tablet
- **Expanded** (> 840 dp) — large tablets, foldable landscape

## Navigation

`NavigationSuiteScaffold` in `AppNavHost.kt` handles navigation automatically:
- Compact → `NavigationBar` (bottom)
- Medium → `NavigationRail` (side)
- Expanded → `NavigationDrawer` (permanent)

Never add a manual `NavigationBar` or `NavigationRail` to individual screens — always let `NavigationSuiteScaffold` own navigation.

## List + Detail screens

Any feature that has a list screen → detail screen transition must use `ListDetailPaneScaffold`:

```kotlin
val navigator = rememberListDetailPaneScaffoldNavigator<String>()
val scope = rememberCoroutineScope()

ListDetailPaneScaffold(
    directive = navigator.scaffoldDirective,
    value = navigator.scaffoldValue,
    listPane = { AnimatedPane { /* list content */ } },
    detailPane = { AnimatedPane { /* detail content */ } },
)
```

- When `navigator.scaffoldDirective.maxHorizontalPartitions > 1` → two-pane mode; show detail in pane
- When `== 1` → single-pane mode; push detail screen via NavController as normal

Store the selected item in the ViewModel (`selectPaneArticle`, `paneArticle`) so the detail pane always has the right data.

## Content max width

Content columns must never stretch indefinitely on large screens:
- Article / card lists: `Modifier.widthIn(max = 840.dp)` on the `LazyColumn`, wrapped in `Box(contentAlignment = Alignment.TopCenter)`
- Article body text: `Modifier.widthIn(max = 720.dp)` on the body `Column`
- Hero images / full-bleed elements: always `fillMaxWidth()` — no cap

## Foldable posture

Use `currentWindowAdaptiveInfo()` from `androidx.compose.material3.adaptive` to detect fold state when needed. The `androidx.window` dependency is already included.

For tabletop mode (device half-open horizontally), place interactive controls below the hinge. Check `WindowLayoutInfo.displayFeatures` for `FoldingFeature`.

## Dependencies

All adaptive dependencies are declared in `libs.versions.toml` and included in `app/build.gradle.kts`:
- `androidx.window` — foldable layout info
- `material3-adaptive-navigation-suite` — NavigationSuiteScaffold
- `adaptive-layout` — ListDetailPaneScaffold, AnimatedPane
- `adaptive-navigation` — rememberListDetailPaneScaffoldNavigator
