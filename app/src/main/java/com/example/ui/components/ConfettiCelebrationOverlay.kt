package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutQuad
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

data class ConfettiParticle(
    val startXFraction: Float,
    val startYFraction: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float,
    val isCircle: Boolean
)

@Composable
fun ConfettiCelebrationOverlay(
    trigger: Boolean,
    onAnimationEnd: () -> Unit = {}
) {
    if (!trigger) return

    val progress = remember { Animatable(0f) }

    val particles = remember {
        val colors = listOf(
            Color(0xFF10B981), // Emerald
            Color(0xFFF59E0B), // Gold
            Color(0xFF3B82F6), // Blue
            Color(0xFFEC4899), // Pink
            Color(0xFF8B5CF6)  // Purple
        )
        List(45) {
            ConfettiParticle(
                startXFraction = Random.nextFloat() * 0.8f + 0.1f,
                startYFraction = 0.35f,
                velocityX = (Random.nextFloat() - 0.5f) * 600f,
                velocityY = -(Random.nextFloat() * 500f + 200f),
                color = colors.random(),
                size = Random.nextFloat() * 12f + 8f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                isCircle = Random.nextBoolean()
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = EaseOutQuad)
        )
        onAnimationEnd()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val p = progress.value
        val canvasWidth = size.width
        val canvasHeight = size.height

        particles.forEach { particle ->
            val currentX = (particle.startXFraction * canvasWidth) + (particle.velocityX * p)
            val gravityEffect = 900f * p * p
            val currentY = (particle.startYFraction * canvasHeight) + (particle.velocityY * p) + gravityEffect
            val alpha = (1f - p).coerceIn(0f, 1f)

            rotate(degrees = particle.rotationSpeed * p, pivot = Offset(currentX, currentY)) {
                if (particle.isCircle) {
                    drawCircle(
                        color = particle.color.copy(alpha = alpha),
                        radius = particle.size / 2,
                        center = Offset(currentX, currentY)
                    )
                } else {
                    drawRect(
                        color = particle.color.copy(alpha = alpha),
                        topLeft = Offset(currentX - particle.size / 2, currentY - particle.size / 2),
                        size = Size(particle.size, particle.size * 0.6f)
                    )
                }
            }
        }
    }
}
