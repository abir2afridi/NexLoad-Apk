package com.example.data.download

import android.util.Log
import com.squareup.moshi.Types
import okhttp3.Request
import java.util.regex.Pattern

internal fun extractPinterest(url: String): TikTokVideoData? {
    val resolved = resolveRedirect(url) ?: url
    val pinId = extractPinId(resolved)
    Log.d(EXTRACTOR_TAG, "Extracting Pinterest pin: $pinId")

    val html = fetchPageHtml(resolved)
    if (html == null) return null

    val metaResult = extractPinterestFromMeta(html)
    if (metaResult != null) return metaResult

    val jsonLdResult = extractFromPinterestJsonLd(html)
    if (jsonLdResult != null) return jsonLdResult

    val scriptResult = extractFromPinterestScriptData(html)
    if (scriptResult != null) return scriptResult

    val videoTagResult = extractFromPinterestVideoTag(html)
    if (videoTagResult != null) return videoTagResult

    return null
}

internal fun extractPinId(url: String): String? {
    var u = url.removeSuffix("/")
    if (u.contains("?")) u = u.substringBefore("?")
    val patterns = listOf(
        Regex("""pinterest\.com/pin/(\d+)"""),
        Regex("""pin\.it/(\w+)"""),
        Regex("""pinterest\.com/pin/[^/]+-(\d+)"""),
        Regex("""pinterest\.com/pin/[A-Za-z0-9_-]+/(\d+)""")
    )
    for (pattern in patterns) {
        val match = pattern.find(u)
        if (match != null) return match.groupValues[1]
    }
    return null
}

private fun extractPinterestFromMeta(html: String): TikTokVideoData? {
    try {
        val ogVideo = extractMetaContent(html, "og:video")
            ?: extractMetaContent(html, "og:video:url")
            ?: extractMetaContent(html, "og:video:secure_url")
        if (!ogVideo.isNullOrBlank()) {
            val title = extractMetaContent(html, "og:title")
                ?: extractMetaContent(html, "pinterest:title") ?: ""
            val description = extractMetaContent(html, "description")
                ?: extractMetaContent(html, "og:description") ?: ""
            val thumbnail = extractMetaContent(html, "og:image") ?: ""

            return TikTokVideoData(
                id = "", title = title.ifBlank { description }, author = "", authorId = "",
                thumbnail = thumbnail, duration = 0L,
                videoUrl = ogVideo, videoUrlNoWatermark = ogVideo, audioUrl = null
            )
        }
    } catch (e: Exception) {
        Log.w(EXTRACTOR_TAG, "extractPinterestFromMeta failed", e)
    }
    return null
}

private fun extractFromPinterestJsonLd(html: String): TikTokVideoData? {
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
                if (type != "VideoObject" && type != "Article") continue

                val contentUrl = data["contentUrl"]?.toString()
                if (contentUrl.isNullOrBlank()) continue

                val title = data["name"]?.toString() ?: data["description"]?.toString() ?: ""
                val thumbnail = (data["thumbnailUrl"] as? List<*>)?.firstOrNull()?.toString()
                    ?: data["thumbnail"]?.toString() ?: ""
                val author = (data["author"] as? Map<*, *>)?.get("name")?.toString() ?: ""

                return TikTokVideoData(
                    id = "", title = title, author = author, authorId = author,
                    thumbnail = thumbnail, duration = 0L,
                    videoUrl = contentUrl, videoUrlNoWatermark = contentUrl, audioUrl = null
                )
            } catch (_: Exception) { continue }
        }
    } catch (e: Exception) {
        Log.w(EXTRACTOR_TAG, "extractFromPinterestJsonLd failed", e)
    }
    return null
}

private fun extractFromPinterestScriptData(html: String): TikTokVideoData? {
    try {
        val pinDataMatch = Regex("""<script[^>]*data-pin-data[^>]*>([\s\S]*?)</script>""").find(html)
            ?: Regex("""<script[^>]*id=["']__PWS_INITIAL_DATA__["'][^>]*>([\s\S]*?)</script>""").find(html)

        if (pinDataMatch != null) {
            val jsonStr = pinDataMatch.groupValues[1]
            val adapter = extractorMoshi.adapter<Map<String, Any?>>(rootMapType)
            val data = adapter.fromJson(jsonStr) ?: return null

            val pinJson = data["pins"]?.let {
                val pinList = it as? List<*>
                pinList?.firstOrNull() as? Map<*, *>
            } ?: data["pin"] as? Map<*, *>
                ?: data["initialState"]?.let {
                    val initState = it as? Map<*, *>
                    initState?.get("pins")?.let { pins ->
                        val pinList = pins as? List<*>
                        pinList?.firstOrNull() as? Map<*, *>
                    }
                }

            if (pinJson != null) {
                val title = pinJson["title"]?.toString()
                    ?: pinJson["description"]?.toString() ?: pinJson["grid_description"]?.toString() ?: ""
                val author = (pinJson["pinner"] as? Map<*, *>)?.get("full_name")?.toString()
                    ?: (pinJson["user"] as? Map<*, *>)?.get("username")?.toString() ?: ""
                val thumbnail = pinJson["image_cover_url"]?.toString()
                    ?: (pinJson["images"] as? Map<*, *>)?.let { images ->
                        val orig = images["orig"] as? Map<*, *>
                        orig?.get("url")?.toString()
                    } ?: ""

                val videoUrl = pinJson["video_url"]?.toString()
                    ?: (pinJson["videos"] as? Map<*, *>)?.let { videos ->
                        val videoList = videos["video_list"] as? Map<*, *>
                        videoList?.values?.firstOrNull()?.let {
                            val entry = it as? Map<*, *>
                            entry?.get("url")?.toString()
                        }
                    }

                if (!videoUrl.isNullOrBlank()) {
                    return TikTokVideoData(
                        id = pinJson["id"]?.toString() ?: "", title = title,
                        author = author, authorId = author,
                        thumbnail = thumbnail, duration = 0L,
                        videoUrl = videoUrl, videoUrlNoWatermark = videoUrl, audioUrl = null
                    )
                }
            }
        }
    } catch (e: Exception) {
        Log.w(EXTRACTOR_TAG, "extractFromPinterestScriptData failed", e)
    }
    return null
}

private fun extractFromPinterestVideoTag(html: String): TikTokVideoData? {
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
        Log.w(EXTRACTOR_TAG, "extractFromPinterestVideoTag failed", e)
    }
    return null
}
