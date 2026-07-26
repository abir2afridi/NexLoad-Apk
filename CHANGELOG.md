# Changelog

## [1.4.0] — 2026-07-27

### 🚀 Features

- **Language Selection & Localization** — Support for ~100 languages including Bengali/Bangla. Language preference persisted and applied via `Configuration.setLocale`. Activity recreates on language change.
- **DotLottie Animation Placeholder** — Animated loading indicator for video preview thumbnails on the dashboard while extraction is in progress.
- **Dynamic Color Support** — Expanded theming with dynamic color (Material You) integration for Android 12+.
- **Extended Theme Customization** — New typography, app bar, and surface style options in Look & Feel settings.
- **Advanced Layout Customization** — Refined visual customization options for display and layout preferences.

### ♻️ Refactoring

- **Look & Feel Settings** — Restructured layout with extracted LanguageData module for maintainable language list management.
- **120dp Bottom Clearance** — Adjusted padding across Browser Home, Browser Tab, Format Settings, Look & Feel, Network Settings, and Subtitle Settings screens for floating navigation bar compatibility.
- **WebView Container** — Wrapped BrowserTab WebView in Column layout with spacer for consistent bottom clearance.

### 🎨 Style

- **Consistent Bottom Padding** — Standardized to 120dp across all 21 screens for uniform floating navigation bar clearance.

### 📦 Build / CI / Docs

- **v1.4.0 Release** — Version bumped, new SHA-256 checksums and APK/AAB sizes in associated CI run.

---

## [1.3.0] — 2026-07-26

### 🚀 Features

- **Instagram Downloader** — Full 3-strategy extraction pipeline: GraphQL POST API (doc_id + X-IG-App-ID), page HTML with Firefox desktop headers, JSON-LD VideoObject; cookie-authenticated session via InstagramLoginActivity WebView
- **Facebook Downloader** — 4-strategy extraction: m.facebook.com → www → mbasic → oEmbed; facebookexternalhit/1.1 UA for CDN access; share URL resolution
- **Pinterest Downloader** — 5-strategy extraction with brace-counting JSON parser for relay scripts; pin.it short URL resolution; browser headers for server-side rendering
- **yt-dlp Integration** — Direct source URL downloads via youtubedl-android + ffmpeg for broad platform coverage
- **Custom HTTP Headers** — Support for authenticated requests with custom headers per download
- **Extensible Media Extraction System** — Plugin-style extractor architecture for easy new platform addition
- **Network Configuration Settings** — Download format selection, subtitle configuration, battery optimization management
- **Look and Feel Customization** — New appearance settings screen
- **Browser Home Screen** — Improved browser start page with better navigation
- **Vault Pattern Lock** — Canvas-based PIN pattern lock component for private vault
- **Biometric Authentication** — Auto-lock vault with biometric (fingerprint/face) unlock
- **Splash Screen** — Branded loading sequence on app start
- **Public Storage Access & Migration** — Legacy storage path migration support
- **Time Display & Animations** — Customizable time format and enhanced dashboard animations

### 🐞 Bug Fixes

- **Facebook CDN 403 Forbidden** — Resolved by using facebookexternalhit/1.1 User-Agent and cookie injection for fbcdn.net URLs
- **TikTok Extraction Reliability** — Expanded domain support and fallback strategy improvements
- **Back Navigation** — System back gesture now navigates to Home tab instead of exiting

### ♻️ Refactoring

- **Video Info Card** — Redesigned for persistent label visibility (Title/Author/Duration/Output)
- **Icon Migration** — Migrated to auto-mirrored icons for RTL support
- **Material 3 Opt-In** — Added experimental Material 3 to concurrent fragments dialog
- **yt-dlp Reliability** — Improved error handling and timeout management
- **Media Detection Bottom Sheet** — Redesigned layout for better usability
- **Browser Navigation** — Standardized padding, moved settings to Settings tab
- **Downloads & Files Consolidation** — Unified into a single tabbed view
- **Dashboard Layout** — Restructured for AMOLED theme support

### 🎨 Style

- **Slider Implementation** — Updated in network settings
- **Padding Consistency** — Adjusted bottom padding and spacing across screens

### 📦 Build / CI / Docs

- **GitHub Community Infrastructure** — Issue templates (6 forms), PR template, CONTRIBUTING.md, CODE_OF_CONDUCT.md, SECURITY.md, SUPPORT.md, stale/label workflows
- **yt-dlp & ffmpeg Dependencies** — Integrated for enhanced extraction capabilities
- **CI Workflow Fix** — Dynamic version extraction from tag, debug keystore fallback
- **README Documentation** — Detailed extraction processes for Instagram, Facebook, TikTok, Pinterest, Steam

