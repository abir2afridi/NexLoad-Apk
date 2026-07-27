package com.example.ui.screens.stream

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.data.download.TikTokVideoData
import com.example.data.download.VideoExtractor
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun StreamDownloadCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var linkText by remember { mutableStateOf("") }
    var extractedVideoInfo by remember { mutableStateOf<TikTokVideoData?>(null) }
    var isExtracting by remember { mutableStateOf(false) }
    var extractionError by remember { mutableStateOf<String?>(null) }
    var selectedDownloadType by remember { mutableIntStateOf(0) }
    var isDownloadingFromStream by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val faviconUrl = remember(linkText) {
                if (linkText.startsWith("http")) {
                    try {
                        val domain = android.net.Uri.parse(linkText).host
                        if (!domain.isNullOrBlank()) {
                            "https://www.google.com/s2/favicons?sz=64&domain=$domain"
                        } else null
                    } catch (e: Exception) { null }
                } else null
            }

            if (faviconUrl != null) {
                AsyncImage(
                    model = faviconUrl,
                    contentDescription = "Favicon",
                    modifier = Modifier.size(20.dp).clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            TextField(
                value = linkText,
                onValueChange = { linkText = it },
                modifier = Modifier
                    .weight(1f)
                    .focusable(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Paste link here...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            IconButton(
                onClick = {
                    val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                    val clipData = clip?.primaryClip
                    if (clipData != null && clipData.itemCount > 0) {
                        val text = clipData.getItemAt(0).text?.toString()
                        if (!text.isNullOrBlank()) {
                            linkText = text
                        }
                    }
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Paste",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            if (linkText.isNotBlank()) {
                IconButton(
                    onClick = { linkText = "" },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    if (extractionError != null) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                Text(
                    text = extractionError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { extractionError = null }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(14.dp))
                }
            }
        }
    }

    val hasLink = linkText.isNotBlank()
    Surface(
        onClick = {
            if (hasLink && !isExtracting) {
                isExtracting = true
                extractionError = null
                extractedVideoInfo = null
                scope.launch {
                    try {
                        val result = withContext(Dispatchers.IO) {
                            VideoExtractor.extract(linkText)
                        }
                        result.fold(
                            onSuccess = { data ->
                                extractedVideoInfo = data
                            },
                            onFailure = { e ->
                                extractionError = e.message ?: "Extraction failed"
                            }
                        )
                    } catch (e: Exception) {
                        extractionError = e.message ?: "Extraction failed"
                    } finally {
                        isExtracting = false
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (hasLink && !isExtracting) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
        border = if (!hasLink || isExtracting) BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isExtracting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (hasLink) MaterialTheme.colorScheme.onPrimary
                          else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isExtracting) "Analyzing..." else "Analyze",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (hasLink && !isExtracting) MaterialTheme.colorScheme.onPrimary
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
            )
        }
    }

    val info = extractedVideoInfo
    val hasAudio = info?.audioUrl?.isNullOrBlank() == false
    val hasVideo = info?.videoUrl?.isNullOrBlank() == false

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---- THUMBNAIL FRAME (always visible) ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp, max = 160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                if (info != null && info.thumbnail.isNotBlank()) {
                    AsyncImage(
                        model = info.thumbnail,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    val previewComposition by rememberLottieComposition(
                        LottieCompositionSpec.Url("https://lottie.host/e7c3629f-2bb1-4259-bf48-69d90b554aac/0ZOTo1G5Dc.lottie")
                    )
                    LottieAnimation(
                        composition = previewComposition,
                        iterations = LottieConstants.IterateForever,
                        speed = 1f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp, max = 160.dp)
                            .background(Color.LightGray)
                    )
                }
            }

            // ---- TITLE ----
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Title",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(64.dp)
                )
                Text(
                    text = if (info != null && info.title.isNotBlank()) info.title else "—",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (info != null) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            // ---- AUTHOR ----
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Author",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(64.dp)
                )
                Text(
                    text = if (info != null && info.author.isNotBlank()) info.author else "—",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (info != null) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            // ---- DURATION ----
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Duration",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(64.dp)
                )
                Text(
                    text = if (info != null && info.duration > 0) formatDuration(info.duration) else "—",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (info != null) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 2.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            // ---- OUTPUT ----
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Output",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(64.dp)
                )
                Text(
                    text = if (info != null) "Ready to download" else "Awaiting analysis...",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (info != null) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }

            // ---- QUALITY & FORMAT (only after successful extraction) ----
            if (info != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Auto", "HD").forEachIndexed { index, quality ->
                        Surface(
                            onClick = { },
                            shape = RoundedCornerShape(10.dp),
                            color = if (index == 0) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (index == 0) Icons.Default.PlayCircle else Icons.Default.VideoLibrary,
                                    contentDescription = null, modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(quality, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }

                if (hasAudio || hasVideo) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (hasVideo) {
                            Surface(
                                onClick = { selectedDownloadType = 0 },
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedDownloadType == 0) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = if (selectedDownloadType == 0) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.VideoFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Video", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                        if (hasAudio) {
                            Surface(
                                onClick = { selectedDownloadType = 1 },
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedDownloadType == 1) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = if (selectedDownloadType == 1) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Audiotrack, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Audio", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }

                // Download button
                Surface(
                    onClick = {
                        if (isDownloadingFromStream) return@Surface
                        isDownloadingFromStream = true
                        scope.launch {
                            try {
                                val downloadUrl = if (selectedDownloadType == 1) {
                                    info.audioUrl ?: info.videoUrl
                                } else {
                                    info.videoUrlNoWatermark ?: info.videoUrl
                                }
                                if (downloadUrl != null) {
                                    viewModel.addDownload(
                                        url = downloadUrl,
                                        suggestedTitle = info.title.ifBlank { "Video" },
                                        quality = "Auto",
                                        isAudioOnly = selectedDownloadType == 1,
                                        customHeaders = info.httpHeaders,
                                        sourceUrl = info.sourceUrl
                                    )
                                    Toast.makeText(context, "Download queued → view progress in Downloads tab", Toast.LENGTH_SHORT).show()
                                    extractedVideoInfo = null
                                } else {
                                    Toast.makeText(context, "No download URL available", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isDownloadingFromStream = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (selectedDownloadType == 1) Icons.Default.MusicNote else Icons.Default.Download,
                            contentDescription = null, modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDownloadingFromStream) "Starting..." else if (selectedDownloadType == 1) "Download Audio" else "Download Video",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // Clear / Dismiss button (always visible)
            Surface(
                onClick = {
                    extractedVideoInfo = null
                    extractionError = null
                    linkText = ""
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                    Text("Clear", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    if (seconds <= 0) return "00:00"
    val mins = seconds / 60
    val secs = seconds % 60
    return "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}
