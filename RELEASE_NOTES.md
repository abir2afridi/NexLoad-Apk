# NexLoad v1.4.1 Release Notes

**Release Date:** July 27, 2026

## Overview

v1.4.1 is a maintenance release focused on codebase modularization and a battery optimization settings fix. The download extraction engine has been restructured for better maintainability, and the battery optimization request flow no longer relies on a direct intent that may fail on certain devices.

## What's Changed

### ♻️ Refactoring

- **Modular Download Extraction** — All 14 platform-specific extractors (TikTok, Facebook, Instagram, Pinterest, Reddit, SoundCloud, Tumblr, Twitch, Twitter/X, Vimeo, Dailymotion, YouTube-DL, Generic) moved into a dedicated `stream` package under `com.example.data.download.stream`. This decouples them from the core `VideoExtractor` and `DownloadEngine`, reducing complexity and making the system easier to extend.
- **Browser Media Sheet** — Inline media detection logic extracted into a reusable `BrowserMediaSheet` component.
- **Stream Download Card** — Dashboard download card logic consolidated into a dedicated `StreamDownloadCard` component.
- **Import & State Cleanup** — Unused imports and stale local state removed from `DashboardTab` and `BrowserTab`.

### 🐞 Bug Fixes

- **Battery Optimization Settings Flow** — Refactored to use a more robust approach. Instead of a direct optimization request intent (which can fail), the app now directs users to the general battery settings or app details page with clear Toast instructions. A `LifecycleEventObserver` automatically refreshes the battery hint state when the activity resumes.

### 📦 Build / CI / Docs

- README.md updated with v1.4.0 release checksums and markdown linting fixes.
- Version bumped to 1.4.1 (versionCode 6).
- Full CI release build with signed APK and AAB outputs.

## Changelog

See [CHANGELOG.md](../CHANGELOG.md) for the complete list of commits.
