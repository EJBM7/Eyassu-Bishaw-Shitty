package com.example.ui.screens.settings

import android.graphics.Color as AndroidColor
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// 40+ curated color palette matching Screenshot 2
private val PRESET_COLORS = listOf(
    // Row 1: Reds, Corals, Deep Oranges
    "#FF3B30", "#FF6961", "#D32F2F", "#FF2D55", "#FF1493", "#FF6F00", "#FF9500", "#FF8A65",
    // Row 2: Ambers, Yellows, Limes, Greens
    "#FFA000", "#FFC107", "#FFEB3B", "#FFF176", "#AEEA00", "#7CB342", "#4CAF50", "#2E7D32",
    // Row 3: Mints, Teals, Cyans, Sky Blues
    "#00E676", "#00C853", "#00BFA5", "#00E5FF", "#00838F", "#00B0FF", "#03A9F4", "#0091EA",
    // Row 4: Apple Blue, Royal Blue (checked in screenshot), Purples, Violets
    "#007AFF", "#0A84FF", "#2979FF", "#448AFF", "#5C6BC0", "#651FFF", "#7C4DFF", "#6200EA",
    // Row 5: Lavenders, Magentas, Roses, Earths, Slates
    "#B388FF", "#E040FB", "#F50057", "#8D6E63", "#A17D23", "#9E9E9E", "#607D8B", "#455A64",
    // Row 6: Pure White & Dark Obsidian
    "#FFFFFF", "#1E1E24"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAccentBottomSheet by remember { mutableStateOf(false) }
    var showBackgroundBottomSheet by remember { mutableStateOf(false) }

    val accentColor = remember(settings.accentColorHex) {
        try {
            Color(AndroidColor.parseColor(settings.accentColorHex))
        } catch (_: Exception) {
            Color(0xFF0A84FF)
        }
    }

    BackHandler {
        if (showAccentBottomSheet) {
            showAccentBottomSheet = false
        } else if (showBackgroundBottomSheet) {
            showBackgroundBottomSheet = false
        } else {
            onBack()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Navigation Header (Back Button + Title)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Appearance",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // Section Title: THEME, ACCENT AND BACKGROUND
        item {
            Text(
                text = "THEME, ACCENT AND BACKGROUND",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7E7E86),
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)
            )
        }

        // Single Grouped Settings Card (Matching Screenshot 1)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16161A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .border(1.dp, Color(0xFF26262E), RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Row 1: Theme (System | Light | Dark)
                    val themeSelection = when {
                        settings.matchSystem -> "System"
                        settings.isDarkMode -> "Dark"
                        else -> "Light"
                    }

                    AppearanceRowItem(
                        icon = Icons.Outlined.BrightnessMedium,
                        title = "Theme",
                        subtitle = "Light, dark or the system one"
                    ) {
                        SegmentedCapsulePicker(
                            options = listOf("System", "Light", "Dark"),
                            selectedOption = themeSelection,
                            accentColor = accentColor,
                            onOptionSelected = { chosen ->
                                when (chosen) {
                                    "System" -> onUpdateSettings { it.copy(matchSystem = true) }
                                    "Light" -> onUpdateSettings { it.copy(matchSystem = false, isDarkMode = false) }
                                    "Dark" -> onUpdateSettings { it.copy(matchSystem = false, isDarkMode = true) }
                                }
                            }
                        )
                    }

                    HorizontalDivider(color = Color(0xFF23232A), thickness = 1.dp, modifier = Modifier.padding(start = 64.dp))

                    // Row 2: Accent color (Shows circular color preview + chevron)
                    AppearanceRowItem(
                        icon = Icons.Outlined.Palette,
                        title = "Accent color",
                        subtitle = "Used on buttons and highlights",
                        onClick = { showAccentBottomSheet = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                                    .border(1.dp, Color(0xFF383842), CircleShape)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Open accent picker",
                                tint = Color(0xFF6E6E78),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF23232A), thickness = 1.dp, modifier = Modifier.padding(start = 64.dp))

                    // Row 3: Background (Shows current background name + chevron)
                    val bgLabel = when (settings.bgPattern) {
                        "SquarePaper" -> "Square Paper"
                        "Grid" -> "Grid"
                        "None" -> "Plain"
                        else -> "Dots"
                    }

                    AppearanceRowItem(
                        icon = Icons.Outlined.Image,
                        title = "Background",
                        subtitle = "What you see behind the cards",
                        onClick = { showBackgroundBottomSheet = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = bgLabel,
                                fontSize = 14.sp,
                                color = Color(0xFF8E8E93)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Open background picker",
                                tint = Color(0xFF6E6E78),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF23232A), thickness = 1.dp, modifier = Modifier.padding(start = 64.dp))

                    // Row 4: Check style (Square | Circle)
                    AppearanceRowItem(
                        icon = Icons.Outlined.CheckBox,
                        title = "Check style",
                        subtitle = "Square or round marks"
                    ) {
                        SegmentedCapsulePicker(
                            options = listOf("Square", "Circle"),
                            selectedOption = settings.checkStyle,
                            accentColor = accentColor,
                            onOptionSelected = { chosen ->
                                onUpdateSettings { it.copy(checkStyle = chosen) }
                            }
                        )
                    }

                    HorizontalDivider(color = Color(0xFF23232A), thickness = 1.dp, modifier = Modifier.padding(start = 64.dp))

                    // Row 5: App icon (Default | Green | Tricolor)
                    AppearanceRowItem(
                        icon = Icons.Outlined.Apps,
                        title = "App icon",
                        subtitle = "The icon on your home screen"
                    ) {
                        SegmentedCapsulePicker(
                            options = listOf("Default", "Green", "Tricolor"),
                            selectedOption = settings.appIcon,
                            accentColor = accentColor,
                            onOptionSelected = { chosen ->
                                onUpdateSettings { it.copy(appIcon = chosen) }
                            }
                        )
                    }
                }
            }
        }

        // Section Title: TAB BAR ORDER
        item {
            Text(
                text = "BOTTOM NAVIGATION DOCK ORDER",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7E7E86),
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 8.dp)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16161A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .border(1.dp, Color(0xFF26262E), RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "Customize Tab Bar Order",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "Use the arrows to reorder tabs in the bottom bar",
                        fontSize = 12.sp,
                        color = Color(0xFF8E8E93),
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    settings.tabOrder.forEachIndexed { index, tabName ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF202026))
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(accentColor.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                                Text(
                                    text = tabName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                IconButton(
                                    onClick = {
                                        if (index > 0) {
                                            val newOrder = settings.tabOrder.toMutableList()
                                            val item = newOrder.removeAt(index)
                                            newOrder.add(index - 1, item)
                                            onUpdateSettings { it.copy(tabOrder = newOrder) }
                                        }
                                    },
                                    enabled = index > 0,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Move Up",
                                        tint = if (index > 0) Color.White else Color(0xFF484852),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (index < settings.tabOrder.size - 1) {
                                            val newOrder = settings.tabOrder.toMutableList()
                                            val item = newOrder.removeAt(index)
                                            newOrder.add(index + 1, item)
                                            onUpdateSettings { it.copy(tabOrder = newOrder) }
                                        }
                                    },
                                    enabled = index < settings.tabOrder.size - 1,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Move Down",
                                        tint = if (index < settings.tabOrder.size - 1) Color.White else Color(0xFF484852),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                onUpdateSettings { it.copy(tabOrder = listOf("Today", "Calendar", "Task", "Habits", "Analysis", "Settings")) }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF383842))
                        ) {
                            Text("Reset Default Order", fontSize = 12.sp, color = accentColor)
                        }
                    }
                }
            }
        }
    }

    // --- ACCENT COLOR BOTTOM SHEET (Screenshots 2 & 3) ---
    if (showAccentBottomSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAccentBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF16161A),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF5E5E68)) },
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
        ) {
            AccentColorBottomSheetContent(
                currentHex = settings.accentColorHex,
                onSelectColor = { newHex ->
                    onUpdateSettings { it.copy(accentColorHex = newHex) }
                }
            )
        }
    }

    // --- BACKGROUND PATTERN BOTTOM SHEET ---
    if (showBackgroundBottomSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showBackgroundBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF16161A),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF5E5E68)) },
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Background Pattern",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val patterns = listOf(
                    Triple("Dots", "Dots", "Clean, subtle modern dot matrix (Recommended)"),
                    Triple("Grid", "Grid", "Minimal modern grid pattern"),
                    Triple("SquarePaper", "Square Paper", "Technical graph paper style"),
                    Triple("None", "Plain", "Solid smooth canvas without pattern")
                )

                patterns.forEach { (key, label, desc) ->
                    val isSelected = settings.bgPattern == key
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Color(0xFF22222A) else Color.Transparent)
                            .clickable {
                                onUpdateSettings { it.copy(bgPattern = key) }
                                showBackgroundBottomSheet = false
                            }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = label,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else Color(0xFFD1D1D6)
                            )
                            Text(
                                text = desc,
                                fontSize = 12.sp,
                                color = Color(0xFF8E8E93)
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// --- ROW ITEM IN GROUPED CARD ---
@Composable
private fun AppearanceRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Badge (Circular / Squircle container)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF222228)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFFB0B0B8),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title and Subtitle
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF8E8E93)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Trailing Control
        trailingContent()
    }
}

