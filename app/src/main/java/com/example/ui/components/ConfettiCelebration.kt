package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class Particle(
    val startXRatio: Float,
    val velocityX: Float,
    val velocityY: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float,
    val shapeType: Int // 0: rect, 1: circle, 2: star/diamond
)

@Composable
fun ConfettiCelebration(
    trigger: Int,
    modifier: Modifier = Modifier
) {
    if (trigger <= 0) return

    val progress = remember(trigger) { Animatable(0f) }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1800, easing = LinearEasing)
        )
    }

    if (progress.value >= 1f) return

    val colors = listOf(
        Color(0xFFFF5722), Color(0xFFFFC107), Color(0xFF4CAF50),
        Color(0xFF2196F3), Color(0xFF9C27B0), Color(0xFFE91E63),
        Color(0xFF00BCD4), Color(0xFFFF9800), Color(0xFF00E676)
    )

    val particles = remember(trigger) {
        val rand = Random(trigger)
        List(50) {
            Particle(
                startXRatio = rand.nextFloat(),
                velocityX = (rand.nextFloat() - 0.5f) * 600f,
                velocityY = -rand.nextFloat() * 800f - 400f,
                size = rand.nextFloat() * 12f + 8f,
                color = colors[rand.nextInt(colors.size)],
                rotationSpeed = (rand.nextFloat() - 0.5f) * 720f,
                shapeType = rand.nextInt(3)
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val t = progress.value
        val gravity = 1400f // px/s^2

        particles.forEach { p ->
            val originX = p.startXRatio * w
            val originY = h * 0.75f // Burst from bottom-center/bottom area

            val currentX = originX + p.velocityX * t
            val currentY = originY + p.velocityY * t + 0.5f * gravity * (t * t)
            val currentRotation = p.rotationSpeed * t
            val alpha = (1f - t * 0.95f).coerceIn(0f, 1f)

            if (currentY in 0f..h && currentX in 0f..w) {
                rotate(degrees = currentRotation, pivot = Offset(currentX, currentY)) {
                    when (p.shapeType) {
                        0 -> {
                            drawRect(
                                color = p.color.copy(alpha = alpha),
                                topLeft = Offset(currentX - p.size / 2, currentY - p.size / 2),
                                size = Size(p.size, p.size * 0.6f)
                            )
                        }
                        1 -> {
                            drawCircle(
                                color = p.color.copy(alpha = alpha),
                                radius = p.size / 2,
                                center = Offset(currentX, currentY)
                            )
                        }
                        else -> {
                            drawRect(
                                color = p.color.copy(alpha = alpha),
                                topLeft = Offset(currentX - p.size / 3, currentY - p.size / 3),
                                size = Size(p.size * 0.7f, p.size * 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
