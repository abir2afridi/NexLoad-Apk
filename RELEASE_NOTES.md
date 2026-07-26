# NexLoad v1.4.0 Release Notes

**Release Date:** July 27, 2026

## Overview

v1.4.0 focuses on theming, localization, and UI polish. The app now supports ~100 languages with persistent language selection, expanded Material You dynamic color integration, and enhanced Look & Feel customization. All screens have been audited for consistent 120dp bottom clearance against the floating navigation bar.

## What's Changed

### 🚀 Features

- **Language Selection & Localization** — Choose from ~100 languages including Bengali/Bangla. Language preference is persisted and applied system-wide via `Configuration.setLocale`. The activity recreates automatically on selection.
- **DotLottie Animation Placeholder** — Video preview thumbnails on the dashboard now show a smooth animated loading indicator (via Lottie) while extraction is in progress, replacing the previous static placeholder.
- **Dynamic Color Support** — Full Material You dynamic color integration for Android 12+, with automatic and manual theme toggling.
- **Extended Theme Customization** — New options for typography, app bar styling, and surface style in the Look & Feel settings screen.
- **Advanced Layout Customization** — Additional display and layout preference controls for a more personalized experience.

### ♻️ Refactoring & Polish

- **120dp Bottom Clearance** — Comprehensive UI audit across all 21 screens ensures no content is hidden behind the floating navigation bar. 6 key screens received specific padding/spacer updates (Browser Home, Browser Tab, Format Settings, Look & Feel, Network Settings, Subtitle Settings).
- **WebView Container Restructure** — Browser tab WebView wrapped in Column layout with a trailing spacer for proper bottom clearance.
- **LanguageData Module** — Language list extracted into a dedicated module (`LanguageData.kt`) for cleaner code organization and easier maintenance.

### 📦 Build / CI / Docs

- Version bumped to 1.4.0 (versionCode 5)
- Full CI release build with signed APK and AAB outputs

## Full Changelog

See [CHANGELOG.md](../CHANGELOG.md) for the complete list of commits.
