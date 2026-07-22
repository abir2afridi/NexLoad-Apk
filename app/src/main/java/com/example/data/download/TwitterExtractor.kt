package com.example.data.download

import android.util.Log
import com.squareup.moshi.Types
import okhttp3.Request
import java.util.regex.Pattern

internal fun extractTwitter(url: String): TikTokVideoData? {
    val resolved = resolveRedirect(url) ?: url
    val tweetId = extractTweetId(resolved)
    Log.d(EXTRACTOR_TAG, "Extracting Tweet: $tweetId")

    val html = fetchPageHtml(resolved)
    if (html == null) return null

    val twitterMetaResult = extractTwitterFromMeta(html)
    if (twitterMetaResult != null) return twitterMetaResult

    val jsonLdResult = extractFromTwitterJsonLd(html)
    if (jsonLdResult != null) return jsonLdResult

    val twitterScriptResult = extractFromTwitterScript(html)
    if (twitterScriptResult != null) return twitterScriptResult

    val embedHtmlResult = extractFromTwitterEmbed(html)
    if (embedHtmlResult != null) return embedHtmlResult

    val videoTagResult = extractFromTwitterVideoTag(html)
    if (videoTagResult != null) return videoTagResult

    return null
}

internal fun extractTweetId(url: String): String? {
    var u = url
    if (u.contains("?")) u = u.substringBefore("?")
    if (u.contains("#")) u = u.substringBefore("#")
    u = u.trimEnd('/')

    val patterns = listOf(
        Regex("""twitter\.com/\w+/status/(\d+)"""),
        Regex("""x\.com/\w+/status/(\d+)"""),
        Regex("""t\.co/[A-Za-z0-9]+"""),
        Regex("""twitter\.com/\w+/(\d+)""")
    )
    for (pattern in patterns) {
        val match = pattern.find(u)
        if (match != null) return match.groupValues[1]
    }
    return u
}

private fun extractTwitterFromMeta(html: String): TikTokVideoData? {
    try {
        val ogVideo = extractMetaContent(html, "og:video")
            ?: extractMetaContent(html, "og:video:url")
            ?: extractMetaContent(html, "og:video:secure_url")
            ?: extractMetaContent(html, "twitter:player:stream")

        if (!ogVideo.isNullOrBlank() && !ogVideo.contains("video_src")) {
            val title = extractMetaContent(html, "og:title")
                ?: extractMetaContent(html, "twitter:title") ?: ""
            val description = extractMetaContent(html, "og:description")
                ?: extractMetaContent(html, "twitter:description") ?: ""
            val thumbnail = extractMetaContent(html, "og:image")
                ?: extractMetaContent(html, "twitter:image") ?: ""

            return TikTokVideoData(
                id = "", title = title.ifBlank { description }, author = "", authorId = "",
                thumbnail = thumbnail, duration = 0L,
                videoUrl = ogVideo, videoUrlNoWatermark = ogVideo, audioUrl = null
            )
        }

        val playerUrl = extractMetaContent(html, "twitter:player")
        if (!playerUrl.isNullOrBlank() && playerUrl.contains("video.twimg.com")) {
            val title = extractMetaContent(html, "twitter:title") ?: ""
            val thumbnail = extractMetaContent(html, "twitter:image") ?: ""
            return TikTokVideoData(
                id = "", title = title, author = "", authorId = "",
                thumbnail = thumbnail, duration = 0L,
                videoUrl = playerUrl, videoUrlNoWatermark = playerUrl, audioUrl = null
            )
        }
    } catch (e: Exception) {
        Log.w(EXTRACTOR_TAG, "extractTwitterFromMeta failed", e)
    }
    return null
}

