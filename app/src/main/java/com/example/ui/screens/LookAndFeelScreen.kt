package com.example.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LookAndFeelScreen(
    onBack: () -> Unit,
    viewModel: com.example.ui.viewmodel.MainViewModel,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val selectedThemeMode by viewModel.selectedThemeMode.collectAsState()
    val isAmoledMode by viewModel.isAmoledMode.collectAsState()
    val isDynamicColor by viewModel.isDynamicColor.collectAsState()
    val isMonochrome by viewModel.isMonochrome.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()
    val cornerRoundness by viewModel.cornerRoundness.collectAsState()
    val selectedAccentColor by viewModel.selectedAccentColor.collectAsState()

    val navLabelVisibility by viewModel.navLabelVisibility.collectAsState()
    val isCompactLayout by viewModel.isCompactLayout.collectAsState()
    val isGlassmorphism by viewModel.isGlassmorphism.collectAsState()
    val isGreetingEnabled by viewModel.isGreetingEnabled.collectAsState()
    val browserTogglePosition by viewModel.browserTogglePosition.collectAsState()
    val isForceDarkWeb by viewModel.isForceDarkWeb.collectAsState()
    val isTime24Hour by viewModel.isTime24Hour.collectAsState()
    val hourColor by viewModel.hourColor.collectAsState()
    val minuteColor by viewModel.minuteColor.collectAsState()
    val secondColor by viewModel.secondColor.collectAsState()
    val selectedFontFamily by viewModel.selectedFontFamily.collectAsState()
    val appBarStyle by viewModel.appBarStyle.collectAsState()
    val animationSpeed by viewModel.animationSpeed.collectAsState()
    val dashboardBgStyle by viewModel.dashboardBgStyle.collectAsState()
    val surfaceTintIntensity by viewModel.surfaceTintIntensity.collectAsState()

    var showCustomColorPicker by remember { mutableStateOf(false) }
    var showThemeModeDialog by remember { mutableStateOf(false) }
    var showNavLabelDialog by remember { mutableStateOf(false) }
    var showBrowserToggleDialog by remember { mutableStateOf(false) }
    var showClockColorDialog by remember { mutableStateOf<String?>(null) }
    var showFontFamilyDialog by remember { mutableStateOf(false) }
    var showAppBarStyleDialog by remember { mutableStateOf(false) }
    var showDashBgStyleDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            when (appBarStyle) {
                1 -> CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Look & feel",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
                2 -> TopAppBar(
                    title = {
                        Text(
                            text = "Look & feel",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
                else -> LargeTopAppBar(
                    title = {
                        Text(
                            text = "Look & feel",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Preview Section
                PreviewCard()

                Spacer(modifier = Modifier.height(8.dp))

                // Theme Schemes Section
                SectionHeader("Accent Color")
                ThemeSchemePicker(
                    selectedScheme = selectedAccentColor,
                    onSchemeSelected = { viewModel.selectedAccentColor.value = it },
                    onAddCustom = { showCustomColorPicker = true }
                )

                // Settings Section: Theme & Display
                SectionHeader("Theme & Display")
                SettingsCard {
                    SettingDetailRow(
                        icon = Icons.Outlined.Brightness4,
                        title = "Theme mode",
                        subtitle = when (selectedThemeMode) {
                            "Dark" -> "Dark theme enabled"
                            "Light" -> "Light theme enabled"
                            else -> "Follow system default"
                        },
                        onClick = { showThemeModeDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SettingSwitchRow(
                        icon = Icons.Outlined.Palette,
                        title = "Dynamic color",
                        subtitle = "Use colors from your wallpaper",
                        checked = isDynamicColor,
                        onCheckedChange = { viewModel.isDynamicColor.value = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SettingSwitchRow(
                        icon = Icons.Outlined.Contrast,
                        title = "Monochrome mode",
                        subtitle = "High contrast black and white theme",
                        checked = isMonochrome,
                        onCheckedChange = { viewModel.isMonochrome.value = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    val isDark = when (selectedThemeMode) {
                        "Dark" -> true
                        "Light" -> false
                        else -> isSystemInDarkTheme()
                    }
                    SettingSwitchRow(
                        icon = Icons.Outlined.BrightnessMedium,
                        title = "AMOLED Black",
                        subtitle = "Perfect blacks for OLED screens",
                        checked = isAmoledMode,
                        enabled = isDark,
                        onCheckedChange = { viewModel.isAmoledMode.value = it }
                    )
                }

                // Settings Section: UI Refinement
                SectionHeader("UI Refinement")
                SettingsCard {
                    SliderSetting(
                        icon = Icons.Outlined.TextFields,
                        title = "Font Scale",
                        value = fontScale,
                        onValueChange = { viewModel.fontScale.value = it },
                        valueRange = 0.8f..1.3f,
                        steps = 5,
                        displayValue = "${(fontScale * 100).toInt()}%"
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SliderSetting(
                        icon = Icons.Outlined.RoundedCorner,
                        title = "Corner Roundness",
                        value = cornerRoundness,
                        onValueChange = { viewModel.cornerRoundness.value = it },
                        valueRange = 0f..2f,
                        steps = 8,
                        displayValue = "${(cornerRoundness * 100).toInt()}%"
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SliderSetting(
                        icon = Icons.Outlined.Opacity,
                        title = "Surface Tint",
                        value = surfaceTintIntensity,
                        onValueChange = { viewModel.surfaceTintIntensity.value = it },
                        valueRange = 0f..1f,
                        steps = 10,
                        displayValue = "${(surfaceTintIntensity * 100).toInt()}%"
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SettingSwitchRow(
                        icon = Icons.Outlined.Layers,
                        title = "Compact layout",
                        subtitle = "Reduce padding for higher density",
                        checked = isCompactLayout,
                        onCheckedChange = { viewModel.isCompactLayout.value = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SettingSwitchRow(
                        icon = Icons.Outlined.BlurOn,
                        title = "Glassmorphism",
                        subtitle = "Translucent frosted effects",
                        checked = isGlassmorphism,
                        onCheckedChange = { viewModel.isGlassmorphism.value = it }
                    )
                }

                // Settings Section: Typography & Animation
                SectionHeader("Typography & Animation")
                SettingsCard {
                    SettingDetailRow(
                        icon = Icons.Outlined.FontDownload,
                        title = "Font family",
                        subtitle = selectedFontFamily,
                        onClick = { showFontFamilyDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SliderSetting(
                        icon = Icons.Outlined.Speed,
                        title = "Animation speed",
                        value = animationSpeed,
                        onValueChange = { viewModel.animationSpeed.value = it },
                        valueRange = 0.5f..1.5f,
                        steps = 2,
                        displayValue = when(animationSpeed) {
                            0.5f -> "Fast"
                            1.0f -> "Normal"
                            else -> "Relaxed"
                        }
                    )
                }

                // Settings Section: Dashboard & Navigation
                SectionHeader("Dashboard & Navigation")
                SettingsCard {
                    SettingDetailRow(
                        icon = Icons.AutoMirrored.Outlined.Label,
                        title = "Navigation labels",
                        subtitle = when (navLabelVisibility) {
                            0 -> "Always visible"
                            1 -> "Selected only"
                            else -> "Icons only"
                        },
                        onClick = { showNavLabelDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SettingDetailRow(
                        icon = Icons.Outlined.WebAsset,
                        title = "App bar style",
                        subtitle = when (appBarStyle) {
                            0 -> "Large (Expanding)"
                            1 -> "Center Aligned"
                            else -> "Small (Compact)"
                        },
                        onClick = { showAppBarStyleDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SettingDetailRow(
                        icon = Icons.Outlined.DashboardCustomize,
                        title = "Dashboard background",
                        subtitle = when (dashboardBgStyle) {
                            1 -> "Subtle Gradient"
                            2 -> "Glassy Effect"
                            else -> "Default Theme"
                        },
                        onClick = { showDashBgStyleDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SettingSwitchRow(
                        icon = Icons.Outlined.WavingHand,
                        title = "Show greeting",
                        subtitle = "Welcome message on dashboard",
                        checked = isGreetingEnabled,
                        onCheckedChange = { viewModel.isGreetingEnabled.value = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SettingSwitchRow(
                        icon = Icons.Outlined.MoreTime,
                        title = "24-hour clock",
                        subtitle = if (isTime24Hour) "Use 24-hour format" else "Use 12-hour format",
                        checked = isTime24Hour,
                        onCheckedChange = { viewModel.isTime24Hour.value = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Outlined.Palette, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Text("Clock segments", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ClockColorChip("H", hourColor) { showClockColorDialog = "hour" }
                            ClockColorChip("M", minuteColor) { showClockColorDialog = "minute" }
                            ClockColorChip("S", secondColor) { showClockColorDialog = "second" }
                        }
                    }
                }

                // Settings Section: Browser UI
                SectionHeader("Browser UI")
                SettingsCard {
                    SettingDetailRow(
                        icon = Icons.Outlined.ViewStream,
                        title = "Toolbar position",
                        subtitle = browserTogglePosition,
                        onClick = { showBrowserToggleDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SettingSwitchRow(
                        icon = Icons.Outlined.Nightlight,
                        title = "Force dark web",
                        subtitle = "Render all websites dark",
                        checked = isForceDarkWeb,
                        onCheckedChange = { viewModel.isForceDarkWeb.value = it }
                    )
                }

                // Miscellaneous
                SectionHeader("Miscellaneous")
                SettingsCard {
                    SettingDetailRow(
                        icon = Icons.Outlined.Language,
                        title = "Display language",
                        subtitle = "English (United States)",
                        onClick = { /* Language change */ }
                    )
                }

                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    )

    if (showCustomColorPicker) {
        AdvancedColorPicker(
            title = "Custom Accent Color",
            currentColor = selectedAccentColor,
            onDismiss = { showCustomColorPicker = false },
            onColorSelected = { hex ->
                viewModel.selectedAccentColor.value = hex
                showCustomColorPicker = false
            }
        )
    }

    if (showThemeModeDialog) {
        ThemeModeDialog(
            currentMode = selectedThemeMode,
            onDismiss = { showThemeModeDialog = false },
            onConfirm = { viewModel.selectedThemeMode.value = it }
        )
    }

    if (showNavLabelDialog) {
        ChoiceDialog(
            title = "Navigation labels",
            options = listOf("Always", "Selected", "Hidden"),
            currentIndex = navLabelVisibility,
            onDismiss = { showNavLabelDialog = false },
            onConfirm = { viewModel.navLabelVisibility.value = it }
        )
    }

    if (showBrowserToggleDialog) {
        val positions = listOf("Bottom Center", "Bottom Left", "Bottom Right")
        ChoiceDialog(
            title = "Toolbar position",
            options = positions,
            currentIndex = positions.indexOf(browserTogglePosition).coerceAtLeast(0),
            onDismiss = { showBrowserToggleDialog = false },
            onConfirm = { viewModel.browserTogglePosition.value = positions[it] }
        )
    }

    if (showClockColorDialog != null) {
        val type = showClockColorDialog!!
        val currentColor = when(type) {
            "hour" -> hourColor
            "minute" -> minuteColor
            else -> secondColor
        }
        AdvancedColorPicker(
            title = "${type.replaceFirstChar { it.uppercase() }} color",
            currentColor = currentColor,
            onDismiss = { showClockColorDialog = null },
            onColorSelected = {
                when(type) {
                    "hour" -> viewModel.hourColor.value = it
                    "minute" -> viewModel.minuteColor.value = it
                    "second" -> viewModel.secondColor.value = it
                }
                showClockColorDialog = null
            }
        )
    }

    if (showFontFamilyDialog) {
        ChoiceDialog(
            title = "Font family",
            options = listOf("Default", "Sans-Serif", "Serif", "Monospace"),
            currentIndex = listOf("Default", "Sans-Serif", "Serif", "Monospace").indexOf(selectedFontFamily),
            onDismiss = { showFontFamilyDialog = false },
            onConfirm = { viewModel.selectedFontFamily.value = listOf("Default", "Sans-Serif", "Serif", "Monospace")[it] }
        )
    }

    if (showAppBarStyleDialog) {
        ChoiceDialog(
            title = "App bar style",
            options = listOf("Large (Expanding)", "Center Aligned", "Small (Compact)"),
            currentIndex = appBarStyle,
            onDismiss = { showAppBarStyleDialog = false },
            onConfirm = { viewModel.appBarStyle.value = it }
        )
    }

    if (showDashBgStyleDialog) {
        ChoiceDialog(
            title = "Dashboard background",
            options = listOf("Default Theme", "Subtle Gradient", "Glassy Effect"),
            currentIndex = dashboardBgStyle,
            onDismiss = { showDashBgStyleDialog = false },
            onConfirm = { viewModel.dashboardBgStyle.value = it }
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) { Column(modifier = Modifier.padding(vertical = 4.dp), content = content) }
}

@Composable
private fun ThemeModeDialog(
    currentMode: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val modes = listOf("System", "Light", "Dark")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Theme mode") },
        text = {
            Column {
                modes.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConfirm(mode); onDismiss() }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(mode)
                        if (mode == currentMode) {
                            Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun SettingDetailRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
    )
}

@Composable
private fun SliderSetting(
    icon: ImageVector,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    displayValue: String
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(displayValue, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun PreviewCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.tertiaryContainer
                            )
                        )
                    ),
                contentAlignment = Alignment.BottomEnd
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                Surface(
                    modifier = Modifier.padding(12.dp),
                    shape = MaterialTheme.shapes.small,
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "69.00 MB • 05:59",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    Text(
                        "NexLoad Material 3 Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Sample text to demonstrate typography",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = true,
                        onClick = {},
                        label = { Text("Selected") },
                        leadingIcon = { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = false,
                        onClick = {},
                        label = { Text("Option") }
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Suggestion") }
                    )
                }

                LinearProgressIndicator(
                    progress = { 0.6f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                )

                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Sample Button")
                }
            }
        }
    }
}

data class ThemeScheme(
    val name: String,
    val primary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color
)

@Composable
fun ThemeSchemePicker(
    selectedScheme: String,
    onSchemeSelected: (String) -> Unit,
    onAddCustom: () -> Unit
) {
    val schemes = listOf(
        ThemeScheme("Bento", BentoPrimary, BentoContainer, Color(0xFF535F70), Color(0xFFD7E3F7)),
        ThemeScheme("Teal", TealPrimary, Color(0xFFB2DFDB), Color(0xFF00796B), Color(0xFFE0F2F1)),
        ThemeScheme("Blue", BluePrimary, Color(0xFFBBDEFB), Color(0xFF1976D2), Color(0xFFE3F2FD)),
        ThemeScheme("Green", GreenPrimary, GreenContainer, Color(0xFF388E3C), Color(0xFFE8F5E9)),
        ThemeScheme("Orange", OrangePrimary, Color(0xFFFFE0B2), Color(0xFFF57C00), Color(0xFFFFF3E0)),
        ThemeScheme("Red", RedPrimary, RedContainer, Color(0xFFD32F2F), Color(0xFFFFEBEE)),
        ThemeScheme("Purple", PurplePrimary, PurpleContainer, Color(0xFF7B1FA2), Color(0xFFF3E5F5)),
        ThemeScheme("Indigo", IndigoPrimary, IndigoContainer, Color(0xFF303F9F), Color(0xFFE8EAF6)),
        ThemeScheme("Cyan", CyanPrimary, CyanContainer, Color(0xFF0097A7), Color(0xFFE0F7FA)),
        ThemeScheme("Amber", AmberPrimary, AmberContainer, Color(0xFFFFA000), Color(0xFFFFF8E1)),
        ThemeScheme("Lime", LimePrimary, LimeContainer, Color(0xFFAFB42B), Color(0xFFF9FBE7)),
        ThemeScheme("Deep Orange", DeepOrangePrimary, DeepOrangeContainer, Color(0xFFE64A19), Color(0xFFFBE9E7)),
        ThemeScheme("Brown", BrownPrimary, BrownContainer, Color(0xFF5D4037), Color(0xFFEFEBE9)),
        ThemeScheme("Deep Purple", DeepPurplePrimary, DeepPurpleContainer, Color(0xFF512DA8), Color(0xFFEDE7F6)),
        ThemeScheme("Light Blue", LightBluePrimary, LightBlueContainer, Color(0xFF0288D1), Color(0xFFE1F5FE)),
        ThemeScheme("Light Green", LightGreenPrimary, LightGreenContainer, Color(0xFF689F38), Color(0xFFF1F8E9)),
    )

    val listState = rememberLazyListState()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            state = listState
        ) {
            items(schemes) { scheme ->
                ThemeSchemeItem(
                    scheme = scheme,
                    isSelected = selectedScheme == scheme.name,
                    onClick = { onSchemeSelected(scheme.name) }
                )
            }
            
            // Custom Color Add Item
            item {
                Card(
                    modifier = Modifier
                        .size(80.dp)
                        .clickable(onClick = onAddCustom),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Add, "Custom Color", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSchemeItem(
    scheme: ThemeScheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (isSelected) 1.05f else 1f, label = "")
    
    Card(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.size(48.dp)) {
                drawArc(color = scheme.primary, startAngle = 180f, sweepAngle = 90f, useCenter = true)
                drawArc(color = scheme.primaryContainer, startAngle = 270f, sweepAngle = 90f, useCenter = true)
                drawArc(color = scheme.secondaryContainer, startAngle = 0f, sweepAngle = 90f, useCenter = true)
                drawArc(color = scheme.secondary, startAngle = 90f, sweepAngle = 90f, useCenter = true)
            }

            if (isSelected) {
                Surface(
                    modifier = Modifier.size(24.dp).align(Alignment.Center),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 4.dp
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.padding(4.dp), tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
private fun ClockColorChip(label: String, colorHex: String, onClick: () -> Unit) {
    val color = remember(colorHex) {
        try { Color(android.graphics.Color.parseColor(if (colorHex == "Default") "#808080" else colorHex)) }
        catch (e: Exception) { Color.Gray }
    }
    Surface(
        onClick = onClick,
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = color,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (colorHex == "Default") Color.White else Color.Transparent)
        }
    }
}

@Composable
private fun ChoiceDialog(
    title: String,
    options: List<String>,
    currentIndex: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConfirm(index); onDismiss() }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(option)
                        if (index == currentIndex) {
                            Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdvancedColorPicker(
    title: String,
    currentColor: String,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    var hexInput by remember { mutableStateOf(if (currentColor == "Default") "#" else currentColor) }
    val presets = listOf(
        "#009688", "#2196F3", "#FF9800", "#43A047", "#E53935", 
        "#8E24AA", "#D81B60", "#3949AB", "#00ACC1", "#FFB300",
        "#C0CA33", "#F4511E", "#6D4C41", "#757575", "#0061A4"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Select a preset or enter a hex code.")
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { colorHex ->
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = Color(android.graphics.Color.parseColor(colorHex)),
                            onClick = { onColorSelected(colorHex) },
                            border = if (hexInput == colorHex) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {}
                    }
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { onColorSelected("Default") },
                        border = if (currentColor == "Default") BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { if (it.length <= 7) hexInput = it.uppercase() },
                    label = { Text("Hex Code") },
                    placeholder = { Text("#000000") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = { onColorSelected("Default") }) { Text("Reset") }
                Button(
                    onClick = { if (hexInput.length == 7) onColorSelected(hexInput) },
                    enabled = hexInput.length == 7
                ) {
                    Text("Apply")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}
