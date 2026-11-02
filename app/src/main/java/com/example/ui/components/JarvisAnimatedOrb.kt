package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import kotlin.math.*

enum class OrbState {
    IDLE,
    WAKE,
    LISTENING,
    THINKING,
    SPEAKING,
    EXECUTING,
    SUCCESS,
    ERROR,
    INTERRUPTED
}

@Composable
fun JarvisAnimatedOrb(
    modifier: Modifier = Modifier,
    state: OrbState = OrbState.IDLE,
    audioLevel: Float = 0f, // 0.0 .. 1.0 (live speech or playback amplitude)
    size: Dp = 260.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "JarvisOrbTransition")

    // Idle organic breathing phase
    val breathingPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BreathingPhase"
    )

    // Dynamic rotation angle (slow continuous revolution)
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.THINKING -> 1800
                    OrbState.EXECUTING -> 2200
                    OrbState.WAKE -> 1400
                    OrbState.LISTENING -> 4000
                    OrbState.SPEAKING -> 3200
                    else -> 9000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationAngle"
    )

    // Dynamic particle pulse phase
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ParticlePhase"
    )

    // Wave intensity spring animation
    val waveIntensity by animateFloatAsState(
        targetValue = when (state) {
            OrbState.WAKE -> 1.35f
            OrbState.LISTENING -> (0.5f + audioLevel * 0.95f).coerceIn(0.5f, 1.45f)
            OrbState.SPEAKING -> (0.45f + audioLevel * 0.85f).coerceIn(0.45f, 1.4f)
            OrbState.THINKING -> 1.0f
            OrbState.EXECUTING -> 1.15f
            OrbState.SUCCESS -> 1.25f
            OrbState.ERROR -> 0.4f
            OrbState.INTERRUPTED -> 0.25f
            OrbState.IDLE -> 0.35f
        },
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "WaveIntensity"
    )

    // Palette mapping based on current Assistant State
    val coreColor = when (state) {
        OrbState.ERROR -> NovaErrorRed
        OrbState.SUCCESS -> NovaSuccessGreen
        OrbState.WAKE -> NovaCyanGlow
        OrbState.THINKING -> NovaSecondaryBlue
        OrbState.EXECUTING -> NovaCyanGlow
        OrbState.LISTENING -> NovaElectricBlue
        OrbState.SPEAKING -> NovaElectricBlue
        OrbState.INTERRUPTED -> NovaTextMuted
        OrbState.IDLE -> NovaElectricBlue
    }

    val wavePrimary = when (state) {
        OrbState.ERROR -> NovaErrorRed
        OrbState.SUCCESS -> NovaSuccessGreen
        OrbState.WAKE -> NovaCyanGlow
        OrbState.THINKING -> NovaSecondaryBlue
        OrbState.EXECUTING -> NovaCyanGlow
        OrbState.LISTENING -> NovaElectricBlue
        OrbState.SPEAKING -> NovaElectricBlue
        OrbState.INTERRUPTED -> NovaTextMuted
        OrbState.IDLE -> NovaElectricBlue
    }

    val waveSecondary = when (state) {
        OrbState.ERROR -> Color(0xFFFFA4A4)
        OrbState.SUCCESS -> Color(0xFF6EE7B7)
        OrbState.WAKE -> Color.White
        OrbState.THINKING -> NovaCyanGlow
        OrbState.EXECUTING -> NovaElectricBlue
        OrbState.LISTENING -> NovaCyanGlow
        OrbState.SPEAKING -> NovaCyanGlow
        OrbState.INTERRUPTED -> NovaBorderLight
        OrbState.IDLE -> NovaCyanGlow
    }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val maxRadius = size.toPx() * 0.47f

            // 1. Atmospheric Cyan-Blue Ambient Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        waveSecondary.copy(alpha = 0.22f * waveIntensity.coerceIn(0.4f, 1.2f)),
                        wavePrimary.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius * 1.22f
                ),
                radius = maxRadius * 1.22f,
                center = center
            )

            // 2. Exact Multi-Harmonic Spirograph & Topographic Mesh Contours
            // Reference image has dense, undulating blue lines forming a floral/harmonic wave
            val layerCount = 30
            val pointsCount = 130
            val rotationRad = Math.toRadians(rotationAngle.toDouble()).toFloat()

            for (i in 0 until layerCount) {
                val fraction = (i + 1).toFloat() / layerCount
                // Radius expands non-linearly to match the visual dispersion
                val baseRadius = maxRadius * (0.24f + 0.74f * fraction)

                // Outer lines are lighter cyan/sky-blue, inner lines are deeper electric blue
                val strokeColor = if (fraction < 0.45f) {
                    lerp(wavePrimary, waveSecondary, fraction / 0.45f)
                } else {
                    lerp(waveSecondary, Color(0xFFBCE5FF), (fraction - 0.45f) / 0.55f)
                }

                // Smooth alpha gradient: softer near outer edge, crisp near center
                val baseAlpha = if (fraction < 0.25f) {
                    0.75f + 0.25f * (fraction / 0.25f)
                } else {
                    (0.85f - 0.45f * ((fraction - 0.25f) / 0.75f)).coerceIn(0.2f, 0.9f)
                }
                val alpha = (baseAlpha * (0.75f + waveIntensity * 0.25f)).coerceIn(0.12f, 0.95f)
                val strokeW = if (i % 3 == 0) 1.5.dp.toPx() else 1.1.dp.toPx()

                val path = Path()
                var firstPoint = Offset.Zero

                // Multi-frequency harmonic perturbation (gives the organic 5-lobed floral contour)
                val harmonics = 5
                val layerPhase = breathingPhase + i * 0.22f

                for (p in 0..pointsCount) {
                    val angle = (p.toFloat() / pointsCount) * 2f * PI.toFloat() + rotationRad * (if (i % 2 == 0) 0.8f else -0.5f)

                    // Wave combination: 5-petal primary harmonic with secondary ripple
                    val w1 = sin(harmonics * angle + layerPhase)
                    val w2 = cos((harmonics - 2) * angle - layerPhase * 1.2f)
                    val w3 = sin(2 * angle + layerPhase * 0.8f)

                    // The wavy perturbation grows stronger toward the outer edge (as in screenshot)
                    val perturbationFactor = fraction.pow(1.2f) * (18.dp.toPx() * waveIntensity)
                    val perturbation = (w1 * 0.62f + w2 * 0.26f + w3 * 0.12f) * perturbationFactor

                    val r = (baseRadius + perturbation).coerceAtLeast(12f)
                    val x = center.x + r * cos(angle)
                    val y = center.y + r * sin(angle)

                    if (p == 0) {
                        path.moveTo(x, y)
                        firstPoint = Offset(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                path.lineTo(firstPoint.x, firstPoint.y)

                drawPath(
                    path = path,
                    color = strokeColor.copy(alpha = alpha),
                    style = Stroke(width = strokeW)
                )
            }

            // 3. Floating Micro-Sparkle Particles during Thinking / Executing / Speaking
            if (state == OrbState.THINKING || state == OrbState.EXECUTING || state == OrbState.SPEAKING || state == OrbState.WAKE) {
                val particleCount = 10
                for (p in 0 until particleCount) {
                    val pAngle = rotationRad * 1.4f + (p.toFloat() / particleCount) * 2f * PI.toFloat()
                    val pDist = maxRadius * (0.42f + 0.48f * sin(particlePhase * 2f * PI.toFloat() + p))
                    val px = center.x + pDist * cos(pAngle)
                    val py = center.y + pDist * sin(pAngle)

                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = 2.2.dp.toPx(),
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = NovaCyanGlow.copy(alpha = 0.35f),
                        radius = 4.5.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }

            // 4. Central Royal Blue Core Disc with Subtle Shadow Ring
            val coreRadius = maxRadius * 0.26f

            // Outer soft glow halo of the core disc
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreColor.copy(alpha = 0.45f),
                        coreColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius * 1.7f
                ),
                radius = coreRadius * 1.7f,
                center = center
            )

            // Core solid disc (vibrant royal blue)
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2575FC), // Vibrant Royal Electric Blue top
                        coreColor           // Deep Cobalt bottom
                    ),
                    start = Offset(center.x, center.y - coreRadius),
                    end = Offset(center.x, center.y + coreRadius)
                ),
                radius = coreRadius,
                center = center
            )

            // Crisp inner rim highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                radius = coreRadius,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )

            // 5. Signature Dual Sparkle AI Stars inside the Center Core Disc (Exact match to reference)
            drawDualJarvisSparkles(
                center = center,
                discRadius = coreRadius,
                color = Color.White
            )
        }
    }
}

