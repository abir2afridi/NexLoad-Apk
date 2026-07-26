# NexLoad v1.3.0 Release Notes

**Release Date:** July 26, 2026

## Overview

v1.3.0 is a major feature release focused on expanding supported video platforms and improving the overall user experience. Instagram, Facebook, and Pinterest now have dedicated multi-strategy extraction pipelines, while the new yt-dlp integration provides broader platform coverage. The vault gets biometric unlock and a pattern lock, and the UI sees numerous refinements.

## What's Changed

### 🚀 New Platforms & Extraction

- **Instagram Downloader** — 3-strategy pipeline: GraphQL POST API (doc_id + X-IG-App-ID) → page HTML with browser headers → JSON-LD VideoObject; cookie-authenticated via WebView login
- **Facebook Downloader** — 4-strategy extraction: m.facebook.com → www → mbasic → oEmbed; facebookexternalhit/1.1 UA bypasses 403 errors
- **Pinterest Downloader** — 5-strategy extraction with brace-counting JSON parser for relay scripts; pin.it short URL resolution
- **yt-dlp Integration** — Broad platform coverage via youtubedl-android + ffmpeg
- **Custom HTTP Headers** — Support for authenticated downloads

### 🔒 Vault & Security

- Biometric authentication (fingerprint/face) for auto-lock vault
- Pattern lock view component added

### 🎨 UI/UX Improvements

- Browser home screen with improved navigation
- Network configuration, download format, and subtitle settings
- Look and feel customization screen
- Battery optimization management
- Splash screen on app start
- Dashboard animations and AMOLED theme support
- Downloads and Files consolidated into a single tabbed view
- Time display customization

### 🐞 Bug Fixes

- Facebook CDN 403 forbidden errors resolved
- TikTok extraction reliability improved with expanded domain support
- Back navigation now goes to Home tab first

### 📦 Infrastructure

- Complete GitHub community templates (issues, PRs, contributing, code of conduct, security, support)
- Stale issue/PR management and auto-labeling workflows

## Installation

### Download

| File | Size |
|------|------|
| `app-release.apk` | — |
| `app-release.aab` | — |

### Requirements

- Android 7.0 (API 24) or higher
- ARM64 / ARM / x86_64

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for complete changelog.
