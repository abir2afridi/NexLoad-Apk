# NexLoad — Smart Video Downloader

<!-- markdownlint-disable MD033 MD013 -->
<p align="center">
  <img src="app/NexLoad.png" alt="NexLoad Logo" width="192">
</p>
<!-- markdownlint-enable MD033 -->

[![Version](https://img.shields.io/badge/version-1.4.1-blue.svg)](https://github.com/abir2afridi/NexLoad-Apk/releases/tag/v1.4.1)
[![Release](https://img.shields.io/github/release/abir2afridi/NexLoad-Apk.svg)](https://github.com/abir2afridi/NexLoad-Apk/releases/latest)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-7.0%2B-brightgreen.svg)](https://developer.android.com/about/versions/nougat)
[![API](https://img.shields.io/badge/API-24%E2%80%9336-blueviolet.svg)](https://developer.android.com/studio/releases/platforms)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple.svg)](https://kotlinlang.org/)
[![Build](https://img.shields.io/badge/build-passing-success.svg)](.github/workflows/release.yml)

[⬇️ Download Latest APK](https://github.com/abir2afridi/NexLoad-Apk/releases/latest/download/app-release.apk)
&nbsp;|&nbsp;
[📜 Release Notes](https://github.com/abir2afridi/NexLoad-Apk/releases/latest)
&nbsp;|&nbsp;
[📋 Changelog](CHANGELOG.md)

An Android application for downloading videos and media from the web with a built-in browser, multi-threaded download engine, and private vault.

## Features

- **In-App Browser** — Full WebView with incognito mode, HTTPS-only toggle, tracker blocking, bookmarking, and media detection via JavaScript bridge
- **Multi-Threaded Download Engine** — Segmented (chunked) downloads with pause/resume, real-time speed tracking, and adaptive threading
- **Analyze-First UX** — Paste any link on the dashboard, tap Analyze, see platform info + quality options, then download
- **Multi-Platform Video Downloader** — Supports TikTok, Instagram, Facebook, Twitter/X, Reddit, Pinterest, SoundCloud, Vimeo, Twitch, Dailymotion, Tumblr, Steam, and ANY website via generic fallback extraction
- **TikTok Downloader** — TikWM API + 9 fallback strategies for HD no-watermark, watermarked, and audio-only downloads
- **Instagram Downloader** — 3-strategy chain: GraphQL POST API (doc_id + X-IG-App-ID, primary), page HTML with Firefox desktop headers (og:video fallback), JSON-LD VideoObject (tertiary); cookie-authenticated session via InstagramLoginActivity WebView
- **Facebook Downloader** — 3-strategy custom extraction (m.facebook.com → www → mbasic), no yt-dlp dependency, User-Agent: facebookexternalhit/1.1 for CDN, cookie injection, share URL resolution
- **Pinterest Downloader** — 5-strategy extraction: og:video, JSON-LD VideoObject, relay script data with brace-counting JSON parser, contentUrl regex, pinimg CDN URL; pin.it short URL resolution; browser headers for server-side rendering
- **Twitter/X Downloader** — og:video, twitter:player:stream, and CDN URL extraction
- **Steam Downloader** — Embedded Steam video page extraction via video URL patterns
- **Generic Fallback** — 10 extraction strategies for ANY website (og:video, JSON-LD, video tags, CDN patterns, etc.)
- **Social Media Authentication** — WebView-based Instagram and Facebook login for cookie-captured extraction
- **Media Detection** — Automatically detects `<video>` and downloadable media links on web pages
- **Download Queue** — Active downloads section with progress bars, speed indicators, estimated remaining time, and health badges
- **Private Vault** — PIN-protected secure storage for sensitive downloads; files hidden from device gallery
- **File Library** — Completed downloads browser with category filtering (Video/Audio/Images/Other), video playback, file sharing, and move-to-vault
- **Download Health Monitoring** — Background WorkManager worker that periodically verifies file integrity and connection health
- **Customizable Theme** — Light, Dark, System, and AMOLED Black modes with 13 accent colors
- **Storage Management** — Visual storage overview with video/audio size breakdown and one-tap cache optimization

## Tech Stack

| Layer | Technology |
| ----- | ---------- |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM with Repository Pattern |
| Database | Room (SQLite) via Kotlin Coroutines Flow |
| Networking | OkHttp 4.x, Moshi |
| Browser Engine | Android WebView with JavaScript Interface |
| Background Work | WorkManager 2.9.x |
| Image Loading | Coil |
| Security | Encrypted SharedPreferences, FileProvider |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 (Android 16) |

## Installation

### Latest Release — v1.4.1

| File | Size | SHA-256 |
| ---- | ---- | ------- |
| [app-release.apk](https://github.com/abir2afridi/NexLoad-Apk/releases/latest/download/app-release.apk) | TBD | `TBD (CI in progress)` |
| [app-release.aab](https://github.com/abir2afridi/NexLoad-Apk/releases/latest/download/app-release.aab) | TBD | `TBD (CI in progress)` |

### Requirements

- Android 7.0 (API 24) or higher
- ARM64 / ARM / x86_64

## Building

1. Open the project in Android Studio.
2. Sync Gradle and build the `app` module.

### Signing

Release builds require the following environment variables:

- `KEYSTORE_PATH`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

Debug builds use a local `debug.keystore` with default credentials.

## Project Structure

```text
app/src/main/java/com/example/
├── MainActivity.kt                    # Entry point with bottom navigation
├── ui/
│   ├── viewmodel/MainViewModel.kt     # Central ViewModel (StateFlow + Room)
│   ├── screens/
│   │   ├── DashboardTab.kt            # Home dashboard with analyze-first link input
│   │   ├── BrowserTab.kt              # WebView browser with media detection
│   │   ├── DownloadsTab.kt            # Download queue (active + completed)
│   │   ├── FilesTab.kt                # File library with categories
│   │   ├── VaultTab.kt                # PIN-protected private vault
│   │   ├── SettingsTab.kt             # Preferences, theme, social media login, about
│   │   ├── InstagramLoginActivity.kt  # WebView-based Instagram login for cookie capture
│   │   ├── FacebookLoginActivity.kt   # WebView-based Facebook login for cookie capture
│   │   ├── stream/
│   │   │   └── StreamDownloadCard.kt  # Stream download quality/card UI
│   │   └── browser/
│   │       └── BrowserMediaSheet.kt   # Browser media detection bottom sheet
│   ├── components/                    # Reusable composables
│   │   ├── TabHeader.kt               # Section header with category + title
│   │   ├── DownloadHealthIndicators.kt # Integrity & connection health badges
│   │   ├── DownloadDialog.kt          # Quality selection, thread count, privacy toggle
│   │   ├── VideoPlayerDialog.kt       # Full-screen video player
│   │   └── PatternLockView.kt         # Canvas-based PIN pattern lock for vault
│   └── theme/                         # Colors, typography, theme system
├── data/
│   ├── database/                      # Room entities, DAOs, database
│   │   ├── Entities.kt                # DownloadEntity, BookmarkEntity, HistoryEntity
│   │   ├── DAOs.kt                    # DownloadDao, queries
│   │   └── AppDatabase.kt             # Room database singleton
│   └── download/                      # Download engine + extractors
│       ├── BaseExtractor.kt           # Shared data types, cookie stores, utilities
│       ├── DownloadEngine.kt          # Multi/single-thread download manager
│       ├── MediaUtils.kt              # Formatting, filename parsing
│       ├── DownloadIntegrityWorker.kt # Periodic health checks via WorkManager
│       ├── VideoExtractor.kt          # Multi-platform extraction router (20+ platforms)
│       └── stream/                    # Platform-specific stream extractors
│           ├── TikTokExtractor.kt     # TikWM API + 9 fallback strategies
│           ├── TikTokCookieStore.kt   # Shared CookieJar for TikTok requests
│           ├── InstagramExtractor.kt  # GraphQL POST → page HTML → JSON-LD
│           ├── FacebookExtractor.kt   # m.facebook → www → mbasic extraction
│           ├── TwitterExtractor.kt    # og:video + player:stream + CDN
│           ├── RedditExtractor.kt     # JSON API extraction
│           ├── PinterestExtractor.kt  # 5-strategy extraction
│           ├── SoundCloudExtractor.kt # oEmbed + og:audio
│           ├── VimeoExtractor.kt      # oEmbed extraction
│           ├── TwitchExtractor.kt     # og:video + CDN
│           ├── DailymotionExtractor.kt# oEmbed extraction
│           ├── TumblrExtractor.kt     # og:video + CDN
│           ├── YtDlpExtractor.kt      # yt-dlp wrapper (youtubedl-android)
│           └── GenericExtractor.kt    # 10-strategy fallback for any website
```

## Instagram Extraction — Detailed Process

Instagram video extraction uses a 3-strategy pipeline in `InstagramExtractor.kt`:

### Strategy 1: GraphQL POST API (Primary)

```http
POST https://www.instagram.com/graphql/query
Content-Type: application/x-www-form-urlencoded
X-IG-App-ID: 1217981644879628
X-CSRFToken: <from cookie>
X-FB-LSD: AVqbxe3J_YA
```

- Sends `doc_id=10015901848480474` + `variables` (shortcode JSON) in form body
- Response contains `data.xdt_shortcode_media.video_url` — the direct CDN video URL
- This is the SAME approach used by working open-source repos (Okramjimmy/Instagram-reels-downloader)

### Strategy 2: Page HTML with Browser Headers (Fallback)

- Fetches page HTML with Firefox desktop User-Agent + Instagram-specific Sec-Fetch headers
- Instagram returns server-rendered HTML with `<meta property="og:video">` containing the direct CDN URL
- Without these specific headers, Instagram returns empty JS-rendered shell

### Strategy 3: JSON-LD VideoObject (Tertiary)

- Parses `<script type="application/ld+json">` for VideoObject with `contentUrl`

### Why the Old Approach Failed

- **Old GraphQL**: Used GET with `query_hash=4777bf1659f3c198a0be3bb630125cce` — Instagram deprecated this
- **__additionalData / __shareConfig**: These JavaScript variables no longer exist in Instagram's HTML
- **Generic headers**: Returned empty HTML without video data

### Cookie Authentication

- Users can log in via WebView (`InstagramLoginActivity`) to capture session cookies
- Cookies are stored in `InstagramCookieStore` (SharedPreferences)
- GraphQL requests extract `X-CSRFToken` from cookies automatically
- Without cookies, public reels/posts still work — login improves reliability

## Facebook Extraction — Detailed Process

Facebook video extraction uses a 4-strategy pipeline in `FacebookExtractor.kt`:

### Strategy 1: m.facebook.com (Primary)

- Rewrites URL to `m.facebook.com` — mobile page has simplest HTML
- Parses `<video>` tags with `hd_src`/`sd_src` attributes for direct CDN URLs
- Also extracts from script data containing `playable_url` patterns
- **Most reliable** — avoids Facebook's aggressive rate-limiting on desktop pages

### Strategy 2: Desktop Fallback (`www.facebook.com`)

- Desktop page HTML with JSON-LD VideoObject extraction
- Script data patterns for `hd_src`/`sd_src` in embedded JSON
- DASH manifest `BaseURL` extraction for modern Facebook video pages

### Strategy 3: mbasic.facebook.com (Legacy Fallback)

- Oldest/simplest HTML format with direct `<video>` tag `src` attributes
- Used when both mobile and desktop pages fail

### Strategy 4: oEmbed API (Last Resort)

- Facebook's oEmbed endpoint (`https://www.facebook.com/plugins/video/oembed.json`)
- Returns thumbnail URL and occasionally a direct video URL
- Used as final fallback before giving up

### URL Resolution

- Facebook share URLs (`/share/r/xxx`) are resolved to actual video page URLs via HEAD/GET request with Android Chrome User-Agent

### Why yt-dlp Is Not Used

- yt-dlp (youtubedl-android) hangs indefinitely on Facebook URLs
- When it does return, it frequently returns HTTP 403 Forbidden on CDN URLs
- Custom extraction is faster and more reliable

### Important Caveats

- **User-Agent**: Uses `facebookexternalhit/1.1` in `BaseExtractor.fetchPageHtml()` for Facebook CDN — this is required to avoid 403 on `fbcdn.net` URLs
- **Cookie Injection**: Facebook login cookies can be captured via WebView (`FacebookLoginActivity`) for access to private/restricted videos
- **DASH vs MP4**: Modern Facebook serves separate audio+video DASH streams — the CDN URL may be a manifest, not a direct MP4
- **Token Expiry**: CDN tokens (`oh=`, `oe=`, `_nc_sid=`) expire in ~30-60 minutes — download immediately after extraction

## TikTok Extraction — Detailed Process

TikTok video extraction uses a 10-strategy pipeline in `TikTokExtractor.kt`:

### Strategy 1: TikWM API (Primary)

```http
POST https://www.tikwm.com/api/
Content-Type: application/x-www-form-urlencoded
```

- Sends `url=<tiktok-url>&hd=1` as form body
- TikWM returns processed video data with direct CDN URLs
- Falls back to GET `https://www.tikwm.com/api/?url=<encoded-url>&hd=1` if POST fails
- Returns HD no-watermark video, watermarked video, and audio-only options

### Strategy 2: SSSTik API (Secondary)

- Alternative third-party API (ssstik.io) for direct video extraction
- Used when TikWM API is unreachable or rate-limited

### Strategy 3: Mobile API (TikTok Mobile Endpoint)

- Extracts video `itemId` from the URL (or HTML)
- Sends request to TikTok's internal mobile API endpoint
- Returns JSON with video URLs, author info, and metadata

### Strategy 4-9: HTML Parsing (Page-Based Fallbacks)

When API strategies fail, the page HTML is fetched and parsed with 6 methods:

1. **Universal Data** — Parses `<script id="__UNIVERSAL_DATA_FOR_VIEW_INITIAL_DATA__">`
2. **Init Props** — Parses `<script id="__INITIAL_PROPS_INITIAL_STATE__">`
3. **Sigi Data** — Parses `<script id="SIGI_STATE">` (client-side state)
4. **CDN URL Pattern** — Regex search for direct TikTok CDN URLs in any script
5. **Meta Tags** — `og:video`, `og:video:secure_url`, `twitter:player:stream`
6. **JSON-LD VideoObject** — `<script type="application/ld+json">` parsing
7. **Video Tags** — Any `<video>` tag `src` attributes in the page

### Strategy 10: oEmbed API (Last Resort)

- TikTok's oEmbed endpoint: `https://www.tiktok.com/oembed?url=<itemId>`
- Returns metadata and thumbnail URL

## Pinterest Extraction — Detailed Process

Pinterest video extraction uses a 5-strategy pipeline in `PinterestExtractor.kt`:

### Strategy 1: og:video Meta Tag (Simplest)

- Checks `<meta property="og:video">` for direct video URL
- Rare in modern Pinterest (2025+) but checked first for simplicity

### Strategy 2: JSON-LD VideoObject (Reliable)

- Parses `<script type="application/ld+json">` for VideoObject with `contentUrl`
- Most reliable strategy — Pinterest includes this on video pins
- Content URL format: `https://v1.pinimg.com/videos/.../720p.mp4`

### Strategy 3: Relay Script Data (Most Complete)

- Parses `__PWS_RELAY_REGISTER_COMPLETED_REQUEST__` scripts
- Uses **brace-counting JSON parser** (`extractBalancedJson()`) — tracks `{` depth and string escaping to extract the complete nested JSON object
- Old regex `[\s\S]*?\}` broke on deeply nested JSON (stopped at first `}`)
- Video data located at: `storyPinData.pages[].blocks[].videoDataV2`
  - `videoList720P.v720P.url` — MP4 (preferred)
  - `videoListMobile.vHLSV3MOBILE.url` — m3u8 (fallback)

### Strategy 4: contentUrl Regex (JSON Parser Bypass)

- Direct regex: `"contentUrl"\s*:\s*"(https:\\/\\/v1\.pinimg\.com[^"]+\.mp4)"`
- Bypasses JSON parser when JSON-LD has duplicate keys that cause parse failures

### Strategy 5: Pinimg CDN URL Regex (Catch-All)

- Searches entire HTML for any `v1.pinimg.com` URL ending in `.mp4`
- Pinterest CDN has no authentication issues — always accessible

### Pinterest URL Resolution

- `pin.it` short URLs (e.g., `https://pin.it/2ima6B8Wm`) are resolved to full `https://www.pinterest.com/pin/{id}/` URLs via redirect following

### Why Other Approaches Failed

- **Missing relay data**: Without browser `Sec-Fetch-*` and `Sec-Ch-Ua` headers, Pinterest returns empty JavaScript shell — no relay scripts
- **Broken regex**: `[\s\S]*?\}` is non-greedy and stops at first `}`, yielding partial JSON that fails to parse — fixed with brace-counting
- **Unresolved short URLs**: `resolveRedirect()` only handled TikTok short URLs — added `isShortPinterest` check

## Supported Platforms

| Platform | Extraction Method |
| -------- | ----------------- |
| TikTok | TikWM API + 9 fallback strategies |
| Instagram | GraphQL POST (doc_id + X-IG-App-ID) → page og:video → JSON-LD VideoObject |
| Facebook | m.facebook.com (primary) → www → mbasic, facebookexternalhit/1.1 UA, cookie injection |
| Twitter/X | og:video + twitter:player:stream + CDN |
| Reddit | JSON API extraction |
| Pinterest | og:video + JSON-LD + relay data (brace-counting) + contentUrl regex + CDN URL |
| SoundCloud | oEmbed + og:audio |
| Vimeo | oEmbed extraction |
| Twitch | og:video + CDN |
| Dailymotion | oEmbed extraction |
| Tumblr | og:video + CDN |
| Steam | Embedded Steam video page extraction |
| Any Website | 10-strategy generic fallback |

## Developer

### Abir Hasan Siam

- GitHub: [github.com/abir2afridi](https://github.com/abir2afridi)
- Portfolio: [abir2afridi.vercel.app](https://abir2afridi.vercel.app/)
- Computer Science · Independent University of Bangladesh

## GitHub Community

- [Contributing Guidelines](.github/CONTRIBUTING.md) — How to contribute, code style, PR process
- [Code of Conduct](.github/CODE_OF_CONDUCT.md) — Community standards and enforcement
- [Security Policy](.github/SECURITY.md) — Reporting vulnerabilities
- [Support](.github/SUPPORT.md) — Where to get help
- [Issue Templates](.github/ISSUE_TEMPLATE/) — Bug reports, feature requests, and more
- [Pull Request Template](.github/PULL_REQUEST_TEMPLATE.md) — PR submission guidelines

## License

MIT License — feel free to use, modify, and distribute.