/**
 * Draws the signature dual 4-pointed sparkle stars (✦✦) side-by-side inside the central core
 * matching the visual reference screenshot.
 */
private fun DrawScope.drawDualJarvisSparkles(
    center: Offset,
    discRadius: Float,
    color: Color
) {
    // Left / Main Sparkle Star
    val leftSparkCenter = Offset(
        x = center.x - discRadius * 0.22f,
        y = center.y + discRadius * 0.05f
    )
    val leftSparkSize = discRadius * 0.58f
    drawFourPointStar(center = leftSparkCenter, size = leftSparkSize, color = color)

    // Right / Secondary Sparkle Star (slightly smaller, positioned higher)
    val rightSparkCenter = Offset(
        x = center.x + discRadius * 0.26f,
        y = center.y - discRadius * 0.18f
    )
    val rightSparkSize = discRadius * 0.40f
    drawFourPointStar(center = rightSparkCenter, size = rightSparkSize, color = color)
}

/**
 * Draws a crisp 4-pointed curved AI sparkle star.
 */
private fun DrawScope.drawFourPointStar(center: Offset, size: Float, color: Color) {
    val path = Path()
    val half = size / 2f
    val inner = size * 0.16f

    path.moveTo(center.x, center.y - half)
    path.quadraticTo(center.x, center.y, center.x + inner, center.y - inner)
    path.lineTo(center.x + half, center.y)
    path.quadraticTo(center.x, center.y, center.x + inner, center.y + inner)
    path.lineTo(center.x, center.y + half)
    path.quadraticTo(center.x, center.y, center.x - inner, center.y + inner)
    path.lineTo(center.x - half, center.y)
    path.quadraticTo(center.x, center.y, center.x - inner, center.y - inner)
    path.close()

    drawPath(path = path, color = color)
}