---

## [1.2.0] — 2026-07-16

### 🚀 Features

- **Back Navigation to Home Tab** — System back gesture now navigates to the Home/Dashboard tab instead of exiting the app. Exit only works from the Home tab.

### 📦 CI/CD

- **Simplified Release Workflow** — Streamlined GitHub Actions release pipeline by removing redundant steps.

---

## [1.1.0] — 2026-07-16

### 🚀 New Features

- **Multi-Tab Browser Support** — Opera Mini-style tab gallery with normal and private (incognito) modes, tab switching, and tab closure
- **Browser Privacy & Ad Blocking Engine** — Comprehensive tracker blocking with custom block list management
- **Persistent Browsing History Storage** — Browse history is now saved and accessible from the browser menu
- **Bulk Download Actions** — Select multiple downloads for batch pause, resume, retry, or delete
- **Configurable Download Path** — Choose custom storage location for downloaded files
- **About Screen** — App information, developer credits, and links
- **Force-Dark Web Mode** — Force dark mode on all web content
- **Dynamic Browser Navigation Positioning** — Move browser navigation bar to top, bottom, or custom position

### ✨ Improvements

- **Redesigned Dashboard** — New analyze-first UX with dynamic platform detection and favicon support
- **Multi-Item Selection Mode** — Select multiple files in library for batch operations
- **Enhanced Download Monitoring** — Real-time progress, speed indicators, ETA, and health badges
- **Redesigned Tab Headers** — Improved typography and theme toggle in section headers
- **AMOLED Dark Mode** — True black theme for OLED displays
- **13 Accent Colors** — Customizable accent color picker with persistence
- **Dynamic Greeting** — Personalized welcome message on dashboard
- **Improved Layout Spacing** — Consistent padding and content spacing across all screens
- **Bottom Navigation Bar** — Refined appearance with collapsible browser navigation

### ⚡ Performance

- **Multi-Threaded Download Engine** — Segmented (chunked) downloads with adaptive threading
- **Automatic File Extension & MIME Type Resolution** — Smart media type detection
- **Media Scan on Completion** — Automatic media store notification after download
- **WorkManager Background Checks** — Periodic file integrity and connection health monitoring

### 🐞 Bug Fixes

- **Download State Management** — Fixed download queue state inconsistencies
- **Media Scanner Trigger** — Fixed media scan not firing on file completion
- **Default Download Path** — Fixed fallback path resolution

### 🛠 Refactoring

- **Unified VideoExtractor** — Replaced platform-specific extractors with a generic multi-strategy engine supporting 20+ platforms
- **Removed Dedicated Login Activities** — Consolidated Instagram/Facebook authentication into WebView-based cookie management
- **Browser Toolbar UI** — Refactored layout for better spacing and responsive design
- **Screen Padding** — Standardized padding across all screens

### 📦 Dependencies

- AGP 9.3.0
- Kotlin 2.2.10
- Gradle 9.6.1
- Compose BOM 2024.09.00
- Room 2.7.0
- OkHttp 4.10.0
- Retrofit 2.12.0
- Moshi 1.15.2
- WorkManager 2.9.1

### 📱 Android

- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 36 (Android 16)
- **Compile SDK:** 36
- **Package:** `com.aistudio.videodownloader.pxqtrv`

---

## [1.0.0] — 2026-07-12

### 🚀 Initial Release

- First public release of NexLoad
- In-App Browser with WebView, HTTPS-only toggle, tracker blocking
- Multi-platform video downloader (TikTok, Instagram, Facebook, Twitter/X, and more)
- Download queue management with progress tracking
- Private vault with PIN protection
- File library with category filtering
- Material 3 design with customizable themes

[1.4.0]: https://github.com/abir2afridi/NexLoad-Apk/releases/tag/v1.4.0
[1.3.0]: https://github.com/abir2afridi/NexLoad-Apk/releases/tag/v1.3.0
[1.2.0]: https://github.com/abir2afridi/NexLoad-Apk/releases/tag/v1.2.0
[1.1.0]: https://github.com/abir2afridi/NexLoad-Apk/releases/tag/v1.1.0
[1.0.0]: https://github.com/abir2afridi/NexLoad-Apk/releases/tag/v1.0.0
