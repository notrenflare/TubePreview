package com.renflare.tubepreview.ui.components

import android.app.Activity
import android.animation.ValueAnimator
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.imageResource
import com.renflare.tubepreview.R
import androidx.core.view.WindowCompat
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.renflare.tubepreview.ui.theme.BrandRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * A single logo-to-line-to-center sequence, gated by real destination readiness.
 * Cached launches still get a visible fill; slow launches wait short of completion.
 */
@Composable
fun StartupSplash(
    isReady: Boolean,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    initialLogoWidth: Dp = 144.dp
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
        // Allow the platform starting window to hand off to the original artwork.
        androidx.compose.runtime.withFrameNanos { }
        androidx.compose.runtime.withFrameNanos { }
        if (animationsEnabled) {
            delay(40)
            shrink.animateTo(1f, tween(100, easing = FastOutSlowInEasing))
            morph.animateTo(1f, tween(200, easing = FastOutSlowInEasing))
            // Never cancel the fill on a cached load: zero-to-full is part of the
            // motion, rather than a flashed-on completed line.
            progress.animateTo(0.92f, tween(280, easing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)))
        }

        snapshotFlow { latestReady }.first { it }
        if (animationsEnabled) {
            progress.animateTo(1f, tween(60, easing = LinearEasing))
            // One clock drives both endpoints, so playhead and tail meet exactly.
            sweep.animateTo(1f, tween(200, easing = FastOutSlowInEasing))
            exit.animateTo(1f, tween(70, easing = LinearEasing))
        }
        latestComplete()
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
            background = background,
            initialLogoWidth = initialLogoWidth.value
        )
    }
}

/** A deterministic frame renderer, shared with animation-clock and pixel tests. */
@Composable
internal fun SplashArtwork(
    shrink: Float,
    morph: Float,
    progress: Float,
    background: Color,
    sweep: Float = 0f,
    modifier: Modifier = Modifier,
    initialLogoWidth: Float = 144f
) {
    val s = shrink.coerceIn(0f, 1f)
    val m = morph.coerceIn(0f, 1f)
    val convergence = sweep.coerceIn(0f, 1f)
    val artwork = ImageBitmap.imageResource(R.drawable.brand_logo)
    // Sample the existing play box, so the morph begins in the artwork's exact red.
    val logoRed = remember(artwork) {
        Color(artwork.asAndroidBitmap().getPixel(artwork.width / 2, (artwork.height * 0.44f).toInt()))
    }
    Box(modifier = modifier.size(300.dp, maxOf(130f, initialLogoWidth).dp).background(background), contentAlignment = Alignment.Center) {
        OriginalSplashLogo(
            width = initialLogoWidth + (84f - initialLogoWidth) * s,
            alpha = (1f - m / 0.55f).coerceIn(0f, 1f),
            fold = m,
            offsetY = -4.51f * s
        )

        Canvas(Modifier.fillMaxSize()) {
            // Prepare the drawing path on the first frame, while the unmodified
            // logo is visible, instead of doing first-use work during the morph.
            val morphAlpha = if (m > 0f) 1f else 0f
            fun mix(start: Float, end: Float) = start + (end - start) * m
            // Match the inner box in the 1024px original artwork at 84dp:
            // x=283..741, y=407..727, with its center 55px below the eye's center.
            val bodyWidth = mix(37.57f, 208f).dp.toPx()
            val bodyHeight = mix(26.25f, 3f).dp.toPx()
            val radius = mix(7.8f, 1.5f).dp.toPx()
            val bodyCenterX = center.x + mix(0f, 16f).dp.toPx()
            val initialLeft = bodyCenterX - bodyWidth / 2f
            val initialRight = bodyCenterX + bodyWidth / 2f
            val playWidth = mix(14.93f, 16f).dp.toPx()
            val playHeight = mix(15.34f, 18f).dp.toPx()
            val initialPlayX = center.x + mix(0.16f, -110f).dp.toPx()

            // The play tip advances from the left while the line's tail retracts
            // from the right. Both arrive at the screen center, never beyond it.
            val playX = initialPlayX + (center.x - playWidth / 2f - initialPlayX) * convergence
            val left = initialLeft + (center.x - initialLeft) * convergence
            val right = initialRight + (center.x - initialRight) * convergence
            val top = center.y - bodyHeight / 2f
            if (right > left) {
                drawRoundRect(
                    color = lerp(logoRed, Color(0xFFBDBDBD), m),
                    topLeft = Offset(left, top),
                    size = Size(right - left, bodyHeight),
                    cornerRadius = CornerRadius(minOf(radius, (right - left) / 2f)),
                    alpha = morphAlpha
                )
                if (progress > 0f) {
                    clipRect(left, top, left + (right - left) * progress.coerceIn(0f, 1f), top + bodyHeight) {
                        drawRoundRect(
                            color = BrandRed,
                            topLeft = Offset(left, top),
                            size = Size(right - left, bodyHeight),
                            cornerRadius = CornerRadius(minOf(radius, (right - left) / 2f))
                        )
                    }
                }
            }

            // Retain the softly rounded play motif from TubePreview's artwork.
            val playLeft = playX - playWidth / 2f
            val playRight = playX + playWidth / 2f
            val playTop = center.y - playHeight / 2f
            val playBottom = center.y + playHeight / 2f
            val rounding = mix(3f, 0.6f).dp.toPx()
            val play = Path().apply {
                moveTo(playLeft, playTop + rounding)
                quadraticTo(playLeft, playTop, playLeft + rounding, playTop + rounding * 0.55f)
                lineTo(playRight - rounding, center.y - rounding * 0.55f)
                quadraticTo(playRight, center.y, playRight - rounding, center.y + rounding * 0.55f)
                lineTo(playLeft + rounding, playBottom - rounding * 0.55f)
                quadraticTo(playLeft, playBottom, playLeft, playBottom - rounding)
                close()
            }
            drawPath(play, color = Color.White, alpha = morphAlpha)
        }
    }
}

@Composable
private fun OriginalSplashLogo(width: Float, alpha: Float, fold: Float, offsetY: Float) {
    Box(
        modifier = Modifier.size(width.dp)
            .graphicsLayer {
                this.alpha = alpha
                scaleY = 1f - fold * 0.8f
                translationY = offsetY.dp.toPx() * (1f - fold)
            }
            .clip(RectangleShape),
        contentAlignment = Alignment.Center
    ) {
        BrandLogo(modifier = Modifier.size(width.dp))
    }
}
