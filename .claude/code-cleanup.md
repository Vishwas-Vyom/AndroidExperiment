# After Every Task — Code Cleanup

After completing any task that modifies Kotlin or Gradle files, scan every touched file and fix the following without being asked:

## Imports

- Remove all unused imports
- Remove wildcard imports (`import foo.*`) — replace with explicit imports only if the symbols are actually used

## Dead Code

- Delete commented-out code blocks (old implementations, TODO-stubs that are never coming back)
- Delete unused private functions, properties, and local variables
- Delete unused parameters only when the function is private and not part of an interface/override

## Boilerplate

- Remove empty `init {}` blocks, empty companion objects, and empty `override fun` bodies that only call `super` when the override adds nothing
- Remove redundant `return Unit`, explicit `: Unit` return types on functions that already have no return value
- Remove `@Suppress` annotations whose warning no longer exists in the file

## Formatting

- Collapse multiple consecutive blank lines to a single blank line
- Remove trailing whitespace

## Do NOT Touch

- Public API signatures (even if unused — callers may be outside this module)
- `@Keep`-annotated or `@JvmField`/`@JvmStatic` members (may be referenced via reflection)
- Any code with a `// keep` or `// intentional` comment
- Test files — leave those exactly as-is unless the task was specifically about tests
