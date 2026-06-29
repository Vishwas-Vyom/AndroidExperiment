# Build Commands

```
# Windows (use gradlew.bat or gradlew in Git Bash)
gradlew assembleDebug          # Build debug APK
gradlew assembleRelease        # Build release APK
gradlew test                   # Run local unit tests
gradlew connectedAndroidTest   # Run instrumented tests (requires connected device/emulator)
gradlew clean                  # Clean build outputs
gradlew lint                   # Run Android Lint
```

Run a single test class:
```
gradlew test --tests "me.vishwas.androidexperimental.ExampleUnitTest"
```
