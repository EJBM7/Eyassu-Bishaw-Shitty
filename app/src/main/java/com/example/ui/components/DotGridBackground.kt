package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun DotGridBackground(
    modifier: Modifier = Modifier,
    pattern: String = "Dots", // "Dots", "SquarePaper", "Grid", "None"
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    val bgColor = if (isDark) Color(0xFF0A0A0E) else Color(0xFFF9FAFB)
    val minorLineColor = if (isDark) Color(0xFF181820) else Color(0xFFECEFF1)
    val majorLineColor = if (isDark) Color(0xFF242430) else Color(0xFFCFD8DC)
    val dotColor = if (isDark) Color(0xFF22222C) else Color(0xFFD1D5DB)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        if (pattern != "None") {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                when (pattern) {
                    "SquarePaper" -> {
                        val cellSize = 24.dp.toPx()
                        var colIndex = 0
                        var x = 0f
                        while (x <= width) {
                            val isMajor = (colIndex % 4 == 0)
                            drawLine(
                                color = if (isMajor) majorLineColor else minorLineColor,
                                start = Offset(x, 0f),
                                end = Offset(x, height),
                                strokeWidth = if (isMajor) 1.0f.dp.toPx() else 0.5f.dp.toPx()
                            )
                            x += cellSize
                            colIndex++
                        }

                        var rowIndex = 0
                        var y = 0f
                        while (y <= height) {
                            val isMajor = (rowIndex % 4 == 0)
                            drawLine(
                                color = if (isMajor) majorLineColor else minorLineColor,
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = if (isMajor) 1.0f.dp.toPx() else 0.5f.dp.toPx()
                            )
                            y += cellSize
                            rowIndex++
                        }
                    }
                    "Grid" -> {
                        val cellSize = 32.dp.toPx()
                        var x = 0f
                        while (x <= width) {
                            drawLine(
                                color = minorLineColor,
                                start = Offset(x, 0f),
                                end = Offset(x, height),
                                strokeWidth = 0.6f.dp.toPx()
                            )
                            x += cellSize
                        }

                        var y = 0f
                        while (y <= height) {
                            drawLine(
                                color = minorLineColor,
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 0.6f.dp.toPx()
                            )
                            y += cellSize
                        }
                    }
                    "Dots" -> {
                        val stepPx = 28.dp.toPx()
                        val dotRadius = 1.2f.dp.toPx()
                        var x = stepPx / 2
                        while (x < width) {
                            var y = stepPx / 2
                            while (y < height) {
                                drawCircle(
                                    color = dotColor,
                                    radius = dotRadius,
                                    center = Offset(x, y)
                                )
                                y += stepPx
                            }
                            x += stepPx
                        }
                    }
                }
            }
        }
        content()
    }
}