// --- SEGMENTED CAPSULE PICKER ---
@Composable
private fun SegmentedCapsulePicker(
    options: List<String>,
    selectedOption: String,
    accentColor: Color,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF222228))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { option ->
            val isSelected = option.equals(selectedOption, ignoreCase = true)
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) accentColor else Color.Transparent,
                animationSpec = tween(durationMillis = 180),
                label = "segment_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color(0xFF9E9EA4),
                animationSpec = tween(durationMillis = 180),
                label = "segment_text"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(bgColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onOptionSelected(option) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }
}

// --- ACCENT COLOR BOTTOM SHEET CONTENT (Tabs: Colors | Custom) ---
@Composable
private fun AccentColorBottomSheetContent(
    currentHex: String,
    onSelectColor: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf("Colors") } // "Colors" or "Custom"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .navigationBarsPadding()
    ) {
        // Title: Accent color
        Text(
            text = "Accent color",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Segmented Tab Selector: [ Colors | Custom ] (Matching Screenshot 2 & 3)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF222228))
                .padding(3.dp)
        ) {
            listOf("Colors", "Custom").forEach { tab ->
                val isTabActive = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isTabActive) Color(0xFF0A84FF) else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedTab = tab },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        fontSize = 14.sp,
                        fontWeight = if (isTabActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isTabActive) Color.White else Color(0xFF8E8E93)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (selectedTab) {
            "Colors" -> {
                // 8-Column Grid of Preset Color Buttons (Screenshot 2)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    items(PRESET_COLORS) { hex ->
                        val itemColor = remember(hex) {
                            try {
                                Color(AndroidColor.parseColor(hex))
                            } catch (_: Exception) {
                                Color.Gray
                            }
                        }
                        val isSelected = currentHex.equals(hex, ignoreCase = true)
                        val isWhite = hex.equals("#FFFFFF", ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(itemColor)
                                .border(
                                    1.dp,
                                    if (isWhite) Color(0xFF383842) else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelectColor(hex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = if (isWhite) Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            "Custom" -> {
                // Interactive Hue Ring + Saturation/Brightness Square Color Picker (Screenshot 3)
                CustomColorWheelPicker(
                    initialHex = currentHex,
                    onColorChanged = onSelectColor
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// --- INTERACTIVE CUSTOM COLOR WHEEL + SAT/VAL PICKER (Screenshot 3) ---
@Composable
private fun CustomColorWheelPicker(
    initialHex: String,
    onColorChanged: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    // Parse initial HSV
    val initialHsv = remember(initialHex) {
        val hsv = FloatArray(3)
        try {
            AndroidColor.colorToHSV(AndroidColor.parseColor(initialHex), hsv)
        } catch (_: Exception) {
            hsv[0] = 210f
            hsv[1] = 0.96f
            hsv[2] = 1.0f
        }
        hsv
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var sat by remember { mutableFloatStateOf(initialHsv[1]) }
    var valB by remember { mutableFloatStateOf(initialHsv[2]) }
    var hexInputText by remember { mutableStateOf(initialHex.uppercase()) }

    LaunchedEffect(hue, sat, valB) {
        val colorInt = AndroidColor.HSVToColor(floatArrayOf(hue, sat, valB))
        val newHex = String.format("#%06X", 0xFFFFFF and colorInt)
        hexInputText = newHex
        onColorChanged(newHex)
    }

    val currentDisplayColor = remember(hue, sat, valB) {
        Color(AndroidColor.HSVToColor(floatArrayOf(hue, sat, valB)))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Outer Hue Ring + Inner Square
        BoxWithConstraints(
            modifier = Modifier
                .size(240.dp)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            val totalSizePx = constraints.maxWidth.toFloat()
            val center = Offset(totalSizePx / 2f, totalSizePx / 2f)
            val ringRadius = totalSizePx * 0.42f
            val ringThickness = 24.dp.value * (totalSizePx / 240f)

            // Canvas for the Hue Spectrum Ring
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val dist = sqrt(dx * dx + dy * dy)
                            if (dist >= ringRadius - ringThickness && dist <= ringRadius + ringThickness) {
                                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                if (angle < 0) angle += 360f
                                hue = angle
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val dx = change.position.x - center.x
                            val dy = change.position.y - center.y
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f
                            hue = angle
                        }
                    }
            ) {
                // Hue Spectrum Sweep Gradient
                val spectrumColors = listOf(
                    Color.Red, Color.Yellow, Color.Green,
                    Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                )
                drawCircle(
                    brush = Brush.sweepGradient(spectrumColors, center = center),
                    radius = ringRadius,
                    center = center,
                    style = Stroke(width = ringThickness, cap = StrokeCap.Round)
                )

                // Hue Thumb Handle (Circular Ring)
                val rad = Math.toRadians(hue.toDouble())
                val thumbCenter = Offset(
                    (center.x + ringRadius * cos(rad)).toFloat(),
                    (center.y + ringRadius * sin(rad)).toFloat()
                )
                drawCircle(
                    color = Color.White,
                    radius = 12.dp.toPx(),
                    center = thumbCenter,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // Inner Saturation & Value Box
            val innerBoxSize = (ringRadius * 1.12f).dp
            Box(
                modifier = Modifier
                    .size(innerBoxSize)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF383842), RoundedCornerShape(8.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                sat = (offset.x / size.width).coerceIn(0f, 1f)
                                valB = (1f - (offset.y / size.height)).coerceIn(0f, 1f)
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                sat = (change.position.x / size.width).coerceIn(0f, 1f)
                                valB = (1f - (change.position.y / size.height)).coerceIn(0f, 1f)
                            }
                        }
                ) {
                    val pureHueColor = Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f)))

                    // 1. Pure Hue Background
                    drawRect(color = pureHueColor)

                    // 2. Horizontal Saturation Gradient (White to Transparent)
                    drawRect(
                        brush = Brush.horizontalGradient(listOf(Color.White, Color.Transparent))
                    )

                    // 3. Vertical Value/Brightness Gradient (Transparent to Black)
                    drawRect(
                        brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black))
                    )

                    // Sat/Val Thumb Handle
                    val thumbX = sat * size.width
                    val thumbY = (1f - valB) * size.height
                    drawCircle(
                        color = Color.White,
                        radius = 8.dp.toPx(),
                        center = Offset(thumbX, thumbY),
                        style = Stroke(width = 2.5f.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Bottom Swatch & Hex Code Row (Screenshot 3)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Big circular preview swatch
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(currentDisplayColor)
                    .border(2.dp, Color(0xFF383842), CircleShape)
            )

            // Hex input field
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Hex",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8E8E93)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = Color(0xFF383842),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(Color(0xFF222228), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    BasicTextField(
                        value = hexInputText,
                        onValueChange = { input ->
                            val clean = input.trim()
                            hexInputText = clean.uppercase()
                            if (clean.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
                                try {
                                    val parsedColor = AndroidColor.parseColor(clean)
                                    val hsv = FloatArray(3)
                                    AndroidColor.colorToHSV(parsedColor, hsv)
                                    hue = hsv[0]
                                    sat = hsv[1]
                                    valB = hsv[2]
                                } catch (_: Exception) {}
                            }
                        },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(Color.White),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                }
            }
        }
    }
}
