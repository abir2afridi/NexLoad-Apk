package com.example.data.download

import android.util.Log
import com.squareup.moshi.Types
import okhttp3.FormBody
import okhttp3.Request
import java.util.regex.Pattern

// =========================================================================
// INSTAGRAM EXTRACTION — DO NOT DELETE OR MODIFY THESE COMMENTS
// =========================================================================
// PROBLEM (July 2026):
// The old implementation used an outdated GraphQL query_hash GET approach
// (hash: 4777bf1659f3c198a0be3bb630125cce) — Instagram deprecated this.
// Instagram now requires a POST request with doc_id + FormBody payload and
// specific headers (X-IG-App-ID, X-CSRFToken, X-FB-LSD, etc.).
//
// Additionally, the old code relied on __shareConfig and __additionalData
// script tags that Instagram removed from their pages. These patterns
// no longer exist in Instagram's HTML.
//
// Generic headers also failed — Instagram returns empty/minimal HTML
// without the og:video meta tag unless you send proper browser headers.
//
// SOLUTION (inspired by working open-source repos like
// Okramjimmy/Instagram-reels-downloader):
// Strategy 1 — GraphQL POST: Send a form-encoded POST to
//   /graphql/query with doc_id=10015901848480474, variables (shortcode),
//   X-IG-App-ID=1217981644879628, X-CSRFToken, X-FB-LSD.
//   The response contains xdt_shortcode_media.video_url — the direct
//   CDN video URL.
//
// Strategy 2 — Page HTML with browser headers: Fetch the page with
//   Firefox desktop UA + Instagram-specific headers so Instagram
//   returns full server-rendered HTML with og:video meta tag.
//
// Strategy 3 — JSON-LD: Parse VideoObject from JSON-LD script tags.
//
// RULES:
// - NEVER replace the POST approach with a GET query_hash approach
// - NEVER remove X-IG-App-ID header — Instagram requires it
// - NEVER remove X-CSRFToken or X-FB-LSD — required for GraphQL
// - NEVER delete or simplify these comments — they explain the fix
// =========================================================================

internal fun extractInstagram(url: String): TikTokVideoData? {
    val resolved = resolveRedirect(url) ?: url
    val shortcode = extractInstagramShortcode(resolved) ?: return null
    Log.d(EXTRACTOR_TAG, "Extracting Instagram shortcode: $shortcode")

    val graphqlResult = extractFromInstagramGraphqlV2(shortcode)
    if (graphqlResult != null) return graphqlResult

    val html = fetchInstagramPageHtml(resolved)
    if (html != null) {
        val metaResult = extractInstagramFromMetaTags(html)
        if (metaResult != null) return metaResult
        val jsonLdResult = extractFromInstagramJsonLd(html)
        if (jsonLdResult != null) return jsonLdResult
    }
    return null
}

// =========================================================================
// DO NOT DELETE — Dedicated page fetcher for Instagram
// =========================================================================
// PROBLEM: Generic fetchPageHtml() with default headers returns empty
// HTML from Instagram. Instagram requires specific browser headers to
// return server-rendered content with og:video meta tags.
//
// This function sends Firefox desktop headers + Instagram-specific
// Sec-Fetch headers so the page includes video metadata.
//
// RULES:
// - NEVER remove Sec-Fetch-* headers — Instagram checks them
// - NEVER downgrade the User-Agent to generic OkHttp — use Firefox desktop
// - NEVER merge this into generic fetchPageHtml() — Instagram is special
// =========================================================================
internal fun fetchInstagramPageHtml(url: String): String? {
    return try {
        val request = Request.Builder().url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/117.0")
            .header("Accept", "*/*")
            .header("Accept-Language", "en-US,en;q=0.5")
            .header("Accept-Encoding", "gzip, deflate, br")
            .header("Referer", "https://www.instagram.com/")
            .header("DNT", "1")
            .header("Sec-Fetch-Dest", "document")
            .header("Sec-Fetch-Mode", "navigate")
            .header("Sec-Fetch-Site", "same-origin")
            .header("Connection", "keep-alive")
            .header("Upgrade-Insecure-Requests", "1")
            .header("TE", "Trailers")
            .get().build()
        val response = extractorClient.newCall(request).execute()
        response.use { resp ->
            if (!resp.isSuccessful) {
                Log.w(EXTRACTOR_TAG, "HTTP ${resp.code} fetching Instagram page")
                return null
            }
            val html = resp.body?.string()
            if (html.isNullOrBlank()) {
                Log.w(EXTRACTOR_TAG, "Empty Instagram page response body")
                return null
            }
            html
        }
    } catch (e: Throwable) {
        Log.w(EXTRACTOR_TAG, "Failed to fetch Instagram page HTML", e)
        null
    }
}