private fun extractFromTwitterJsonLd(html: String): TikTokVideoData? {
    try {
        val jsonLdPattern = Pattern.compile(
            """<script[^>]*type=["']application/ld\+json["'][^>]*>([\s\S]*?)</script>""",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = jsonLdPattern.matcher(html)
        while (matcher.find()) {
            val jsonStr = matcher.group(1) ?: continue
            try {
                val adapter = extractorMoshi.adapter<Map<String, Any?>>(rootMapType)
                val data = adapter.fromJson(jsonStr) ?: continue
                val type = (data["@type"] as? String) ?: ""
                if (type != "VideoObject" && type != "SocialMediaPosting") continue

                val video = data["video"] as? Map<*, *>
                val contentUrl = video?.get("contentUrl")?.toString()
                    ?: data["contentUrl"]?.toString()
                if (contentUrl.isNullOrBlank()) continue

                val title = (data["name"]?.toString()
                    ?: video?.get("name")?.toString()
                    ?: data["description"]?.toString() ?: "")
                val author = ((data["author"] as? Map<*, *>)?.get("name")?.toString()
                    ?: (video?.get("author") as? Map<*, *>)?.get("name")?.toString() ?: "")
                val thumbnail = (data["thumbnailUrl"] as? List<*>)?.firstOrNull()?.toString()
                    ?: video?.get("thumbnailUrl")?.toString() ?: ""

                return TikTokVideoData(
                    id = "", title = title, author = author, authorId = author,
                    thumbnail = thumbnail, duration = 0L,
                    videoUrl = contentUrl, videoUrlNoWatermark = contentUrl, audioUrl = null
                )
            } catch (_: Exception) { continue }
        }
    } catch (e: Exception) {
        Log.w(EXTRACTOR_TAG, "extractFromTwitterJsonLd failed", e)
    }
    return null
}

private fun extractFromTwitterScript(html: String): TikTokVideoData? {
    try {
        val scriptPattern = Pattern.compile(
            """<script[^>]*type=["']text\/javascript["'][^>]*>([\s\S]*?)</script>""",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = scriptPattern.matcher(html)
        while (matcher.find()) {
            val scriptContent = matcher.group(1) ?: continue
            val urlMatch = Regex("""https?://[^"'\s<>]+\.(mp4|m3u8)[^"'\s<>]*""").find(scriptContent)
            if (urlMatch != null) {
                val videoUrl = urlMatch.value
                if (videoUrl.contains("video.twimg.com") || videoUrl.contains("twitter.com")) {
                    return TikTokVideoData(
                        id = "", title = "", author = "", authorId = "",
                        thumbnail = "", duration = 0L,
                        videoUrl = videoUrl, videoUrlNoWatermark = videoUrl, audioUrl = null
                    )
                }
            }
        }
    } catch (e: Exception) {
        Log.w(EXTRACTOR_TAG, "extractFromTwitterScript failed", e)
    }
    return null
}

private fun extractFromTwitterEmbed(html: String): TikTokVideoData? {
    try {
        val embedMatch = Regex("""<blockquote[^>]*class="twitter-video"[^>]*>[\s\S]*?<a[^>]*href="([^"]+)"[\s\S]*?</blockquote>""").find(html)
            ?: Regex("""<blockquote[^>]*class="twitter-tweet"[^>]*>[\s\S]*?<a[^>]*href="([^"]+)"[\s\S]*?</blockquote>""").find(html)
        if (embedMatch != null) {
            val tweetUrl = embedMatch.groupValues[1]
            val newResult = extractTwitter(tweetUrl)
            if (newResult != null) return newResult
        }
    } catch (e: Exception) {
        Log.w(EXTRACTOR_TAG, "extractFromTwitterEmbed failed", e)
    }
    return null
}

private fun extractFromTwitterVideoTag(html: String): TikTokVideoData? {
    try {
        val videoTagPattern = Pattern.compile(
            """<video[^>]+src=["']([^"']+)["']""",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = videoTagPattern.matcher(html)
        while (matcher.find()) {
            val videoUrl = matcher.group(1)
            if (!videoUrl.isNullOrBlank() && videoUrl.contains("twimg.com")) {
                return TikTokVideoData(
                    id = "", title = "", author = "", authorId = "",
                    thumbnail = "", duration = 0L,
                    videoUrl = videoUrl, videoUrlNoWatermark = videoUrl, audioUrl = null
                )
            }
        }
    } catch (e: Exception) {
        Log.w(EXTRACTOR_TAG, "extractFromTwitterVideoTag failed", e)
    }
    return null
}
