package com.example.ui.modals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Habit
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.CardShape
import com.example.ui.theme.ModalShape
import com.example.ui.theme.PillShape
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusSessionModal(
    habits: List<Habit>,
    onDismiss: () -> Unit,
    onCompleteSession: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedHabit by remember { mutableStateOf(habits.firstOrNull()) }
    var selectedDurationMinutes by remember { mutableIntStateOf(25) }
    var remainingSeconds by remember { mutableIntStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }

    // Countdown effect
    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        } else if (remainingSeconds == 0) {
            isRunning = false
        }
    }

    val totalSeconds = selectedDurationMinutes * 60
    val progress = if (totalSeconds > 0) (remainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f) else 1f
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val presets = listOf(10, 15, 25, 45, 60)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = ModalShape,
        containerColor = Color(0xFF141416),
        tonalElevation = 0.dp,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏱ Focus Session",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF9CA3AF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Habit Selector
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "FOCUSING ON",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = PillShape,
                    color = Color(0xFF1E1E24),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2C2C36), PillShape)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedHabit?.name ?: "Habit"} (${selectedHabit?.category ?: "General"})",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )

                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Select habit",
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Duration presets (10m, 15m, 25m, 45m, 60m)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                presets.forEach { mins ->
                    val isSel = selectedDurationMinutes == mins
                    Surface(
                        shape = PillShape,
                        color = if (isSel) AccentPurple else Color(0xFF1E1E24),
                        modifier = Modifier
                            .clip(PillShape)
                            .clickable {
                                selectedDurationMinutes = mins
                                remainingSeconds = mins * 60
                                isRunning = false
                            }
                            .border(1.dp, if (isSel) AccentPurple else Color(0xFF2B2B33), PillShape)
                    ) {
                        Text(
                            text = "${mins}m",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else Color(0xFF9CA3AF),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Circular Countdown Gauge with glowing indicator dot
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    val strokePx = 12.dp.toPx()
                    val diameter = size.minDimension - strokePx
                    val radius = diameter / 2f
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)

                    // Track
                    drawCircle(
                        color = Color(0xFF22222A),
                        radius = radius,
                        center = centerOffset,
                        style = Stroke(width = strokePx)
                    )

                    // Progress Arc
                    val sweepAngle = 360f * progress
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF8B5CF6))
                        ),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )

                    // Red Indicator dot at end of arc
                    val angleRad = Math.toRadians((-90f + sweepAngle).toDouble())
                    val dotX = (centerOffset.x + radius * cos(angleRad)).toFloat()
                    val dotY = (centerOffset.y + radius * sin(angleRad)).toFloat()

                    drawCircle(
                        color = Color(0xFFEF4444),
                        radius = 8.dp.toPx(),
                        center = Offset(dotX, dotY)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = timeFormatted,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isRunning) "Focusing" else if (remainingSeconds == 0) "Finished!" else "Ready",
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Timer Controls: Reset, Play/Pause, Complete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E24))
                        .border(1.dp, Color(0xFF2C2C36), CircleShape)
                        .clickable {
                            isRunning = false
                            remainingSeconds = selectedDurationMinutes * 60
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reset",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Start / Pause Focus Button
                Surface(
                    shape = PillShape,
                    color = AccentPurple,
                    modifier = Modifier
                        .clip(PillShape)
                        .clickable { isRunning = !isRunning }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Toggle",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isRunning) "Pause" else "Start Focus",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Complete Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E24))
                        .border(1.dp, Color(0xFF2C2C36), CircleShape)
                        .clickable {
                            selectedHabit?.let { onCompleteSession(it.id) }
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Complete",
                        tint = AccentAmber,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Bottom Motivational Quote
            Text(
                text = "\"Deep focus produces extraordinary results.\"",
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