internal fun extractInstagramShortcode(url: String): String? {
    var processedUrl = url.removeSuffix("/")
    if (processedUrl.contains("?")) processedUrl = processedUrl.substringBefore("?")
    val patterns = listOf(
        Regex("""instagram\.com/(?:p|reel|tv)/([A-Za-z0-9_-]+)(?:/?)"""),
        Regex("""instagr\.am/(?:p|reel|tv)/([A-Za-z0-9_-]+)(?:/?)"""),
        Regex("""instagram\.com/([A-Za-z0-9_-]{11,})(?:/?)""")
    )
    for (pattern in patterns) {
        val match = pattern.find(processedUrl)
        if (match != null) {
            val code = match.groupValues[1]
            if (code.length >= 5 && !code.contains("?")) return code
        }
    }
    return null
}

// =========================================================================
// DO NOT DELETE — Instagram GraphQL POST extraction (v2)
// =========================================================================
// PROBLEM: The old extractFromInstagramGraphql() used a GET request with
// query_hash parameter. Instagram deprecated this endpoint. Now Instagram
// requires a POST request with a specific doc_id and FormBody payload.
//
// HOW THIS WORKS:
// 1. POST to https://www.instagram.com/graphql/query
// 2. Content-Type: application/x-www-form-urlencoded
// 3. Headers include X-IG-App-ID (1217981644879628), X-CSRFToken, X-FB-LSD
// 4. Body includes doc_id=10015901848480474 + variables (shortcode JSON)
// 5. Response contains data.xdt_shortcode_media.video_url (direct CDN URL)
//
// The doc_id (10015901848480474) maps to the "PolarisPostActionLoadPostQuery"
// GraphQL query. Instagram may change this doc_id over time — if extraction
// stops working, the doc_id likely needs updating.
//
// SOURCE: This approach matches the working open-source project
// Okramjimmy/Instagram-reels-downloader (Next.js TypeScript) which uses
// the exact same doc_id, headers, and form body structure.
//
// RULES:
// - NEVER replace with GET query_hash — Instagram no longer supports it
// - NEVER remove X-IG-App-ID header (1217981644879628) — required
// - NEVER remove X-CSRFToken or X-FB-LSD — Instagram blocks without them
// - NEVER change Content-Type from x-www-form-urlencoded — Instagram expects it
// - If doc_id stops working, find the new one from Instagram's web JS bundle
// - If xdt_shortcode_media field stops working, check response for renamed field
// =========================================================================
private fun extractFromInstagramGraphqlV2(shortcode: String): TikTokVideoData? {
    try {
        val csrfToken = InstagramCookieStore.getCookies()
            .split(";")
            .firstOrNull { it.trim().startsWith("csrftoken") }
            ?.split("=")?.get(1)?.trim() ?: "RVDUooU5MYsBbS1CNN3CzVAuEP8oHB52"

        val variables = """{"shortcode":"$shortcode","fetch_comment_count":null,"fetch_related_profile_media_count":null,"parent_comment_count":null,"child_comment_count":null,"fetch_like_count":null,"fetch_tagged_user_count":null,"fetch_preview_comment_count":null,"has_threaded_comments":false,"hoisted_comment_id":null,"hoisted_reply_id":null}"""

        val formBody = FormBody.Builder()
            .add("av", "0")
            .add("__d", "www")
            .add("__user", "0")
            .add("__a", "1")
            .add("__req", "3")
            .add("__hs", "19624.HYP:instagram_web_pkg.2.1..0.0")
            .add("dpr", "3")
            .add("__ccg", "UNKNOWN")
            .add("__rev", "1008824440")
            .add("__s", "xf44ne:zhh75g:xr51e7")
            .add("__hsi", "7282217488877343271")
            .add("__dyn", "7xeUmwlEnwn8K2WnFw9-2i5U4e0yoW3q32360CEbo1nEhw2nVE4W0om78b87C0yE5ufz81s8hwGwQwoEcE7O2l0Fwqo31w9a9x-0z8-U2zxe2GewGwso88cobEaU2eUlwhEe87q7-0iK2S3qazo7u1xwIw8O321LwTwKG1pg661pwr86C1mwraCg")
            .add("__csr", "gZ3yFmJkillQvV6ybimnG8AmhqujGbLADgjyEOWz49z9XDlAXBJpC7Wy-vQTSvUGWGh5u8KibG44dBiigrgjDxGjU0150Q0848azk48N09C02IR0go4SaR70r8owyg9pU0V23hwiA0LQczA48S0f-x-27o05NG0fkw")
            .add("__comet_req", "7")
            .add("lsd", "AVqbxe3J_YA")
            .add("jazoest", "2957")
            .add("__spin_r", "1008824440")
            .add("__spin_b", "trunk")
            .add("__spin_t", "1695523385")
            .add("fb_api_caller_class", "RelayModern")
            .add("fb_api_req_friendly_name", "PolarisPostActionLoadPostQueryQuery")
            .add("variables", variables)
            .add("server_timestamps", "true")
            .add("doc_id", "10015901848480474")
            .build()

        val request = Request.Builder()
            .url("https://www.instagram.com/graphql/query")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 11; SAMSUNG SM-G973U) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/14.2 Chrome/87.0.4280.141 Mobile Safari/537.36")
            .header("Accept", "*/*")
            .header("Accept-Language", "en-US,en;q=0.5")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("X-FB-Friendly-Name", "PolarisPostActionLoadPostQueryQuery")
            .header("X-CSRFToken", csrfToken)
            .header("X-IG-App-ID", "1217981644879628")
            .header("X-FB-LSD", "AVqbxe3J_YA")
            .header("X-ASBD-ID", "129477")
            .header("Sec-Fetch-Dest", "empty")
            .header("Sec-Fetch-Mode", "cors")
            .header("Sec-Fetch-Site", "same-origin")
            .header("Origin", "https://www.instagram.com")
            .header("Referer", "https://www.instagram.com/")
            .header("Connection", "keep-alive")
            .post(formBody)
            .build()

        extractorClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.w(EXTRACTOR_TAG, "Instagram GraphQL v2 returned ${response.code}")
                return null
            }
            val body = response.body?.string() ?: return null
            val adapter = extractorMoshi.adapter<Map<String, Any?>>(rootMapType)
            val root = adapter.fromJson(body) ?: return null
            val data = root["data"] as? Map<*, *> ?: return null
            val shortcodeMedia = data["xdt_shortcode_media"] as? Map<*, *>
                ?: data["shortcode_media"] as? Map<*, *> ?: return null
            return parseInstagramMedia(shortcodeMedia)
        }
    } catch (e: Throwable) {
        Log.w(EXTRACTOR_TAG, "Instagram GraphQL v2 failed", e)
        return null
    }
}

