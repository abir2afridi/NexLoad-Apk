package com.example.ui.screens.browser

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.download.VideoExtractor
import com.example.ui.viewmodel.DetectedMedia
import com.example.ui.viewmodel.MainViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun isDirectMediaUrl(url: String): Boolean {
    val path = try { Uri.parse(url).path?.lowercase() ?: url.lowercase() } catch (_: Exception) { url.lowercase() }
    return path.endsWith(".mp4") || path.endsWith(".m3u8") || path.endsWith(".webm") ||
           path.endsWith(".mkv") || path.endsWith(".avi") || path.endsWith(".mov") ||
           path.endsWith(".flv") || path.endsWith(".3gp") || path.endsWith(".ts") ||
           path.endsWith(".mp3") || path.endsWith(".m4a") || path.endsWith(".aac") ||
           path.endsWith(".ogg") || path.endsWith(".wav") || path.endsWith(".flac") ||
           path.endsWith(".wma") || path.endsWith(".opus")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserMediaSheet(
    detectedMedia: List<DetectedMedia>,
    showMediaSheet: Boolean,
    onDismiss: () -> Unit,
    onDownloadDialog: (url: String) -> Unit,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var resolvingMedia by remember { mutableStateOf(false) }

    if (showMediaSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Detected Media",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${detectedMedia.size} media item${if (detectedMedia.size != 1) "s" else ""} found",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (detectedMedia.size > 1) {
                            FilledTonalButton(
                                onClick = {
                                    resolvingMedia = true
                                    viewModel.viewModelScope.launch {
                                        val mediaItems = detectedMedia.toList()
                                        var successCount = 0
                                        try {
                                            for (item in mediaItems) {
                                                try {
                                                    var resolvedHeaders: Map<String, String>? = null
                                                    val resolved = withContext(Dispatchers.IO) {
                                                        if (isDirectMediaUrl(item.url)) {
                                                            item.url
                                                        } else {
                                                            val result = try {
                                                                VideoExtractor.extract(item.url)
                                                            } catch (e: Throwable) {
                                                                Log.e("BrowserMediaSheet", "Extract crash for ${item.url}", e)
                                                                null
                                                            }
                                                            val data = result?.getOrNull()
                                                            resolvedHeaders = data?.httpHeaders
                                                            data?.videoUrlNoWatermark ?: data?.videoUrl ?: item.url
                                                        }
                                                    }
                                                    viewModel.addDownload(resolved, item.title, customHeaders = resolvedHeaders, sourceUrl = item.url)
                                                    successCount++
                                                } catch (e: CancellationException) {
                                                    throw e
                                                } catch (e: Throwable) {
                                                    Log.e("BrowserMediaSheet", "Failed to extract ${item.url}", e)
                                                }
                                            }
                                        } catch (e: CancellationException) {
                                            throw e
                                        } catch (e: Throwable) {
                                            Log.e("BrowserMediaSheet", "Download All failed", e)
                                        }
                                        resolvingMedia = false
                                        Toast.makeText(context, "$successCount/${mediaItems.size} downloads queued!", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                },
                                enabled = !resolvingMedia,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("All", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Surface(
                            onClick = { viewModel.clearDetectedMedia(); onDismiss() },
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear all",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(8.dp).size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(detectedMedia) { media ->
                        val isAudio = media.url.lowercase().run { contains(".mp3") || contains(".m4a") || contains(".wav") }
                        val domain = try { Uri.parse(media.url).host?.removePrefix("www.") ?: "unknown" } catch (_: Exception) { "unknown" }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (isAudio) MaterialTheme.colorScheme.tertiaryContainer
                                                else MaterialTheme.colorScheme.primaryContainer
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isAudio) Icons.Default.Audiotrack else Icons.Default.PlayCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = if (isAudio) MaterialTheme.colorScheme.onTertiaryContainer
                                                   else MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = media.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isAudio) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                                                       else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                            ) {
                                                Text(
                                                    text = if (isAudio) "Audio" else "Video",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (isAudio) MaterialTheme.colorScheme.onTertiaryContainer
                                                           else MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = domain,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = media.url,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { onDownloadDialog(media.url) },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Download", style = MaterialTheme.typography.labelLarge)
                                    }
                                    Surface(
                                        onClick = {
                                            resolvingMedia = true
                                            viewModel.viewModelScope.launch {
                                                var success = false
                                                try {
                                                    var resolvedHeaders: Map<String, String>? = null
                                                    val resolved = withContext(Dispatchers.IO) {
                                                        if (isDirectMediaUrl(media.url)) {
                                                            media.url
                                                        } else {
                                                            val result = try {
                                                                VideoExtractor.extract(media.url)
                                                            } catch (e: Throwable) {
                                                                Log.e("BrowserMediaSheet", "Extract crash for ${media.url}", e)
                                                                null
                                                            }
                                                            val data = result?.getOrNull()
                                                            resolvedHeaders = data?.httpHeaders
                                                            data?.audioUrl ?: data?.videoUrlNoWatermark ?: data?.videoUrl ?: media.url
                                                        }
                                                    }
                                                    viewModel.addDownload(resolved, media.title, isAudioOnly = true, customHeaders = resolvedHeaders, sourceUrl = media.url)
                                                    success = true
                                                } catch (e: CancellationException) {
                                                    throw e
                                                } catch (e: Throwable) {
                                                    Log.e("BrowserMediaSheet", "Failed to extract audio from ${media.url}", e)
                                                    Toast.makeText(context, "Extraction failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                                }
                                                resolvingMedia = false
                                                if (success) {
                                                    Toast.makeText(context, "Audio extraction queued!", Toast.LENGTH_SHORT).show()
                                                    onDismiss()
                                                }
                                            }
                                        },
                                        enabled = !resolvingMedia,
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.MusicNote,
                                                contentDescription = "Audio only",
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Surface(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(media.url))
                                            Toast.makeText(context, "URL copied", Toast.LENGTH_SHORT).show()
                                        },
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = "Copy link",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
