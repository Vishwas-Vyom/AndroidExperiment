# Testing

## Running Tests

- **Unit tests:** `app/src/test/java/` — run with `gradlew test`, no device needed
- **Instrumented tests:** `app/src/androidTest/java/` — run with `gradlew connectedAndroidTest`, requires a connected device or running emulator
- Test runner: `androidx.test.runner.AndroidJUnitRunner`, framework: JUnit 4

## Rules

- Never mock the database in instrumented tests — use a real in-memory Room instance
- Do not touch test files during the after-task cleanup pass
- Preview composables (`@Preview`) live in the same file as the composable they preview; they are not tests and do not belong under `src/test/`