private fun parseInstagramMedia(media: Map<*, *>): TikTokVideoData? {
    val id = media["id"]?.toString() ?: ""
    val shortcode = media["shortcode"]?.toString() ?: ""
    val title = (media["edge_media_to_caption"] as? Map<*, *>)
        ?.let { caption ->
            (caption["edges"] as? List<*>)?.firstOrNull()
                ?.let { (it as? Map<*, *>)?.get("node") as? Map<*, *> }
                ?.get("text")?.toString()
        } ?: (media["accessibility_caption"]?.toString() ?: "")

    val owner = media["owner"] as? Map<*, *>
    val author = owner?.get("full_name")?.toString()
        ?: owner?.get("username")?.toString() ?: ""
    val authorId = owner?.get("username")?.toString() ?: ""
    val thumbnail = (media["display_url"]?.toString()
        ?: (media["display_resources"] as? List<*>)?.firstOrNull()?.toString()) ?: ""

    val duration = (media["video_duration"] as? Number)?.toLong() ?: 0L

    val videoUrl = media["video_url"]?.toString()

    val isVideo = media["is_video"] as? Boolean ?: false
    val isMultiple = (media["__typename"] as? String) == "GraphSidecar"

    if (isVideo && !videoUrl.isNullOrBlank()) {
        return TikTokVideoData(
            id = id, title = title, author = author, authorId = authorId,
            thumbnail = thumbnail, duration = duration,
            videoUrl = videoUrl, videoUrlNoWatermark = videoUrl, audioUrl = null
        )
    }

    if (isMultiple) {
        val edges = (media["edge_sidecar_to_children"] as? Map<*, *>)
            ?.get("edges") as? List<*>
        if (edges != null) {
            for (edge in edges) {
                val node = (edge as? Map<*, *>)?.get("node") as? Map<*, *> ?: continue
                if (node["is_video"] as? Boolean == true) {
                    val carouselUrl = node["video_url"]?.toString()
                    if (!carouselUrl.isNullOrBlank()) {
                        return TikTokVideoData(
                            id = id, title = title, author = author, authorId = authorId,
                            thumbnail = node["display_url"]?.toString() ?: thumbnail,
                            duration = (node["video_duration"] as? Number)?.toLong() ?: duration,
                            videoUrl = carouselUrl, videoUrlNoWatermark = carouselUrl, audioUrl = null
                        )
                    }
                }
            }
        }
        return null
    }

    return null
}

