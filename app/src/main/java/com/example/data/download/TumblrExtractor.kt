package com.example.data.download

import android.util.Log
import com.squareup.moshi.Types
import okhttp3.Request
import java.util.regex.Pattern

internal fun extractTumblr(url: String): TikTokVideoData? {
    val resolved = resolveRedirect(url) ?: url
    Log.d(EXTRACTOR_TAG, "Extracting Tumblr post: $resolved")

    val html = fetchPageHtml(resolved)
    if (html != null) {
        val metaResult = extractTumblrFromMeta(html)
        if (metaResult != null) return metaResult
        val jsonLdResult = extractFromTumblrJsonLd(html)
        if (jsonLdResult != null) return jsonLdResult
        val scriptResult = extractFromTumblrScriptData(html)
        if (scriptResult != null) return scriptResult
        val tumblrVideoBlockResult = extractFromTumblrVideoBlock(html)
        if (tumblrVideoBlockResult != null) return tumblrVideoBlockResult
        val videoTagResult = extractFromTumblrVideoTag(html)
        if (videoTagResult != null) return videoTagResult
    }
    return null
}

private fun extractTumblrFromMeta(html: String): TikTokVideoData? {
    try {
        val ogVideo = extractMetaContent(html, "og:video")
            ?: extractMetaContent(html, "og:video:url")
            ?: extractMetaContent(html, "og:video:secure_url")
        if (!ogVideo.isNullOrBlank()) {
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
    } catch (e: Exception) {
        Log.w(EXTRACTOR_TAG, "extractTumblrFromMeta failed", e)
    }
    return null
}

private fun extractFromTumblrJsonLd(html: String): TikTokVideoData? {
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
                if (type != "VideoObject") continue

                val contentUrl = data["contentUrl"]?.toString()
                if (contentUrl.isNullOrBlank()) continue

                val title = data["name"]?.toString() ?: data["description"]?.toString() ?: ""
                val thumbnail = (data["thumbnailUrl"] as? List<*>)?.firstOrNull()?.toString() ?: ""

                return TikTokVideoData(
                    id = "", title = title, author = "", authorId = "",
                    thumbnail = thumbnail, duration = 0L,
                    videoUrl = contentUrl, videoUrlNoWatermark = contentUrl, audioUrl = null
                )
            } catch (_: Exception) { continue }
        }
    } catch (e: Exception) {
        Log.w(EXTRACTOR_TAG, "extractFromTumblrJsonLd failed", e)
    }
    return null
}

private fun extractFromTumblrScriptData(html: String): TikTokVideoData? {
    try {
        val postMatch = Regex("""<script[^>]*id=["']post_[^"']*["'][^>]*>([\s\S]*?)</script>""").find(html)
            ?: Regex("""<script[^>]*>[\s\S]*?tumblr\.app[\s\S]*?</script>""").find(html)

        if (postMatch != null) {
            val scriptContent = postMatch.value
            val urlMatch = Regex(""""url"\s*:\s*"([^"]+\.(mp4|m3u8)[^"]*)""").find(scriptContent)
                ?: Regex(""""video_url"\s*:\s*"([^"]+)""").find(scriptContent)
                ?: Regex(""""src"\s*:\s*"([^"]+\.(mp4|m3u8)[^"]*)""").find(scriptContent)

            if (urlMatch != null) {
                val videoUrl = urlMatch.groupValues[1].replace("\\/", "/")
                if (videoUrl.isNotBlank()) {
                    return TikTokVideoData(
                        id = "", title = "", author = "", authorId = "",
                        thumbnail = "", duration = 0L,
                        videoUrl = videoUrl, videoUrlNoWatermark = videoUrl, audioUrl = null
                    )
                }
            }
        }
    } catch (e: Exception) {
        Log.w(EXTRACTOR_TAG, "extractFromTumblrScriptData failed", e)
    }
    return null
}

private fun extractFromTumblrVideoBlock(html: String): TikTokVideoData? {
    try {
        val videoBlocks = listOf(
            Regex("""<video[^>]*>[\s\S]*?<source[^>]+src=["']([^"']+)["']"""),
            Regex("""<iframe[^>]+src=["']([^"']+tumblr[^"']+video[^"']*)["']"""),
            Regex("""data-video-src=["']([^"']+)["']"""),
            Regex("""<div[^>]+class=["'][^"']*post_video[^"']*["'][^>]+data-url=["']([^"']+)["']""")
        )
        for (pattern in videoBlocks) {
            val match = pattern.find(html)
            if (match != null) {
                val videoUrl = match.groupValues[1]
                if (videoUrl.isNotBlank()) {
                    return TikTokVideoData(
                        id = "", title = "", author = "", authorId = "",
                        thumbnail = "", duration = 0L,
                        videoUrl = videoUrl, videoUrlNoWatermark = videoUrl, audioUrl = null
                    )
                }
            }
        }
    } catch (e: Exception) {
        Log.w(EXTRACTOR_TAG, "extractFromTumblrVideoBlock failed", e)
    }
    return null
}

private fun extractFromTumblrVideoTag(html: String): TikTokVideoData? {
    try {
        val videoTagPattern = Pattern.compile(
            """<video[^>]+src=["']([^"']+)["']""",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = videoTagPattern.matcher(html)
        if (matcher.find()) {
            val videoUrl = matcher.group(1)
            if (!videoUrl.isNullOrBlank()) {
                return TikTokVideoData(
                    id = "", title = "", author = "", authorId = "",
                    thumbnail = "", duration = 0L,
                    videoUrl = videoUrl, videoUrlNoWatermark = videoUrl, audioUrl = null
                )
            }
        }
    } catch (e: Exception) {
        Log.w(EXTRACTOR_TAG, "extractFromTumblrVideoTag failed", e)
    }
    return null
}
