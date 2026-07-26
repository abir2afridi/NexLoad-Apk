# Contributing to NexLoad

Thank you for your interest in contributing to NexLoad — an Android video downloader with a built-in browser, multi-threaded download engine, and private vault.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Development Workflow](#development-workflow)
- [Branching Strategy](#branching-strategy)
- [Commit Message Convention](#commit-message-convention)
- [Code Style](#code-style)
- [Testing](#testing)
- [Pull Request Process](#pull-request-process)
- [Reporting Issues](#reporting-issues)

## Getting Started

### Prerequisites

- **Android Studio** Ladybug or newer (2024.2+)
- **JDK 21** (Temurin recommended)
- **Gradle 9.6.1** (included via Gradle Wrapper)
- **Android SDK** API 24–36
- **Git**

### Local Setup

1. Fork and clone the repository:
   ```bash
   git clone https://github.com/your-username/NexLoad-Apk.git
   cd NexLoad-Apk
   ```

2. Open the project in Android Studio.

3. Sync Gradle and verify the build:
   ```bash
   ./gradlew assembleDebug
   ```

4. Run on an emulator or device:
   ```bash
   ./gradlew installDebug
   ```

### Build Variants

- **debug** — Uses `debug.keystore` with default credentials. Suitable for development.
- **release** — Requires `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` environment variables.

## Development Workflow

1. Find or create an issue to work on.
2. Assign the issue to yourself (if you have permissions) or leave a comment.
3. Create a branch from `main` (see branching strategy below).
4. Make your changes following the code style guide.
5. Add or update tests as needed.
6. Run the linter and tests locally.
7. Submit a pull request.

## Branching Strategy

- **`main`** — Stable, release-ready code. Protected.
- **`feat/<short-description>`** — New features (e.g., `feat/twitter-video-extraction`).
- **`fix/<short-description>`** — Bug fixes (e.g., `fix/crash-on-empty-link`).
- **`docs/<short-description>`** — Documentation changes.
- **`refactor/<short-description>`** — Code refactoring.
- **`chore/<short-description>`** — Tooling, dependency updates, CI changes.

Base all branches off `main` and open PRs back to `main`.

## Commit Message Convention

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]
[optional footer]
```

**Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `chore`, `ci`

**Examples:**
```
feat(downloader): add multi-threaded segmented download support
fix(extractor): handle null response from Instagram GraphQL API
docs(readme): update supported platforms table
refactor(browser): extract tab management into separate class
```

## Code Style

- **Kotlin** — Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- **Compose** — Follow [Jetpack Compose API Guidelines](https://developer.android.com/jetpack/compose/api-guidelines).
- **Formatting** — Use Android Studio's built-in Kotlin formatter (`Ctrl+Alt+L` / `Cmd+Option+L`).
- **Linting** — Run `./gradlew lint` before committing.
- **No tabs** — Use 2-space indentation for Kotlin, 4-space for XML.

## Testing

- **Unit tests** use JUnit 4, Robolectric, and Roborazzi.
- **Instrumented tests** use AndroidX Test.
- Run all tests:
  ```bash
  ./gradlew test
  ```
- Run instrumented tests (requires emulator/device):
  ```bash
  ./gradlew connectedCheck
  ```
- Run a specific test class:
  ```bash
  ./gradlew test --tests "com.example.*ExtractorTest"
  ```

## Pull Request Process

1. Ensure your PR branch is up to date with `main`.
2. Fill out the pull request template completely.
3. Link the relevant issue(s) with `Closes #issue-number`.
4. Request review from a maintainer.
5. Address all review comments.
6. Maintainers will merge once all checks pass and approvals are received.

### PR Requirements

- All CI checks must pass (build, lint, tests).
- No new compiler warnings.
- UI changes must include screenshots.
- Changes to extractors must include test URLs and manual verification steps.

## Reporting Issues

Use the appropriate issue template:

- **Bug report** — For crashes, incorrect behavior, or unexpected results.
- **Feature request** — For new features or improvements.
- **UI/UX feedback** — For visual or usability issues.
- **Build failure** — If the project does not compile.
- **Documentation** — For README, CHANGELOG, or doc improvements.
- **Security vulnerability** — Report privately via GitHub Security Advisories.

See [Issue Templates](https://github.com/abir2afridi/NexLoad-Apk/issues/new/choose).

## Community

- **Discussions:** [GitHub Discussions](https://github.com/abir2afridi/NexLoad-Apk/discussions)
- **Developer:** [Abir Hasan Siam](https://github.com/abir2afridi)

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
