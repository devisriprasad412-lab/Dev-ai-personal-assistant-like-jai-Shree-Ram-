package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.speech.VoiceState
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDark
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.StarkGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisArcReactorVisualizer(
    voiceState: VoiceState,
    soundLevel: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reactor_anim")

    // Continuous rotation for outer tech ring
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rot"
    )

    // Counter rotation for inner tech ring
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rot"
    )

    // Breathing pulse for reactor glow
    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    val activeGlowMultiplier = when (voiceState) {
        VoiceState.LISTENING -> 1.5f + (soundLevel * 1.2f)
        VoiceState.SPEAKING -> 1.35f + (soundLevel * 0.8f)
        VoiceState.PROCESSING -> 1.2f
        VoiceState.ERROR -> 1.0f
        VoiceState.IDLE -> 1.0f
    }

    val stateColor = when (voiceState) {
        VoiceState.LISTENING -> NeonGreen
        VoiceState.SPEAKING -> ArcCyan
        VoiceState.PROCESSING -> StarkGold
        VoiceState.ERROR -> NeonRed
        VoiceState.IDLE -> ArcCyan
    }

    Box(
        modifier = modifier
            .size(240.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 120.dp),
                onClick = onClick
            )
            .testTag("arc_reactor_visualizer"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension / 2f - 12.dp.toPx()

            // 1. Ambient Background Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        stateColor.copy(alpha = 0.35f * activeGlowMultiplier.coerceAtMost(2f)),
                        ArcCyanDark.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.1f
                ),
                radius = baseRadius * 1.1f,
                center = center
            )

            // 2. Outer Segmented Ring
            val segments = 8
            val sweep = 360f / segments
            for (i in 0 until segments) {
                val startAngle = outerRotation + (i * sweep) + 4f
                val arcSweep = sweep - 8f
                drawArc(
                    color = stateColor.copy(alpha = if (i % 2 == 0) 0.85f else 0.4f),
                    startAngle = startAngle,
                    sweepAngle = arcSweep,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                    topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 2, baseRadius * 2)
                )
            }

            // 3. Middle Orbit Ring with Tick Marks
            val midRadius = baseRadius * 0.78f
            drawCircle(
                color = StarkGold.copy(alpha = 0.45f),
                radius = midRadius,
                center = center,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 15f), innerRotation)
                )
            )

            // 4. Waveform / Audio Level Ring
            val waveRadius = baseRadius * (0.6f + (soundLevel * 0.15f))
            val waveColor = if (voiceState == VoiceState.LISTENING) NeonGreen else ArcCyan
            drawCircle(
                color = waveColor.copy(alpha = 0.6f),
                radius = waveRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 5. Inner Core Rings
            val innerRadius = baseRadius * 0.45f * corePulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        stateColor.copy(alpha = 0.9f),
                        ArcCyanGlow,
                        Color.Transparent
                    ),
                    center = center,
                    radius = innerRadius * 1.2f
                ),
                radius = innerRadius,
                center = center
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.95f),
                radius = innerRadius * 0.4f,
                center = center
            )

            // 6. Holographic Crosshair ticks
            val tickCount = 12
            for (i in 0 until tickCount) {
                val angle = (i * (360f / tickCount) + innerRotation) * (PI / 180.0)
                val startR = midRadius - 8.dp.toPx()
                val endR = midRadius + 8.dp.toPx()
                val startX = (center.x + startR * cos(angle)).toFloat()
                val startY = (center.y + startR * sin(angle)).toFloat()
                val endX = (center.x + endR * cos(angle)).toFloat()
                val endY = (center.y + endR * sin(angle)).toFloat()

                drawLine(
                    color = stateColor.copy(alpha = 0.6f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
        }
    }
}