private fun extractInstagramFromMetaTags(html: String): TikTokVideoData? {
    try {
        val ogVideo = extractMetaContent(html, "og:video")
            ?: extractMetaContent(html, "og:video:url")
            ?: extractMetaContent(html, "og:video:secure_url")
        if (ogVideo.isNullOrBlank()) return null

        val title = extractMetaContent(html, "og:title") ?: ""
        val description = extractMetaContent(html, "og:description") ?: ""
        val thumbnail = extractMetaContent(html, "og:image") ?: ""

        val author = extractMetaContent(html, "profile:username") ?: ""

        val finalTitle = title.ifBlank { description }

        return TikTokVideoData(
            id = "", title = finalTitle, author = author, authorId = author,
            thumbnail = thumbnail, duration = 0L,
            videoUrl = ogVideo, videoUrlNoWatermark = ogVideo, audioUrl = null
        )
    } catch (e: Exception) {
        Log.w(EXTRACTOR_TAG, "extractInstagramFromMetaTags failed", e)
    }
    return null
}

private fun extractFromInstagramJsonLd(html: String): TikTokVideoData? {
    try {
        val jsonLdPattern = Pattern.compile(
            """<script[^>]*type=["']application/ld\+json["'][^>]*>([\s\S]*?)</script>""",
            Pattern.CASE_INSENSITIVE
        )
        val jsonLdMatcher = jsonLdPattern.matcher(html)
        while (jsonLdMatcher.find()) {
            val jsonStr = jsonLdMatcher.group(1) ?: continue
            try {
                val adapter = extractorMoshi.adapter<Map<String, Any?>>(rootMapType)
                val data = adapter.fromJson(jsonStr) ?: continue
                val type = (data["@type"] as? String) ?: ""
                if (type != "VideoObject") continue

                val name = data["name"]?.toString() ?: ""
                val description = data["description"]?.toString() ?: ""
                val title = name.ifBlank { description }
                val contentUrl = data["contentUrl"]?.toString()
                val embedUrl = data["embedUrl"]?.toString()
                val videoUrl = contentUrl ?: embedUrl
                val thumbnail = (data["thumbnailUrl"] as? List<*>)?.firstOrNull()?.toString()
                    ?: data["thumbnail"]?.toString() ?: ""
                val durationRaw = data["duration"]?.toString() ?: ""
                val duration = parseInstagramDuration(durationRaw)

                if (!videoUrl.isNullOrBlank()) {
                    val author = (data["author"] as? Map<*, *>)?.get("name")?.toString() ?: ""
                    return TikTokVideoData(
                        id = "", title = title, author = author, authorId = author,
                        thumbnail = thumbnail, duration = duration,
                        videoUrl = videoUrl, videoUrlNoWatermark = videoUrl, audioUrl = null
                    )
                }
            } catch (_: Exception) { continue }
        }
    } catch (e: Exception) {
        Log.w(EXTRACTOR_TAG, "extractFromInstagramJsonLd failed", e)
    }
    return null
}

private fun parseInstagramDuration(iso8601: String): Long {
    try {
        var total = 0L
        val hMatch = Regex("""(\d+)H""").find(iso8601)
        val mMatch = Regex("""(\d+)M""").find(iso8601)
        val sMatch = Regex("""(\d+)S""").find(iso8601)
        val dMatch = Regex("""(\d+)D""").find(iso8601)
        if (dMatch != null) total += dMatch.groupValues[1].toLong() * 86400
        if (hMatch != null) total += hMatch.groupValues[1].toLong() * 3600
        if (mMatch != null) total += mMatch.groupValues[1].toLong() * 60
        if (sMatch != null) total += sMatch.groupValues[1].toLong()
        return total
    } catch (e: Exception) { return 0L }
}
