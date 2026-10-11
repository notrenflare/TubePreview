package com.renflare.tubepreview.ui.components

import android.os.Build
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.floor

/** Records only the feed, so the navigation controls never blur or sample themselves. */
@Stable
class FeedBackdrop internal constructor(internal val layer: GraphicsLayer) {
    internal var position by mutableStateOf(Offset.Zero)
    internal val revision = mutableIntStateOf(0)
    private var drawCount = 0

    internal fun recorded() {
        // Do not read snapshot state in the source's draw: that would invalidate itself.
        revision.intValue = ++drawCount
    }
}

@Composable
fun rememberFeedBackdrop(): FeedBackdrop {
    val layer = rememberGraphicsLayer()
    return remember(layer) { FeedBackdrop(layer) }
}

fun Modifier.feedBackdropSource(backdrop: FeedBackdrop, listState: LazyListState): Modifier =
    onGloballyPositioned { backdrop.position = it.positionInRoot() }
        .drawWithContent {
            // Lazy items can move in retained child layers without redrawing their parent.
            // Observe scroll here so the sampled strip always follows the visible feed.
            listState.firstVisibleItemIndex
            listState.firstVisibleItemScrollOffset
            backdrop.layer.record { this@drawWithContent.drawContent() }
            drawLayer(backdrop.layer)
            backdrop.recorded()
        }

/**
 * Blur a padded strip of the recorded feed, then tint it and draw sharp controls above it.
 * The offscreen blur buffer is bar-sized rather than a second full-screen bitmap.
 * Android 7–11 use a translucent scrim because RenderEffect requires Android 12.
 */
@Composable
fun Modifier.feedBackdrop(backdrop: FeedBackdrop, tint: Color): Modifier {
    val blurred = rememberGraphicsLayer()
    var position by remember { mutableStateOf(Offset.Zero) }
    val radius = with(LocalDensity.current) { 18.dp.toPx() }
    val effect = remember(radius) { BlurEffect(radius, radius, TileMode.Clamp) }
    return onGloballyPositioned { position = it.positionInRoot() }
        .drawWithContent {
            backdrop.revision.intValue // Redraw when scrolling or an image finishes loading.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                backdrop.layer.size.width > 0 && backdrop.layer.size.height > 0
            ) {
                val padding = ceil(radius * 2).toInt()
                val offset = position - backdrop.position
                // Never pad past the recorded feed: transparent pixels there would fade
                // the blur and reveal sharp content underneath the navigation inset.
                // Clamp the blur at the actual feed edge, including while the bar slides.
                val sourceHeight = backdrop.layer.size.height.toFloat()
                val sampleTop = floor(offset.y - padding).coerceIn(0f, sourceHeight - 1f)
                val sampleBottom = ceil(offset.y + size.height + padding)
                    .coerceIn(sampleTop + 1f, sourceHeight)
                blurred.renderEffect = effect
                blurred.clip = true
                blurred.record(size = IntSize(size.width.toInt(), (sampleBottom - sampleTop).toInt())) {
                    translate(-offset.x, -sampleTop) { drawLayer(backdrop.layer) }
                }
                clipRect {
                    translate(top = sampleTop - offset.y) { drawLayer(blurred) }
                }
            }
            drawRect(tint)
            drawContent()
        }
}
