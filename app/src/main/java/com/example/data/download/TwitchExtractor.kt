package com.example.data.download

import android.util.Log
import com.squareup.moshi.Types
import okhttp3.Request
import java.util.regex.Pattern

internal fun extractTwitch(url: String): TikTokVideoData? {
    val resolved = resolveRedirect(url) ?: url
    val videoId = extractTwitchVideoId(resolved)
    Log.d(EXTRACTOR_TAG, "Extracting Twitch: $videoId")

    val html = fetchPageHtml(resolved)
    if (html != null) {
        val metaResult = extractTwitchFromMeta(html)
        if (metaResult != null) return metaResult
        val jsonLdResult = extractFromTwitchJsonLd(html)
        if (jsonLdResult != null) return jsonLdResult
        val scriptResult = extractFromTwitchScriptData(html)
        if (scriptResult != null) return scriptResult
        val videoTagResult = extractFromTwitchVideoTag(html)
        if (videoTagResult != null) return videoTagResult
    }
    return null
}

internal fun extractTwitchVideoId(url: String): String? {
    var u = url.removeSuffix("/")
    if (u.contains("?")) u = u.substringBefore("?")
    if (u.contains("#")) u = u.substringBefore("#")

    val patterns = listOf(
        Regex("""twitch\.tv/videos/(\d+)"""),
        Regex("""twitch\.tv/\w+/clip/(\w+)"""),
        Regex("""clips\.twitch\.tv/(\w+)"""),
        Regex("""twitch\.tv/(\w+)""")
    )
    for (pattern in patterns) {
        val match = pattern.find(u)
        if (match != null) return match.groupValues[1]
    }
    return null
}

private fun extractTwitchFromMeta(html: String): TikTokVideoData? {
    try {
        val ogVideo = extractMetaContent(html, "og:video")
            ?: extractMetaContent(html, "og:video:url")
            ?: extractMetaContent(html, "og:video:secure_url")
            ?: extractMetaContent(html, "twitter:player:stream")
        if (!ogVideo.isNullOrBlank()) {
            val title = extractMetaContent(html, "og:title")
                ?: extractMetaContent(html, "twitter:title") ?: ""
            val thumbnail = extractMetaContent(html, "og:image")
                ?: extractMetaContent(html, "twitter:image") ?: ""
            return TikTokVideoData(
                id = "", title = title, author = "", authorId = "",
                thumbnail = thumbnail, duration = 0L,
                videoUrl = ogVideo, videoUrlNoWatermark = ogVideo, audioUrl = null
            )
        }
    } catch (e: Exception) {
        Log.w(EXTRACTOR_TAG, "extractTwitchFromMeta failed", e)
    }
    return null
}

private fun extractFromTwitchJsonLd(html: String): TikTokVideoData? {
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
                if (type != "VideoObject" && type != "Clip") continue

                val contentUrl = data["contentUrl"]?.toString()
                if (contentUrl.isNullOrBlank()) continue

                val title = data["name"]?.toString() ?: ""
                val author = (data["author"] as? Map<*, *>)?.get("name")?.toString() ?: ""
                val thumbnail = (data["thumbnailUrl"] as? List<*>)?.firstOrNull()?.toString() ?: ""
                val durationRaw = data["duration"]?.toString() ?: ""
                val duration = parseDuration(durationRaw)

                return TikTokVideoData(
                    id = "", title = title, author = author, authorId = author,
                    thumbnail = thumbnail, duration = duration,
                    videoUrl = contentUrl, videoUrlNoWatermark = contentUrl, audioUrl = null
                )
            } catch (_: Exception) { continue }
        }
    } catch (e: Exception) {
        Log.w(EXTRACTOR_TAG, "extractFromTwitchJsonLd failed", e)
    }
    return null
}

private fun extractFromTwitchScriptData(html: String): TikTokVideoData? {
    try {
        val vodMatch = Regex("""<script[^>]*>[\s\S]*?video_url[\s\S]*?</script>""").find(html)
            ?: Regex("""<script[^>]*>[\s\S]*?sourceUrl[\s\S]*?</script>""").find(html)

        if (vodMatch != null) {
            val scriptContent = vodMatch.value
            val urlInScript = Regex(""""video_url"\s*:\s*"([^"]+)""").find(scriptContent)
                ?: Regex(""""sourceUrl"\s*:\s*"([^"]+)""").find(scriptContent)
                ?: Regex(""""default_url"\s*:\s*"([^"]+)""").find(scriptContent)
                ?: Regex("""https?://[^"'\s<>]*\.m3u8[^"'\s<>]*""").find(scriptContent)

            if (urlInScript != null) {
                val videoUrl = urlInScript.groupValues.let { values ->
                    if (values.size > 1) values[1] else values[0]
                }.replace("\\/", "/")

                val titleMatch = Regex(""""title"\s*:\s*"([^"]+)""").find(scriptContent)
                val title = titleMatch?.groupValues?.get(1)?.replace("\\/", "/") ?: ""

                return TikTokVideoData(
                    id = "", title = title, author = "", authorId = "",
                    thumbnail = "", duration = 0L,
                    videoUrl = videoUrl, videoUrlNoWatermark = videoUrl, audioUrl = null
                )
            }
        }
    } catch (e: Exception) {
        Log.w(EXTRACTOR_TAG, "extractFromTwitchScriptData failed", e)
    }
    return null
}

private fun extractFromTwitchVideoTag(html: String): TikTokVideoData? {
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
        Log.w(EXTRACTOR_TAG, "extractFromTwitchVideoTag failed", e)
    }
    return null
}
