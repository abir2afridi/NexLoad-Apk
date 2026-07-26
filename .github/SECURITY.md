# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.2.x   | :white_check_mark: |
| 1.1.x   | :white_check_mark: |
| < 1.1   | :x:                |

## Reporting a Vulnerability

**Please do NOT file a public GitHub issue for security vulnerabilities.**

NexLoad handles sensitive data including:
- Social media login cookies (Instagram, Facebook)
- Private vault files (PIN/biometric-protected storage)
- Download history and bookmarks
- Local file system access

### Private Reporting Channels

1. **GitHub Security Advisories** (preferred):
   [Report a vulnerability](https://github.com/abir2afridi/NexLoad-Apk/security/advisories/new)

2. **Email**: abir2afridi@gmail.com

You will receive a response within **72 hours** of your report. We ask that you
give us 90 days to address the issue before public disclosure.

### What to Include

- Description of the vulnerability and its impact
- Steps to reproduce (Android version, device model, NexLoad version)
- Proof of concept or exploit code (if applicable)
- Any suggested fixes (optional)

## Scope

### In Scope

- Authentication bypass (vault PIN, biometric)
- Cookie theft / session hijacking (Instagram, Facebook login)
- Unauthorized file system access
- Data leakage (download history, bookmarks, vault files)
- Remote code execution via extractor input
- Man-in-the-middle attacks on download connections

### Out of Scope

- Social engineering attacks
- Physical device access attacks
- Vulnerabilities in third-party libraries (report upstream)
- Denial of service attacks on public APIs

## Safe Harbor

We will not pursue legal action against researchers who:
- Follow this disclosure policy
- Make a good faith effort to avoid privacy violations
- Do not destroy or corrupt data
- Do not publicly disclose the vulnerability before we have addressed it

## Preferred Languages

We prefer all communications in English.
