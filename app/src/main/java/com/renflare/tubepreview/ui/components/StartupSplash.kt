package com.renflare.tubepreview.ui.components

import android.app.Activity
import android.animation.ValueAnimator
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.renflare.tubepreview.ui.theme.BrandRed
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * Startup overlay driven by real destination readiness. Always show the short
 * logo-to-line intro; ready content then takes the quicker fill-and-wipe path.
 */
@Composable
fun StartupSplash(
    isReady: Boolean,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shrink = remember { Animatable(0f) }
    val morph = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }
    val exit = remember { Animatable(0f) }
    val sweep = remember { Animatable(0f) }
    val latestReady by rememberUpdatedState(isReady)
    val latestComplete by rememberUpdatedState(onComplete)
    val animationsEnabled = remember {
        Build.VERSION.SDK_INT < 26 || ValueAnimator.areAnimatorsEnabled()
    }

    LaunchedEffect(Unit) {
        // Let the first Compose frame draw and the platform splash hand off
        // before advancing the visible intro.
        androidx.compose.runtime.withFrameNanos { }
        androidx.compose.runtime.withFrameNanos { }
        if (animationsEnabled) {
            delay(40)
            shrink.animateTo(1f, tween(110, easing = FastOutSlowInEasing))
            morph.animateTo(1f, tween(170, easing = FastOutSlowInEasing))
        }

        coroutineScope {
            val fill = launch {
                if (animationsEnabled) {
                    progress.animateTo(0.92f, tween(300, easing = FastOutSlowInEasing))
                }
            }
            // Readiness shortens the fill, rather than cancelling before any
            // motion is visible. Slow loads can still wait at the end of the line.
            snapshotFlow { latestReady }.first { it }
            fill.cancelAndJoin()
            if (animationsEnabled) {
                val fastLoad = progress.value < 0.5f
                progress.animateTo(1f, tween(70, easing = LinearEasing))
                sweep.animateTo(
                    1f,
                    tween(if (fastLoad) 100 else 180, easing = FastOutSlowInEasing)
                )
                exit.animateTo(1f, tween(60, easing = LinearEasing))
            }
            latestComplete()
        }
    }

    // Match the starting window even if the saved light theme arrives mid-animation.
    val background = Color.Black
    val view = LocalView.current
    val lightSystemBars = MaterialTheme.colorScheme.background.luminance() > 0.5f
    DisposableEffect(view, lightSystemBars) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            controller?.isAppearanceLightStatusBars = lightSystemBars
            controller?.isAppearanceLightNavigationBars = lightSystemBars
        }
    }
    Box(
        modifier = modifier.fillMaxSize()
            .graphicsLayer { alpha = 1f - exit.value }
            .background(background)
            // Keep the ready destination from receiving taps through the overlay.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .semantics {
                contentDescription = "Starting TubePreview"
                progressBarRangeInfo = ProgressBarRangeInfo(progress.value, 0f..1f)
            },
        contentAlignment = Alignment.Center
    ) {
        SplashArtwork(
            shrink = shrink.value,
            morph = morph.value,
            progress = progress.value,
            sweep = sweep.value,
            background = background
        )
    }
}

/** A deterministic frame renderer, shared with previews and animation-clock tests. */
@Composable
internal fun SplashArtwork(
    shrink: Float,
    morph: Float,
    progress: Float,
    background: Color,
    sweep: Float = 0f,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(300.dp, 130.dp), contentAlignment = Alignment.Center) {
        val logoOpacity = (1f - morph * 3f).coerceIn(0f, 1f)
        OriginalSplashLogo(
            width = 144f,
            alpha = (1f - shrink) * logoOpacity
        )
        OriginalSplashLogo(width = 84f, alpha = shrink * logoOpacity)

        Canvas(Modifier.fillMaxSize()) {
            if (morph <= 0f) return@Canvas
            val m = morph.coerceIn(0f, 1f)
            fun mix(start: Float, end: Float) = start + (end - start) * m
            val bodyWidth = mix(84f, 208f).dp.toPx()
            val bodyHeight = mix(61f, 3f).dp.toPx()
            val bodyCenter = center + Offset(mix(0f, 16f).dp.toPx(), 0f)
            val topLeft = bodyCenter - Offset(bodyWidth / 2f, bodyHeight / 2f)
            val radius = mix(18f, 1.5f).dp.toPx()
            val trackColor = lerp(BrandRed, Color(0xFFBDBDBD), m)
            // Move the triangle's trailing edge from just before the track
            // to its far end, clipping away everything it has passed.
            val sweepPosition = sweep.coerceIn(0f, 1f)
            val initialPlayX = center.x + mix(-5f, -110f).dp.toPx()
            val finalPlayX = topLeft.x + bodyWidth + 8.dp.toPx()
            val playX = initialPlayX + (finalPlayX - initialPlayX) * sweepPosition
            val wipeLeft = if (sweepPosition > 0f) {
                (playX - 8.dp.toPx()).coerceIn(topLeft.x, topLeft.x + bodyWidth)
            } else {
                topLeft.x
            }
            clipRect(
                left = wipeLeft, top = topLeft.y,
                right = topLeft.x + bodyWidth, bottom = topLeft.y + bodyHeight
            ) {
                drawRoundRect(
                    color = trackColor,
                    topLeft = topLeft,
                    size = Size(bodyWidth, bodyHeight),
                    cornerRadius = CornerRadius(radius),
                    alpha = (m * 3f).coerceAtMost(1f)
                )
                if (progress > 0f) {
                    clipRect(
                        left = topLeft.x, top = topLeft.y,
                        right = topLeft.x + bodyWidth * progress.coerceIn(0f, 1f),
                        bottom = topLeft.y + bodyHeight
                    ) {
                        drawRoundRect(
                            color = BrandRed,
                            topLeft = topLeft,
                            size = Size(bodyWidth, bodyHeight),
                            cornerRadius = CornerRadius(radius)
                        )
                    }
                }
            }

            // The same play shape leaves the logo, then wipes away the filled line.
            val playCenter = Offset(playX, center.y)
            val playWidth = mix(25f, 16f).dp.toPx()
            val playHeight = mix(28f, 18f).dp.toPx()
            val play = Path().apply {
                moveTo(playCenter.x - playWidth / 2f, playCenter.y - playHeight / 2f)
                lineTo(playCenter.x + playWidth / 2f, playCenter.y)
                lineTo(playCenter.x - playWidth / 2f, playCenter.y + playHeight / 2f)
                close()
            }
            drawPath(play, color = lerp(background, Color.White, m), alpha = (m * 3f).coerceAtMost(1f))
        }
    }
}

@Composable
private fun OriginalSplashLogo(width: Float, alpha: Float) {
    Box(
        modifier = Modifier.size(width.dp)
            .graphicsLayer { this.alpha = alpha }
            .clip(RectangleShape),
        contentAlignment = Alignment.Center
    ) {
        BrandLogo(
            modifier = Modifier.size(width.dp)
        )
    }
}
