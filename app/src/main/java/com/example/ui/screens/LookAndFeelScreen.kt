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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LookAndFeelScreen(
    onBack: () -> Unit,
    viewModel: com.example.ui.viewmodel.MainViewModel,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val selectedThemeMode by viewModel.selectedThemeMode.collectAsState()
    val isAmoledMode by viewModel.isAmoledMode.collectAsState()
    val isDynamicColor by viewModel.isDynamicColor.collectAsState()
    val selectedAccentColor by viewModel.selectedAccentColor.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
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
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Preview Section
                PreviewCard()

                // Theme Schemes Section
                ThemeSchemePicker(
                    selectedScheme = selectedAccentColor,
                    onSchemeSelected = { viewModel.selectedAccentColor.value = it }
                )

                // Settings Rows
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SettingSwitchRow(
                        icon = Icons.Outlined.Palette,
                        title = "Dynamic color",
                        subtitle = "Apply colors from wallpapers to the app theme",
                        checked = isDynamicColor,
                        onCheckedChange = { viewModel.isDynamicColor.value = it }
                    )

                    SettingSwitchRow(
                        icon = Icons.Outlined.DarkMode,
                        title = "Dark theme",
                        subtitle = if (selectedThemeMode == "Dark") "On" else "Off",
                        checked = selectedThemeMode == "Dark",
                        onCheckedChange = { 
                            viewModel.selectedThemeMode.value = if (it) "Dark" else "Light" 
                        }
                    )

                    SettingSwitchRow(
                        icon = Icons.Outlined.BrightnessMedium,
                        title = "AMOLED Black Mode",
                        subtitle = "Pure black background for OLED screens",
                        checked = isAmoledMode,
                        onCheckedChange = { viewModel.isAmoledMode.value = it }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Language change logic */ }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Display language",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "English (United States)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    )
}

@Composable
fun PreviewCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
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
                // Mock Artwork/Design
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                Surface(
                    modifier = Modifier.padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
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

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Video title sample text",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Video creator sample text",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LinearProgressIndicator(
                    progress = { 0.6f },
                    modifier = Modifier.fillMaxWidth().clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                )
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
    onSchemeSelected: (String) -> Unit
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
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            state = rememberLazyListState()
        ) {
            items(schemes) { scheme ->
                ThemeSchemeItem(
                    scheme = scheme,
                    isSelected = selectedScheme == scheme.name,
                    onClick = { onSchemeSelected(scheme.name) }
                )
            }
        }

        // Dots Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(schemes.size) { index ->
                val isSelected = schemes[index].name == selectedScheme
                val width by animateDpAsState(if (isSelected) 12.dp else 6.dp, label = "")
                val alpha by animateFloatAsState(if (isSelected) 1f else 0.3f, label = "")
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = width, height = 6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                )
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
            // Quadrant Circle
            Canvas(modifier = Modifier.size(48.dp)) {
                // Top Left: Primary
                drawArc(
                    color = scheme.primary,
                    startAngle = 180f,
                    sweepAngle = 90f,
                    useCenter = true
                )
                // Top Right: Primary Container
                drawArc(
                    color = scheme.primaryContainer,
                    startAngle = 270f,
                    sweepAngle = 90f,
                    useCenter = true
                )
                // Bottom Right: Secondary Container
                drawArc(
                    color = scheme.secondaryContainer,
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = true
                )
                // Bottom Left: Secondary
                drawArc(
                    color = scheme.secondary,
                    startAngle = 90f,
                    sweepAngle = 90f,
                    useCenter = true
                )
            }

            if (isSelected) {
                Surface(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 4.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.padding(4.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
