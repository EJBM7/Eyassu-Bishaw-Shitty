package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleLight

@Composable
fun CircularProgressRing(
    progress: Float, // 0f..1f
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    strokeWidth: Dp = 6.dp,
    trackColor: Color = Color(0xFF26262D),
    gradientColors: List<Color> = listOf(AccentPurple, AccentPurpleLight),
    centerText: String? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val diameter = this.size.minDimension - strokePx - 4.dp.toPx()
            val radius = diameter / 2f
            val centerOffset = androidx.compose.ui.geometry.Offset(this.size.width / 2f, this.size.height / 2f)

            // Background Track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = centerOffset,
                style = Stroke(width = strokePx)
            )

            // Glowing Progress Arc
            if (animatedProgress > 0f) {
                val brush = Brush.sweepGradient(gradientColors)
                val sweep = 360f * animatedProgress

                // Soft glowing outer shadow
                drawArc(
                    brush = brush,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        centerOffset.x - radius,
                        centerOffset.y - radius
                    ),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = strokePx + 4.dp.toPx(), cap = StrokeCap.Round),
                    alpha = 0.35f
                )

                // Crisp vibrant stroke
                drawArc(
                    brush = brush,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        centerOffset.x - radius,
                        centerOffset.y - radius
                    ),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        if (centerText != null) {
            Text(
                text = centerText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = Color.White
            )
        }
    }
}
