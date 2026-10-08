package com.renflare.tubepreview.ui.components

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.ceil

@Stable
class FeedBackdrop internal constructor(internal val source: GraphicsLayer) {
    internal var sourcePosition by mutableStateOf(Offset.Zero)
}

@Composable
fun rememberFeedBackdrop(): FeedBackdrop {
    val source = rememberGraphicsLayer()
    return remember(source) { FeedBackdrop(source) }
}

/** Records the feed once; both the feed and navigation glass reuse its display list. */
fun Modifier.feedBackdropSource(backdrop: FeedBackdrop): Modifier =
    onGloballyPositioned { backdrop.sourcePosition = it.positionInRoot() }
        .drawWithContent {
            backdrop.source.record { this@drawWithContent.drawContent() }
            drawLayer(backdrop.source)
        }

/** Blurs only the sampled backdrop, leaving navigation icons and text sharp. */
@Composable
fun Modifier.feedBackdropGlass(backdrop: FeedBackdrop, tint: Color): Modifier {
    val blurred = rememberGraphicsLayer()
    var position by remember { mutableStateOf(Offset.Zero) }
    val supportsBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    return onGloballyPositioned { position = it.positionInRoot() }
        .drawWithContent {
            if (supportsBlur && backdrop.source.size.width > 0) {
                val radius = 10.dp.toPx()
                val padding = ceil(radius * 3).toInt()
                val relative = position - backdrop.sourcePosition
                // Overscan prevents a hard blur seam at the rounded bar's upper edge.
                blurred.renderEffect = BlurEffect(radius, radius, TileMode.Clamp)
                blurred.record(
                    size = IntSize(
                        size.width.toInt() + padding * 2,
                        size.height.toInt() + padding * 2
                    )
                ) {
                    translate(padding - relative.x, padding - relative.y) {
                        drawLayer(backdrop.source)
                    }
                }
                translate(-padding.toFloat(), -padding.toFloat()) { drawLayer(blurred) }
            }
            // Older Android versions retain translucent glass with a stronger legibility tint.
            drawRect(tint.copy(alpha = if (supportsBlur) 0.68f else 0.85f))
            drawContent()
        }
}
