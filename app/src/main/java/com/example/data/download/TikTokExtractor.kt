package com.example.data.download

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class TikTokVideoData(
    val id: String,
    val title: String,
    val author: String,
    val authorId: String,
    val thumbnail: String,
    val duration: Long,
    val videoUrl: String?,
    val videoUrlNoWatermark: String?,
    val audioUrl: String?
)

object TikTokExtractor {
    private const val TAG = "TikTokExtractor"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Cache-Control", "no-cache")
                .header("Pragma", "no-cache")
                .build()
            chain.proceed(request)
        }
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun extract(url: String): Result<TikTokVideoData> {
        return try {
            val resolvedUrl = resolveRedirect(url)
            if (resolvedUrl == null) {
                return Result.failure(Exception("Failed to resolve TikTok URL"))
            }

            val request = Request.Builder().url(resolvedUrl).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return Result.failure(Exception("HTTP ${response.code}: Failed to fetch TikTok page"))
            }

            val html = response.body?.string() ?: return Result.failure(Exception("Empty response from TikTok"))

            val data = extractFromUniversalData(html)
                ?: extractFromInitProps(html)
                ?: extractFromMetaTags(html)
                ?: extractFromVideoUrlPattern(html)

            if (data != null && (data.videoUrl != null || data.videoUrlNoWatermark != null)) {
                Log.d(TAG, "Successfully extracted TikTok video: ${data.title}")
                Result.success(data)
            } else {
                Result.failure(Exception("Could not find video URL in TikTok page"))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error extracting TikTok video", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting TikTok video", e)
            Result.failure(e)
        }
    }

    private fun resolveRedirect(url: String): String? {
        if (!url.contains("vt.tiktok.com")) return url

        return try {
            val request = Request.Builder()
                .url(url)
                .head()
                .build()
            val response = client.newCall(request).execute()
            var resolvedUrl = response.header("Location")
            if (resolvedUrl.isNullOrBlank()) return url
            if (!resolvedUrl.startsWith("http")) {
                resolvedUrl = "https:$resolvedUrl"
            }
            resolvedUrl
        } catch (e: Exception) {
            Log.w(TAG, "Redirect resolution failed, using original URL", e)
            url
        }
    }

    private fun extractFromUniversalData(html: String): TikTokVideoData? {
        try {
            val pattern = Pattern.compile(
                """<script\s+id="__UNIVERSAL_DATA_FOR_REHYDRATION__"\s+type="application/json">(.*?)</script>""",
                Pattern.DOTALL or Pattern.CASE_INSENSITIVE
            )
            val matcher = pattern.matcher(html)
            if (!matcher.find()) return null

            val jsonStr = matcher.group(1) ?: return null

            val rootMapType = Types.newParameterizedType(
                Map::class.java,
                String::class.java,
                Any::class.java
            )
            val rootAdapter = moshi.adapter<Map<String, Any?>>(rootMapType)
            val root = rootAdapter.fromJson(jsonStr) ?: return null

            val defaultScope = root["__DEFAULT_SCOPE__"] as? Map<*, *> ?: return null
            val videoDetail = defaultScope["webapp.video-detail"] as? Map<*, *> ?: return null
            val itemInfo = videoDetail["itemInfo"] as? Map<*, *> ?: return null
            val itemStruct = itemInfo["itemStruct"] as? Map<*, *> ?: return null

            if (itemStruct == null) return null

            return parseItemStruct(itemStruct)
        } catch (e: Exception) {
            Log.w(TAG, "extractFromUniversalData failed", e)
            return null
        }
    }

    private fun extractFromInitProps(html: String): TikTokVideoData? {
        try {
            val pattern = Pattern.compile(
                """<script\s+id="__INIT_PROPS__"\s+type="application/json">(.*?)</script>""",
                Pattern.DOTALL or Pattern.CASE_INSENSITIVE
            )
            val matcher = pattern.matcher(html)
            if (!matcher.find()) return null

            val jsonStr = matcher.group(1) ?: return null

            val rootMapType = Types.newParameterizedType(
                Map::class.java,
                String::class.java,
                Any::class.java
            )
            val rootAdapter = moshi.adapter<Map<String, Any?>>(rootMapType)
            val root = rootAdapter.fromJson(jsonStr) ?: return null

            val page = root["page"] as? Map<*, *> ?: return null
            val videoData = page["videoData"] as? Map<*, *>
                ?: root["videoData"] as? Map<*, *>
                ?: return null

            val videoId = (videoData["id"] ?: videoData["video_id"])?.toString() ?: ""
            val title = (videoData["title"] ?: videoData["desc"])?.toString() ?: ""
            val author = (videoData["author"] as? Map<*, *>)?.get("nickname")?.toString()
                ?: videoData["author_name"]?.toString()
                ?: ""
            val authorId = (videoData["author"] as? Map<*, *>)?.get("unique_id")?.toString()
                ?: videoData["author_id"]?.toString()
                ?: ""
            val thumbnail = (videoData["cover"] as? Map<*, *>)?.let { urlList(it) }
                ?: videoData["cover"]?.toString()
                ?: ""
            val duration = (videoData["duration"] as? Double)?.toLong()
                ?: videoData["duration"]?.toString()?.toLongOrNull()
                ?: 0L

            val videoUrl = extractVideoUrlFromMap(videoData, "play_addr")
            val videoUrlNoWatermark = extractVideoUrlFromMap(videoData, "download_addr")
                ?: extractVideoUrlFromMap(videoData, "play_without_watermark")
            val audioUrl = extractAudioUrlFromMap(videoData)

            if (videoUrl == null && videoUrlNoWatermark == null) return null

            return TikTokVideoData(
                id = videoId,
                title = title,
                author = author,
                authorId = authorId,
                thumbnail = thumbnail,
                duration = duration,
                videoUrl = videoUrl,
                videoUrlNoWatermark = videoUrlNoWatermark,
                audioUrl = audioUrl
            )
        } catch (e: Exception) {
            Log.w(TAG, "extractFromInitProps failed", e)
            return null
        }
    }

    private fun extractFromVideoUrlPattern(html: String): TikTokVideoData? {
        try {
            val urlPattern = Pattern.compile(
                """https?://[a-z0-9]+(?:-wa)?\.tiktok\.com[^"']+?\.(?:mp4|m4a)[^"'\s]*""",
                Pattern.CASE_INSENSITIVE
            )
            val matcher = urlPattern.matcher(html)
            val urls = mutableSetOf<String>()
            while (matcher.find()) {
                urls.add(matcher.group())
            }

            if (urls.isEmpty()) return null

            val videoUrl = urls.firstOrNull { !it.contains("music") && !it.contains("audio") }
            val audioUrl = urls.firstOrNull { it.contains("music") || it.contains("audio") }

            val authorPattern = Pattern.compile(
                """@(\w+)""",
                Pattern.CASE_INSENSITIVE
            )
            val authorMatcher = authorPattern.matcher(html)
            val author = if (authorMatcher.find()) "@${authorMatcher.group(1)}" else ""

            val titlePattern = Pattern.compile(
                """<title[^>]*>(.*?)</title>""",
                Pattern.DOTALL or Pattern.CASE_INSENSITIVE
            )
            val titleMatcher = titlePattern.matcher(html)
            val title = if (titleMatcher.find()) {
                titleMatcher.group(1)?.trim()?.replace(" - TikTok", "")?.trim() ?: ""
            } else ""

            if (videoUrl == null) return null

            return TikTokVideoData(
                id = "",
                title = title,
                author = author,
                authorId = "",
                thumbnail = "",
                duration = 0L,
                videoUrl = videoUrl,
                videoUrlNoWatermark = null,
                audioUrl = audioUrl
            )
        } catch (e: Exception) {
            Log.w(TAG, "extractFromVideoUrlPattern failed", e)
            return null
        }
    }

    private fun extractFromMetaTags(html: String): TikTokVideoData? {
        try {
            // Try to find video URL in og:video or meta tags
            val videoPattern = Pattern.compile(
                """<meta\s+[^>]*property=["']og:video["'][^>]*content=["']([^"']+)["']""",
                Pattern.CASE_INSENSITIVE
            )
            val videoMatcher = videoPattern.matcher(html)
            val videoUrl = if (videoMatcher.find()) videoMatcher.group(1) else null

            val thumbnailPattern = Pattern.compile(
                """<meta\s+[^>]*property=["']og:image["'][^>]*content=["']([^"']+)["']""",
                Pattern.CASE_INSENSITIVE
            )
            val thumbnailMatcher = thumbnailPattern.matcher(html)
            val thumbnail = if (thumbnailMatcher.find()) thumbnailMatcher.group(1) else ""

            val titlePattern = Pattern.compile(
                """<meta\s+[^>]*property=["']og:title["'][^>]*content=["']([^"']+)["']""",
                Pattern.CASE_INSENSITIVE
            )
            val titleMatcher = titlePattern.matcher(html)
            val title = if (titleMatcher.find()) titleMatcher.group(1) ?: "" else ""

            // Extract author from URL
            val authorPattern = Pattern.compile(
                """@(\w+)""",
                Pattern.CASE_INSENSITIVE
            )
            val authorMatcher = authorPattern.matcher(html)
            val author = if (authorMatcher.find()) "@${authorMatcher.group(1)}" else ""

            if (videoUrl == null) return null

            return TikTokVideoData(
                id = "",
                title = title,
                author = author,
                authorId = "",
                thumbnail = thumbnail,
                duration = 0L,
                videoUrl = videoUrl,
                videoUrlNoWatermark = null,
                audioUrl = null
            )
        } catch (e: Exception) {
            Log.w(TAG, "extractFromMetaTags failed", e)
            return null
        }
    }

    private fun parseItemStruct(itemStruct: Map<*, *>): TikTokVideoData? {
        val videoId = (itemStruct["id"] ?: itemStruct["video_id"])?.toString() ?: ""
        val title = (itemStruct["desc"] ?: itemStruct["title"])?.toString() ?: ""

        val author = (itemStruct["author"] as? Map<*, *>)?.let { authorMap ->
            authorMap["nickname"]?.toString() ?: ""
        } ?: ""
        val authorId = (itemStruct["author"] as? Map<*, *>)?.let { authorMap ->
            authorMap["uniqueId"]?.toString() ?: ""
        } ?: ""

        val video = itemStruct["video"] as? Map<*, *> ?: return null

        val thumbnail = (video["cover"] as? Map<*, *>)?.let { cover ->
            urlList(cover)
        } ?: (video["originCover"] as? Map<*, *>)?.let { cover ->
            urlList(cover)
        } ?: ""

        val duration = (video["duration"] as? Double)?.toLong()
            ?: video["duration"]?.toString()?.toLongOrNull()
            ?: 0L

        val videoUrl = extractVideoUrlFromMap(video, "playAddr")
        val videoUrlNoWatermark = extractVideoUrlFromMap(video, "downloadAddr")
        val audioUrl = extractAudioUrlFromMap(video)

        if (videoUrl == null && videoUrlNoWatermark == null) return null

        return TikTokVideoData(
            id = videoId,
            title = title,
            author = author,
            authorId = authorId,
            thumbnail = thumbnail,
            duration = duration,
            videoUrl = videoUrl,
            videoUrlNoWatermark = videoUrlNoWatermark,
            audioUrl = audioUrl
        )
    }

    private fun extractVideoUrlFromMap(map: Map<*, *>, key: String): String? {
        val addr = map[key] as? Map<*, *> ?: return null
        val urlList = addr["url_list"] as? List<*> ?: return null
        return urlList
            .filterIsInstance<String>()
            .firstOrNull { it.isNotBlank() }
    }

    private fun extractAudioUrlFromMap(map: Map<*, *>): String? {
        val music = map["music"] as? Map<*, *> ?: return null
        return extractVideoUrlFromMap(music, "playUrl")
            ?: extractVideoUrlFromMap(music, "play_url")
    }

    private fun urlList(cover: Map<*, *>): String {
        return (cover["url_list"] as? List<*>)
            ?.filterIsInstance<String>()
            ?.firstOrNull { it.isNotBlank() }
            ?: ""
    }
}
