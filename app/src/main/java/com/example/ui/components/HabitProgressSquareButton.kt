package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Habit action button that follows the rounded rectangle / squircle form.
 * For target/achieve-it habits:
 * - When in progress, the progress stroke traces around the perimeter of the rounded square.
 * - When reaching completion (100%), the square box fills completely with a glowing mood and
 *   displays a crisp white checkmark.
 * For single-step habits:
 * - Empty rounded square when incomplete, fully filled glowing square with checkmark when complete.
 */
@Composable
fun HabitProgressSquareButton(
    habitColor: Color,
    isComplete: Boolean,
    progress: Float, // 0f..1f
    isMultiStep: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    cornerRadius: Dp = 14.dp,
    testTag: String = "habit_action_button"
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "habit_progress"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isComplete) 1.0f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "habit_scale"
    )

    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }

    // Glow and background colors
    val targetBgColor = when {
        isComplete -> habitColor
        isMultiStep && animatedProgress > 0f -> habitColor.copy(alpha = 0.12f)
        else -> Color(0xFF16161C)
    }
    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 300),
        label = "habit_bg"
    )

    val shadowElevation = if (isComplete) 10.dp else 0.dp
    val checkTint = if (habitColor.luminance() > 0.85f) Color.Black else Color.White

    Box(
        modifier = modifier
            .size(size)
            .scale(animatedScale)
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = habitColor.copy(alpha = 0.6f),
                spotColor = habitColor
            )
            .clip(shape)
            .background(animatedBgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = habitColor),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (isComplete) {
            // Completed State: Filled glowing square with crisp checkmark
            // Subtle inner glow border
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = 1.5.dp.toPx()
                val inset = strokePx / 2f
                val r = cornerRadius.toPx() - inset
                val path = Path().apply {
                    addRoundedRectContour(
                        left = inset,
                        top = inset,
                        right = this@Canvas.size.width - inset,
                        bottom = this@Canvas.size.height - inset,
                        radius = r
                    )
                }
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.35f),
                    style = Stroke(width = strokePx)
                )
            }

            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Completed",
                tint = checkTint,
                modifier = Modifier.size(24.dp)
            )
        } else if (isMultiStep && animatedProgress > 0f) {
            // In-Progress State: The stroke follows the perimeter of the square box!
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = 2.5.dp.toPx()
                val halfStroke = strokePx / 2f
                val left = halfStroke
                val top = halfStroke
                val right = this.size.width - halfStroke
                val bottom = this.size.height - halfStroke
                val r = (cornerRadius.toPx() - halfStroke).coerceAtLeast(2f)

                val fullPath = Path().apply {
                    addRoundedRectContour(left, top, right, bottom, r)
                }

                // 1. Subtle background track along the square perimeter
                drawPath(
                    path = fullPath,
                    color = habitColor.copy(alpha = 0.22f),
                    style = Stroke(width = strokePx)
                )

                // 2. Active progress path tracing the square perimeter
                val pathMeasure = PathMeasure()
                pathMeasure.setPath(fullPath, forceClosed = true)
                val totalLength = pathMeasure.length
                val progressPath = Path()

                pathMeasure.getSegment(
                    startDistance = 0f,
                    stopDistance = (totalLength * animatedProgress).coerceIn(0f, totalLength),
                    destination = progressPath,
                    startWithMoveTo = true
                )

                // Glowing outer aura
                drawPath(
                    path = progressPath,
                    color = habitColor.copy(alpha = 0.4f),
                    style = Stroke(width = strokePx + 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Crisp sharp neon stroke
                drawPath(
                    path = progressPath,
                    color = habitColor,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            // Center + icon in glowing habitColor
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Increment",
                tint = habitColor,
                modifier = Modifier.size(20.dp)
            )
        } else {
            // Not started (0 progress) state
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = 1.5.dp.toPx()
                val halfStroke = strokePx / 2f
                val left = halfStroke
                val top = halfStroke
                val right = this.size.width - halfStroke
                val bottom = this.size.height - halfStroke
                val r = (cornerRadius.toPx() - halfStroke).coerceAtLeast(2f)

                val fullPath = Path().apply {
                    addRoundedRectContour(left, top, right, bottom, r)
                }

                drawPath(
                    path = fullPath,
                    color = if (isMultiStep) habitColor.copy(alpha = 0.35f) else habitColor.copy(alpha = 0.45f),
                    style = Stroke(width = strokePx)
                )
            }

            if (isMultiStep) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Increment",
                    tint = habitColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Helper to construct a clockwise rounded rectangle path starting at top-center.
 */
private fun Path.addRoundedRectContour(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    radius: Float
) {
    val width = right - left
    val height = bottom - top
    val r = radius.coerceAtMost(minOf(width, height) / 2f)

    // Start at top center
    moveTo(left + width / 2f, top)
    // Top line toward top-right corner
    lineTo(right - r, top)
    // Top-right corner arc (-90° to 0°)
    arcTo(
        rect = Rect(right - 2f * r, top, right, top + 2f * r),
        startAngleDegrees = -90f,
        sweepAngleDegrees = 90f,
        forceMoveTo = false
    )
    // Right vertical line
    lineTo(right, bottom - r)
    // Bottom-right corner arc (0° to 90°)
    arcTo(
        rect = Rect(right - 2f * r, bottom - 2f * r, right, bottom),
        startAngleDegrees = 0f,
        sweepAngleDegrees = 90f,
        forceMoveTo = false
    )
    // Bottom line toward bottom-left corner
    lineTo(left + r, bottom)
    // Bottom-left corner arc (90° to 180°)
    arcTo(
        rect = Rect(left, bottom - 2f * r, left + 2f * r, bottom),
        startAngleDegrees = 90f,
        sweepAngleDegrees = 90f,
        forceMoveTo = false
    )
    // Left vertical line
    lineTo(left, top + r)
    // Top-left corner arc (180° to 270°)
    arcTo(
        rect = Rect(left, top, left + 2f * r, top + 2f * r),
        startAngleDegrees = 180f,
        sweepAngleDegrees = 90f,
        forceMoveTo = false
    )
    // Back to top center
    lineTo(left + width / 2f, top)
    close()
}
