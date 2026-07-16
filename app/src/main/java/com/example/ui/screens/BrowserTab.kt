package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.viewmodel.MainViewModel

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    val currentUrl by viewModel.currentWebUrl.collectAsState()
    val isIncognito by viewModel.isIncognito.collectAsState()
    val isTrackerBlocking by viewModel.isTrackerBlocking.collectAsState()
    val isHttpsOnly by viewModel.isHttpsOnly.collectAsState()
    val isForceDarkWeb by viewModel.isForceDarkWeb.collectAsState()
    val detectedMedia by viewModel.detectedMediaList.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val tabs by viewModel.tabs.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()

    var urlInput by remember { mutableStateOf(currentUrl) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var progressVal by remember { mutableStateOf(0) }
    var showBookmarksSheet by remember { mutableStateOf(false) }
    var showMediaSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showTabGallerySheet by remember { mutableStateOf(false) }

    // WebView instances per tab
    val webViewInstances = remember { mutableMapOf<String, WebView>() }

    // Clean up destroyed tabs
    LaunchedEffect(tabs) {
        val activeIds = tabs.map { it.id }.toSet()
        webViewInstances.keys
            .filter { it !in activeIds }
            .forEach { id ->
                webViewInstances[id]?.destroy()
                webViewInstances.remove(id)
            }
    }

    // Sync input bar when url state changes
    LaunchedEffect(currentUrl) {
        urlInput = currentUrl
    }

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 3.dp,
                color = if (isIncognito) Color(0xFF1C1B1F) else MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Navigation controls
                        IconButton(
                            onClick = { webViewInstance?.goBack() },
                            enabled = webViewInstance?.canGoBack() == true,
                            modifier = Modifier.size(34.dp).testTag("browser_back")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp))
                        }
                        IconButton(
                            onClick = { webViewInstance?.goForward() },
                            enabled = webViewInstance?.canGoForward() == true,
                            modifier = Modifier.size(34.dp).testTag("browser_forward")
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Forward", modifier = Modifier.size(20.dp))
                        }
                        IconButton(
                            onClick = { webViewInstance?.reload() },
                            modifier = Modifier.size(34.dp).testTag("browser_reload")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(20.dp))
                        }
                        IconButton(
                            onClick = { webViewInstance?.loadUrl("https://google.com") },
                            modifier = Modifier.size(34.dp).testTag("browser_home")
                        ) {
                            Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(20.dp))
                        }

                        // Address Bar
                        TextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .padding(horizontal = 4.dp)
                                .testTag("browser_address_bar"),
                            placeholder = { Text("Search...", style = MaterialTheme.typography.bodyMedium) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(24.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isHttpsOnly) Icons.Default.Lock else Icons.Default.Language,
                                    contentDescription = null,
                                    tint = if (isHttpsOnly) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyMedium,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    var destination = urlInput.trim()
                                    if (destination.isNotBlank()) {
                                        if (!destination.startsWith("http://") && !destination.startsWith("https://")) {
                                            if (destination.contains(".") && !destination.contains(" ")) {
                                                destination = "https://$destination"
                                            } else {
                                                destination = "https://google.com/search?q=$destination"
                                            }
                                        }
                                        webViewInstance?.loadUrl(destination)
                                    }
                                }
                            )
                        )

                        // Tab Gallery button
                        IconButton(
                            onClick = { showTabGallerySheet = true },
                            modifier = Modifier.size(34.dp).testTag("tab_gallery")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tab,
                                contentDescription = "Tabs",
                                tint = if (tabs.size > 1) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 3-dot overflow menu
                        var showOverflow by remember { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = { showOverflow = true },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showOverflow,
                                onDismissRequest = { showOverflow = false }
                            ) {
                                val isBookmarked = viewModel.isUrlBookmarked(currentUrl)
                                DropdownMenuItem(
                                    text = { Text(if (isBookmarked) "Remove Bookmark" else "Bookmark Page") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = null,
                                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else Color.Gray
                                        )
                                    },
                                    onClick = {
                                        viewModel.toggleBookmark(currentUrl, webViewInstance?.title ?: "Web Page")
                                        Toast.makeText(context, if (isBookmarked) "Bookmark removed" else "Page bookmarked", Toast.LENGTH_SHORT).show()
                                        showOverflow = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isTrackerBlocking) "Disable Tracker Blocker" else "Enable Tracker Blocker") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (isTrackerBlocking) Icons.Filled.Shield else Icons.Outlined.Shield,
                                            contentDescription = null,
                                            tint = if (isTrackerBlocking) Color(0xFF4CAF50) else Color.Gray
                                        )
                                    },
                                    onClick = {
                                        viewModel.isTrackerBlocking.value = !isTrackerBlocking
                                        Toast.makeText(context, "Tracker Blocker: " + if (!isTrackerBlocking) "ENABLED" else "DISABLED", Toast.LENGTH_SHORT).show()
                                        showOverflow = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isIncognito) "Disable Incognito" else "Enable Incognito") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (isIncognito) Icons.Filled.VisibilityOff else Icons.Outlined.Visibility,
                                            contentDescription = null,
                                            tint = if (isIncognito) MaterialTheme.colorScheme.primary else Color.Gray
                                        )
                                    },
                                    onClick = {
                                        val newState = !isIncognito
                                        viewModel.toggleActiveTabIncognito()
                                        if (newState) {
                                            CookieManager.getInstance().removeAllCookies(null)
                                            webViewInstance?.clearCache(true)
                                            webViewInstance?.clearHistory()
                                            Toast.makeText(context, "Incognito Mode Activated", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Standard Mode Activated", Toast.LENGTH_SHORT).show()
                                        }
                                        showOverflow = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Bookmarks List") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Bookmarks, contentDescription = null, tint = Color.Gray)
                                    },
                                    onClick = {
                                        showBookmarksSheet = true
                                        showOverflow = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("History") },
                                    leadingIcon = {
                                        Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray)
                                    },
                                    onClick = {
                                        showHistorySheet = true
                                        showOverflow = false
                                    }
                                )
                            }
                        }
                    }

                    // Loading Progress Indicator
                    if (progressVal in 1..99) {
                        LinearProgressIndicator(
                            progress = { progressVal / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .padding(top = 2.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (detectedMedia.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showMediaSheet = true },
                    modifier = Modifier
                        .padding(bottom = 90.dp)
                        .testTag("media_detected_fab"),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlayCircleFilled, contentDescription = "Detected Media", tint = Color.Red)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${detectedMedia.size} Media Found",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            // Multi-tab WebView container
            AndroidView(
                factory = { ctx ->
                    FrameLayout(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { container ->
                    val activeId = activeTabId ?: return@AndroidView
                    container.removeAllViews()

                    val webView = webViewInstances.getOrPut(activeId) {
                        WebView(container.context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.databaseEnabled = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true

                            if (isForceDarkWeb && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                settings.forceDark = WebSettings.FORCE_DARK_ON
                            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                settings.forceDark = WebSettings.FORCE_DARK_OFF
                            }

                            addJavascriptInterface(object {
                                @JavascriptInterface
                                fun postMedia(url: String, title: String) {
                                    viewModel.addDetectedMedia(url, title)
                                }
                            }, "MediaScanner")

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    viewModel.clearDetectedMedia()
                                    if (url != null) {
                                        viewModel.updateActiveTabUrl(url)
                                    }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    view?.let { viewModel.updateActiveTabTitle(it.title ?: "") }
                                    view?.evaluateJavascript("""
                                        (function() {
                                            var style = document.createElement('style');
                                            style.innerHTML = 'body { padding-bottom: 120px !important; }';
                                            document.head.appendChild(style);
                                            
                                            function scan() {
                                                var urls = [];
                                                var videos = document.getElementsByTagName('video');
                                                for (var i = 0; i < videos.length; i++) {
                                                    var src = videos[i].src || videos[i].getAttribute('data-src') || videos[i].getAttribute('data-url') || '';
                                                    if (src && !src.startsWith('blob:') && !src.startsWith('data:')) urls.push({url: src, title: document.title || 'Video'});
                                                    var sources = videos[i].getElementsByTagName('source');
                                                    for (var j = 0; j < sources.length; j++) {
                                                        var s = sources[j].src || sources[j].getAttribute('data-src') || '';
                                                        if (s && !s.startsWith('blob:') && !s.startsWith('data:')) urls.push({url: s, title: document.title || 'Video'});
                                                    }
                                                }
                                                var metas = document.querySelectorAll('meta[property="og:video"], meta[property="og:video:url"], meta[property="og:video:secure_url"], meta[name="twitter:player"]');
                                                for (var i = 0; i < metas.length; i++) {
                                                    var c = metas[i].content;
                                                    if (c && c.indexOf('http') === 0) urls.push({url: c, title: document.title || 'Video'});
                                                }
                                                var iframes = document.querySelectorAll('iframe[src*="youtube"], iframe[src*="vimeo"], iframe[src*="tiktok"], iframe[src*="instagram"]');
                                                for (var i = 0; i < iframes.length; i++) {
                                                    var s = iframes[i].src;
                                                    if (s) urls.push({url: s, title: document.title || 'Embed'});
                                                }
                                                var links = document.getElementsByTagName('a');
                                                for (var i = 0; i < links.length; i++) {
                                                    var href = links[i].href;
                                                    if (href && (href.indexOf('.mp4') !== -1 || href.indexOf('.mp3') !== -1 || href.indexOf('.m4a') !== -1 || href.indexOf('.webm') !== -1 || href.indexOf('.mov') !== -1 || href.indexOf('.avi') !== -1) && href.indexOf('blob:') !== 0) {
                                                        urls.push({url: href, title: links[i].innerText || document.title || 'Media File'});
                                                    }
                                                }
                                                var imgs = document.querySelectorAll('img[src*="media"], img[src*="cdn"], img[src*="video"]');
                                                for (var i = 0; i < imgs.length; i++) {
                                                    var s = imgs[i].src;
                                                    if (s && (s.indexOf('.jpg') !== -1 || s.indexOf('.png') || s.indexOf('.webp') !== -1)) urls.push({url: s, title: document.title || 'Image'});
                                                }
                                                var embeds = document.querySelectorAll('[data-media-url], [data-video-url], [data-video-src], [data-mp4]');
                                                for (var i = 0; i < embeds.length; i++) {
                                                    var attr = embeds[i].getAttribute('data-media-url') || embeds[i].getAttribute('data-video-url') || embeds[i].getAttribute('data-video-src') || embeds[i].getAttribute('data-mp4') || '';
                                                    if (attr && attr.indexOf('http') === 0) urls.push({url: attr, title: document.title || 'Media'});
                                                }
                                                var seen = {};
                                                for (var i = 0; i < urls.length; i++) {
                                                    var key = urls[i].url;
                                                    if (!seen[key]) { seen[key] = true; window.MediaScanner.postMedia(urls[i].url, urls[i].title); }
                                                }
                                            }
                                            scan();
                                            setTimeout(function() { scan(); }, 1500);
                                            setTimeout(function() { scan(); }, 4000);
                                        })();
                                    """.trimIndent(), null)
                                }

                                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                                    val urlStr = request?.url?.toString() ?: ""
                                    if (isTrackerBlocking) {
                                        val blockedKeywords = listOf(
                                            "doubleclick.net", "ads.", "analytics", "telemetry",
                                            "google-analytics", "facebook.com/tr", "adnxs.com", "taboola"
                                        )
                                        if (blockedKeywords.any { urlStr.contains(it, ignoreCase = true) }) {
                                            return WebResourceResponse("text/plain", "UTF-8", null)
                                        }
                                    }
                                    if (urlStr.contains(".mp4") || urlStr.contains(".m3u8") || urlStr.contains(".ts?") || 
                                        urlStr.contains(".webm") || urlStr.contains(".mov?") || urlStr.contains(".avi?")) {
                                        val title = request?.requestHeaders?.get("Referer")?.let { 
                                            it.substringAfterLast("/").substringBefore("?").take(30) 
                                        } ?: "Stream"
                                        viewModel.addDetectedMedia(urlStr, title)
                                    }
                                    return super.shouldInterceptRequest(view, request)
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    super.onProgressChanged(view, newProgress)
                                    progressVal = newProgress
                                }
                            }

                            val tabData = tabs.find { it.id == activeId }
                            loadUrl(tabData?.url ?: currentUrl)
                            webViewInstance = this
                        }
                    }

                    container.addView(webView, FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    ))
                }
            )
        }

        // BOOKMARKS SHEET
        if (showBookmarksSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBookmarksSheet = false },
                modifier = Modifier.testTag("bookmarks_bottom_sheet")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Your Bookmarks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    if (bookmarks.isEmpty()) {
                        Text(
                            text = "No bookmarks saved yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            items(bookmarks) { b ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            webViewInstance?.loadUrl(b.url)
                                            showBookmarksSheet = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(b.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(b.url, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // HISTORY SHEET
        if (showHistorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showHistorySheet = false },
                modifier = Modifier.testTag("history_bottom_sheet")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Browsing History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val historyList = remember(webViewInstance) {
                        val wv = webViewInstance
                        if (wv != null) {
                            val bfl = wv.copyBackForwardList()
                            val items = mutableListOf<android.webkit.WebHistoryItem>()
                            for (i in 0 until bfl.size) {
                                bfl.getItemAtIndex(i)?.let { items.add(it) }
                            }
                            items
                        } else {
                            emptyList()
                        }
                    }

                    if (historyList.isEmpty()) {
                        Text(
                            text = "No browsing history.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            items(historyList) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            webViewInstance?.loadUrl(item.url)
                                            showHistorySheet = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            item.title.ifEmpty { "Untitled" },
                                            style = MaterialTheme.typography.bodyLarge,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            item.url,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // TAB GALLERY SHEET
        if (showTabGallerySheet) {
            ModalBottomSheet(
                onDismissRequest = { showTabGallerySheet = false },
                modifier = Modifier.testTag("tab_gallery_sheet")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Tab Gallery",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val normalTabs = tabs.filter { !it.isIncognito }
                    val privateTabs = tabs.filter { it.isIncognito }

                    if (normalTabs.isNotEmpty()) {
                        Text(
                            text = "Normal",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                        LazyColumn(modifier = Modifier.heightIn(max = 180.dp)) {
                            items(normalTabs) { tab ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clickable {
                                            viewModel.switchTab(tab.id)
                                            showTabGallerySheet = false
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (tab.id == activeTabId) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Language, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                tab.title.ifEmpty { "New Tab" },
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                tab.url,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (tabs.size > 1) {
                                            IconButton(
                                                onClick = { viewModel.closeTab(tab.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Close tab", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (privateTabs.isNotEmpty()) {
                        Text(
                            text = "Private",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                        LazyColumn(modifier = Modifier.heightIn(max = 180.dp)) {
                            items(privateTabs) { tab ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clickable {
                                            viewModel.switchTab(tab.id)
                                            showTabGallerySheet = false
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (tab.id == activeTabId) MaterialTheme.colorScheme.tertiaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.VisibilityOff, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                tab.title.ifEmpty { "New Tab" },
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                "Private Browsing",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray,
                                                maxLines = 1
                                            )
                                        }
                                        if (tabs.size > 1) {
                                            IconButton(
                                                onClick = { viewModel.closeTab(tab.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Close tab", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.addTab(isIncognito = false)
                                showTabGallerySheet = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Tab")
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.addTab(isIncognito = true)
                                showTabGallerySheet = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.VisibilityOff, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Private")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // DETECTED MEDIA BOTTOM SHEET
        if (showMediaSheet) {
            ModalBottomSheet(
                onDismissRequest = { showMediaSheet = false },
                modifier = Modifier.testTag("media_bottom_sheet"),
                dragHandle = { BottomSheetDefaults.DragHandle() },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color(0xFFFFD600),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Detected Media",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Surface(
                            onClick = { viewModel.clearDetectedMedia(); showMediaSheet = false },
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
                    
                    Text(
                        text = "${detectedMedia.size} items ready for high-speed download",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 36.dp, top = 2.dp, bottom = 16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false).heightIn(max = 450.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(detectedMedia) { media ->
                            val isAudio = media.url.lowercase().run { contains(".mp3") || contains(".m4a") || contains(".wav") }
                            
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isAudio) MaterialTheme.colorScheme.tertiaryContainer 
                                                    else MaterialTheme.colorScheme.primaryContainer
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isAudio) Icons.Default.Audiotrack else Icons.Default.PlayCircle,
                                                contentDescription = null,
                                                tint = if (isAudio) MaterialTheme.colorScheme.onTertiaryContainer 
                                                       else MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.width(12.dp))
                                        
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = media.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = media.url,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Primary Action: Download
                                        Button(
                                            onClick = {
                                                viewModel.addDownload(media.url, media.title)
                                                Toast.makeText(context, "Download Queued!", Toast.LENGTH_SHORT).show()
                                                showMediaSheet = false
                                            },
                                            modifier = Modifier.weight(1.2f).height(42.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Download", style = MaterialTheme.typography.labelLarge)
                                        }

                                        // Secondary Action: Fast Download
                                        FilledTonalButton(
                                            onClick = {
                                                viewModel.addDownload(media.url, media.title)
                                                Toast.makeText(context, "Fast Download started!", Toast.LENGTH_SHORT).show()
                                                showMediaSheet = false
                                            },
                                            modifier = Modifier.weight(1.2f).height(42.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Fast", style = MaterialTheme.typography.labelLarge)
                                        }

                                        // Utility Actions
                                        Row(
                                            modifier = Modifier.weight(0.8f),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Surface(
                                                onClick = {
                                                    viewModel.addDownload(media.url, media.title, isAudioOnly = true)
                                                    Toast.makeText(context, "Extracting Audio...", Toast.LENGTH_SHORT).show()
                                                    showMediaSheet = false
                                                },
                                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.size(42.dp)
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
                                            
                                            Spacer(modifier = Modifier.width(8.dp))
                                            
                                            Surface(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(media.url))
                                                    Toast.makeText(context, "URL Copied", Toast.LENGTH_SHORT).show()
                                                },
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.size(42.dp)
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
    }
}
