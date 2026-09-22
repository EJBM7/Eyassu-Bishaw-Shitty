package com.example.ui.modals

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.HabitIcon
import com.example.ui.theme.AccentOrange
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CustomColorDialog(
    initialColorHex: String,
    currentIcon: String,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Circular Wheel, 1: Presets Grid

    var hexInput by remember {
        mutableStateOf(if (initialColorHex.startsWith("#")) initialColorHex else "#$initialColorHex")
    }

    // Convert initial color to HSV
    val initialHsv = remember(initialColorHex) {
        val parsed = runCatching {
            AndroidColor.parseColor(if (initialColorHex.startsWith("#")) initialColorHex else "#$initialColorHex")
        }.getOrDefault(AndroidColor.parseColor("#0A84FF"))
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(parsed, hsv)
        hsv
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var sat by remember { mutableFloatStateOf(initialHsv[1]) }
    var valB by remember { mutableFloatStateOf(initialHsv[2]) }

    val parsedColor = remember(hexInput) {
        runCatching {
            val formatted = if (hexInput.startsWith("#")) hexInput else "#$hexInput"
            if (formatted.length == 7 || formatted.length == 9) {
                Color(android.graphics.Color.parseColor(formatted))
            } else {
                null
            }
        }.getOrNull()
    }

    val curatedColors = remember {
        listOf(
            // Row 1: Oranges & Ambers
            "#FF6D00", "#FF5722", "#F97316", "#F59E0B", "#FBBF24",
            // Row 2: Reds & Corals
            "#EF4444", "#DC2626", "#F43F5E", "#E11D48", "#FB7185",
            // Row 3: Pinks & Violets
            "#EC4899", "#D946EF", "#A855F7", "#8B5CF6", "#7C3AED",
            // Row 4: Blues & Cyans
            "#6366F1", "#3B82F6", "#0284C7", "#06B6D4", "#0EA5E9",
            // Row 5: Teals & Greens
            "#14B8A6", "#10B981", "#059669", "#22C55E", "#84CC16",
            // Row 6: Deep & Muted Neutrals
            "#78716C", "#64748B", "#475569", "#D97706", "#B45309",
            // Row 7: Pastels & Accents
            "#38BDF8", "#4ADE80", "#F472B6", "#C084FC", "#FDE047",
            // Row 8: Rich jewel tones
            "#4338CA", "#1D4ED8", "#047857", "#B91C1C", "#6D28D9"
        )
    }

    val activePreviewColor = parsedColor ?: runCatching {
        Color(android.graphics.Color.parseColor(initialColorHex))
    }.getOrDefault(AccentOrange)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF16161F),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E3E)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Habit Color Picker",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF9E968F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Preview Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E28)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2E2E3E), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(activePreviewColor.copy(alpha = 0.22f))
                                .border(2.dp, activePreviewColor, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            HabitIcon(
                                iconName = currentIcon,
                                tint = activePreviewColor,
                                size = 26.dp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Selected Color",
                                fontSize = 12.sp,
                                color = Color(0xFF9E968F)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (hexInput.startsWith("#")) hexInput.uppercase() else "#${hexInput.uppercase()}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = activePreviewColor
                            )
                        }

                        // Large Color Swatch
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(activePreviewColor)
                                .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Selector: Circular Wheel vs Presets Grid
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF121218),
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = activePreviewColor,
                            height = 2.5.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Circular Wheel",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 0) activePreviewColor else Color(0xFF9E968F)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Palette Presets",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 1) activePreviewColor else Color(0xFF9E968F)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // ==========================================
                    // CIRCULAR COLOR WHEEL (360 SweepGradient)
                    // ==========================================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val wheelSize = 200.dp
                        val ringThickness = 28.dp

                        Canvas(
                            modifier = Modifier
                                .size(wheelSize)
                                .pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        val dx = offset.x - size.width / 2f
                                        val dy = offset.y - size.height / 2f
                                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                        if (angle < 0) angle += 360f
                                        hue = angle
                                        val colorInt = AndroidColor.HSVToColor(floatArrayOf(hue, sat, valB))
                                        hexInput = String.format("#%06X", 0xFFFFFF and colorInt)
                                    }
                                }
                                .pointerInput(Unit) {
                                    detectDragGestures { change, _ ->
                                        change.consume()
                                        val dx = change.position.x - size.width / 2f
                                        val dy = change.position.y - size.height / 2f
                                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                        if (angle < 0) angle += 360f
                                        hue = angle
                                        val colorInt = AndroidColor.HSVToColor(floatArrayOf(hue, sat, valB))
                                        hexInput = String.format("#%06X", 0xFFFFFF and colorInt)
                                    }
                                }
                        ) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val ringRadius = (size.width - ringThickness.toPx()) / 2f

                            val spectrumColors = listOf(
                                Color.Red, Color.Yellow, Color.Green,
                                Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                            )
                            drawCircle(
                                brush = Brush.sweepGradient(spectrumColors, center = center),
                                radius = ringRadius,
                                center = center,
                                style = Stroke(width = ringThickness.toPx(), cap = StrokeCap.Round)
                            )

                            // Hue Thumb Ring
                            val rad = Math.toRadians(hue.toDouble())
                            val thumbCenter = Offset(
                                (center.x + ringRadius * cos(rad)).toFloat(),
                                (center.y + ringRadius * sin(rad)).toFloat()
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 10.dp.toPx(),
                                center = thumbCenter,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }

                        // Inner Saturation / Brightness Box
                        val innerBoxSize = 90.dp
                        Box(
                            modifier = Modifier
                                .size(innerBoxSize)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF383848), RoundedCornerShape(8.dp))
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures { offset ->
                                            sat = (offset.x / size.width).coerceIn(0.1f, 1f)
                                            valB = (1f - (offset.y / size.height)).coerceIn(0.1f, 1f)
                                            val colorInt = AndroidColor.HSVToColor(floatArrayOf(hue, sat, valB))
                                            hexInput = String.format("#%06X", 0xFFFFFF and colorInt)
                                        }
                                    }
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, _ ->
                                            change.consume()
                                            sat = (change.position.x / size.width).coerceIn(0.1f, 1f)
                                            valB = (1f - (change.position.y / size.height)).coerceIn(0.1f, 1f)
                                            val colorInt = AndroidColor.HSVToColor(floatArrayOf(hue, sat, valB))
                                            hexInput = String.format("#%06X", 0xFFFFFF and colorInt)
                                        }
                                    }
                            ) {
                                val pureHueColor = Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f)))
                                drawRect(color = pureHueColor)
                                drawRect(brush = Brush.horizontalGradient(listOf(Color.White, Color.Transparent)))
                                drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))

                                val thumbX = sat * size.width
                                val thumbY = (1f - valB) * size.height
                                drawCircle(
                                    color = Color.White,
                                    radius = 6.dp.toPx(),
                                    center = Offset(thumbX, thumbY),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        }
                    }
                } else {
                    // ==========================================
                    // PRESETS GRID
                    // ==========================================
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 36.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(curatedColors) { hex ->
                            val isSelected = hexInput.equals(hex, ignoreCase = true)
                            val c = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray)

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        hexInput = hex
                                        val parsed = AndroidColor.parseColor(hex)
                                        val hsv = FloatArray(3)
                                        AndroidColor.colorToHSV(parsed, hsv)
                                        hue = hsv[0]
                                        sat = hsv[1]
                                        valB = hsv[2]
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hex input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { input ->
                            val clean = input.filter { it == '#' || it.isLetterOrDigit() }.take(7)
                            hexInput = if (!clean.startsWith("#") && clean.isNotEmpty()) "#$clean" else clean
                            if (clean.length == 7) {
                                runCatching {
                                    val parsed = AndroidColor.parseColor(hexInput)
                                    val hsv = FloatArray(3)
                                    AndroidColor.colorToHSV(parsed, hsv)
                                    hue = hsv[0]
                                    sat = hsv[1]
                                    valB = hsv[2]
                                }
                            }
                        },
                        placeholder = { Text("#0A84FF", color = Color(0xFF6E665E), fontSize = 14.sp) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E1E28),
                            unfocusedContainerColor = Color(0xFF1E1E28),
                            focusedIndicatorColor = if (parsedColor != null) activePreviewColor else Color(0xFFEF4444),
                            unfocusedIndicatorColor = Color(0xFF2E2E3E),
                            cursorColor = activePreviewColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            if (parsedColor != null) {
                                val cleanHex = if (hexInput.startsWith("#")) hexInput else "#$hexInput"
                                onColorSelected(cleanHex)
                                onDismiss()
                            }
                        },
                        enabled = parsedColor != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = activePreviewColor,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text(
                            "Apply",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
