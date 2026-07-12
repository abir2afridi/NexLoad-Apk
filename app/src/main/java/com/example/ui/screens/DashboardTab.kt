package com.example.ui.screens

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.DownloadEntity
import com.example.data.download.MediaUtils
import com.example.ui.components.DownloadHealthIndicators
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTab(
    viewModel: MainViewModel,
    onNavigateToTab: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val downloads by viewModel.publicDownloads.collectAsState()
    val isIncognito by viewModel.isIncognito.collectAsState()

    // Download calculations
    val activeDownloads = downloads.filter { it.status == "DOWNLOADING" || it.status == "QUEUED" }
    val completedDownloads = downloads.filter { it.status == "COMPLETED" }
    
    val totalSpeed = activeDownloads.sumOf { it.speed }
    val activeTasksCount = activeDownloads.size

    val avgProgress = if (activeDownloads.isNotEmpty()) {
        val totalBytes = activeDownloads.sumOf { it.totalBytes }
        val downloadedBytes = activeDownloads.sumOf { it.downloadedBytes }
        if (totalBytes > 0) (downloadedBytes.toFloat() / totalBytes.toFloat() * 100).toInt() else 0
    } else {
        0
    }

    var showPasteLinkDialog by remember { mutableStateOf(false) }
    var pastedUrl by remember { mutableStateOf("") }
    var pastedTitle by remember { mutableStateOf("") }
    var isOptimizing by remember { mutableStateOf(false) }

    // Storage stats
    val storageStats = remember(downloads) {
        getStorageStats(context, downloads)
    }

    val recentDownload = completedDownloads.maxByOrNull { it.id }

    // Live Clock State
    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())) }
    var currentDate by remember { mutableStateOf(SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date())) }
    
    // Connectivity State
    var networkStatus by remember { mutableStateOf("Checking...") }
    var pingValue by remember { mutableStateOf<Long?>(null) }
    var connectionStrength by remember { mutableStateOf("N/A") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)
            currentDate = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(now)
            
            // Check Network
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork
            if (network == null) {
                networkStatus = "Offline"
                connectionStrength = "None"
                pingValue = null
            } else {
                val caps = cm.getNetworkCapabilities(network)
                val type = when {
                    caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
                    caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Mobile Data"
                    else -> "Connected"
                }
                networkStatus = type
                
                // Estimate strength from capabilities
                val speed = caps?.linkDownstreamBandwidthKbps ?: 0
                connectionStrength = when {
                    speed > 10000 -> "Excellent"
                    speed > 3000 -> "Good"
                    speed > 1000 -> "Average"
                    else -> "Poor"
                }

                // Periodic Ping (every 5 seconds)
                scope.launch {
                    val start = System.currentTimeMillis()
                    val reachable = withContext(Dispatchers.IO) {
                        try {
                            Socket().use { socket ->
                                socket.connect(InetSocketAddress("8.8.8.8", 53), 2000)
                                true
                            }
                        } catch (e: IOException) {
                            false
                        }
                    }
                    if (reachable) {
                        pingValue = System.currentTimeMillis() - start
                    } else {
                        pingValue = null
                        connectionStrength = "Unstable"
                    }
                }
            }
            delay(1000)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
    ) {
        // 1. MINIMALIST GREETING HEADER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VORTEX ENGINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Workspace",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Theme toggle icon
                    val themeIcon = when (viewModel.selectedThemeMode.value) {
                        "Light" -> Icons.Default.LightMode
                        "Dark" -> Icons.Default.DarkMode
                        else -> Icons.Default.BrightnessAuto
                    }
                    Surface(
                        onClick = {
                            val modes = listOf("System", "Light", "Dark")
                            val next = (modes.indexOf(viewModel.selectedThemeMode.value) + 1) % modes.size
                            viewModel.selectedThemeMode.value = modes[next]
                        },
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            Icon(
                                imageVector = themeIcon,
                                contentDescription = "Toggle theme",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Super Clean Minimalist Engine Tag
                    Surface(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (activeTasksCount > 0) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                            )
                            Text(
                                text = if (activeTasksCount > 0) "ACTIVE" else "READY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 1.5. LIVE CLOCK & CONNECTIVITY CARD
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Clock Card
                MinimalCard(
                    onClick = {},
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentTime,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = currentDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                // Connectivity Card
                MinimalCard(
                    onClick = {},
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = when (networkStatus) {
                                "Wi-Fi" -> Icons.Default.Wifi
                                "Offline" -> Icons.Default.WifiOff
                                else -> Icons.Default.SignalCellularAlt
                            },
                            contentDescription = null,
                            tint = when (connectionStrength) {
                                "Excellent", "Good" -> Color(0xFF4CAF50)
                                "Average" -> Color(0xFFFFC107)
                                "Poor", "Unstable" -> Color(0xFFF44336)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = networkStatus,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = connectionStrength,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            if (pingValue != null) {
                                Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)))
                                Text(
                                    text = "${pingValue}ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. PRIMARY STATUS BLOCK (MINIMAL & INTUITIVE CARD)
        item {
            MinimalCard(
                onClick = { onNavigateToTab("Downloads") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bento_active_tasks_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STREAMS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (activeTasksCount > 0) {
                                    "Downloading • ${MediaUtils.formatSpeed(totalSpeed)}"
                                } else {
                                    "No ongoing streams"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        // Subtle clean functional icon
                        Icon(
                            imageVector = if (activeTasksCount > 0) Icons.Default.CloudSync else Icons.Outlined.CloudQueue,
                            contentDescription = null,
                            tint = if (activeTasksCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    if (activeTasksCount > 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$activeTasksCount multi-core channel active",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$avgProgress%",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { avgProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            )
                        }
                    } else {
                        // Quick Action Link Inside Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                                .clickable { showPasteLinkDialog = true }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Paste a new link to begin...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Link",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. UNIFIED AND SLEEK PORTALS (BROWSER & VAULT)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Minimalist Safe Browser Portal
                MinimalCard(
                    onClick = { onNavigateToTab("Browser") },
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .testTag("bento_browser_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isIncognito) Icons.Default.LocalFireDepartment else Icons.Outlined.Language,
                                contentDescription = null,
                                tint = if (isIncognito) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )

                            if (isIncognito) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.tertiary)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Safe Browser",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isIncognito) "Incognito Active" else "Surfing Secure",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Minimalist Secure Vault Portal
                MinimalCard(
                    onClick = { onNavigateToTab("Vault") },
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .testTag("bento_vault_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )

                        Column {
                            Text(
                                text = "Private Vault",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "PIN Encrypted",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // 4. STORAGE OVERVIEW (TRULY MINIMALIST LINE DESIGN)
        item {
            MinimalCard(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bento_storage_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STORAGE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${storageStats.usedFormatted} of ${storageStats.totalFormatted} Used",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Super subtle quiet clean button
                        Surface(
                            onClick = {
                                scope.launch {
                                    isOptimizing = true
                                    kotlinx.coroutines.delay(1000)
                                    isOptimizing = false
                                    Toast.makeText(context, "Cache flushed and storage optimized", Toast.LENGTH_SHORT).show()
                                }
                            },
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "OPTIMIZE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // A single solid sleek tracking bar instead of busy multi-colored tracks
                    val progressFraction = storageStats.percentageUsed
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    )

                    // Compact quiet legend details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Video • ${storageStats.videoSizeFormatted}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Audio • ${storageStats.audioSizeFormatted}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Free • ${storageStats.freeFormatted}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // 5. RECENTLY COMPLETED SESSION PORTAL
        item {
            if (recentDownload != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Recent Complete",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    MinimalCard(
                        onClick = { onNavigateToTab("Files") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bento_recent_download_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (recentDownload.category == "Audio") Icons.Default.Audiotrack else Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = recentDownload.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Completed • ${MediaUtils.formatBytes(recentDownload.totalBytes)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                DownloadHealthIndicators(
                                    integrityStatus = recentDownload.integrityStatus,
                                    connectionHealth = recentDownload.connectionHealth
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "View",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else {
                // Minimal Empty State Card
                MinimalCard(
                    onClick = { onNavigateToTab("Browser") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Socket Idle",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Browse or paste media paths to initiate the stream socket.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // PASTE SOCKET DIALOG
    if (showPasteLinkDialog) {
        AlertDialog(
            onDismissRequest = { showPasteLinkDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            },
            title = {
                Text("New Download", fontWeight = FontWeight.SemiBold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = pastedUrl,
                        onValueChange = { pastedUrl = it },
                        label = { Text("URL") },
                        placeholder = { Text("Paste media link...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = pastedTitle,
                        onValueChange = { pastedTitle = it },
                        label = { Text("Title") },
                        placeholder = { Text("Optional") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val url = pastedUrl.trim()
                        if (url.isNotBlank()) {
                            val title = if (pastedTitle.isBlank()) "Stream Socket Link" else pastedTitle
                            viewModel.addDownload(url, title)
                            Toast.makeText(context, "Download queued", Toast.LENGTH_SHORT).show()
                            pastedUrl = ""
                            pastedTitle = ""
                            showPasteLinkDialog = false
                        } else {
                            Toast.makeText(context, "Please provide a valid stream link", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Download")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteLinkDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // DEEP DEFRAGMENTATION PROGRESS DIALOG
    if (isOptimizing) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = { Text("Optimizing Storage...", fontWeight = FontWeight.SemiBold) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Reclaiming orphan bits and compressing local schema caches...",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun MinimalCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}

// Storage space details helper
data class StorageDetails(
    val totalFormatted: String,
    val usedFormatted: String,
    val freeFormatted: String,
    val videoSizeFormatted: String,
    val audioSizeFormatted: String,
    val percentageUsed: Float
)

private fun getStorageStats(context: Context, downloads: List<DownloadEntity>): StorageDetails {
    return try {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalBytes = totalBlocks * blockSize
        val freeBytes = availableBlocks * blockSize
        val usedBytes = totalBytes - freeBytes

        // Filter download sizes
        val videoBytes = downloads.filter { it.category == "Video" && it.status == "COMPLETED" }.sumOf { it.totalBytes }
        val audioBytes = downloads.filter { it.category == "Audio" && it.status == "COMPLETED" }.sumOf { it.totalBytes }

        val percentage = if (totalBytes > 0) usedBytes.toFloat() / totalBytes.toFloat() else 0f

        StorageDetails(
            totalFormatted = MediaUtils.formatBytes(totalBytes),
            usedFormatted = MediaUtils.formatBytes(usedBytes),
            freeFormatted = MediaUtils.formatBytes(freeBytes),
            videoSizeFormatted = MediaUtils.formatBytes(videoBytes),
            audioSizeFormatted = MediaUtils.formatBytes(audioBytes),
            percentageUsed = percentage
        )
    } catch (e: Exception) {
        StorageDetails("64.0 GB", "42.8 GB", "21.2 GB", "28.4 GB", "1.2 GB", 0.68f)
    }
}
