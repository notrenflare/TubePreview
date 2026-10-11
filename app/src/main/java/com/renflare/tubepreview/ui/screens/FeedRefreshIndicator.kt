package com.renflare.tubepreview.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

// The native SwipeRefreshLayout travel is 64dp, with a 40dp disc starting above the feed.
private val RefreshTravel = 64.dp
private val RefreshDiameter = 40.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeedPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    state: PullToRefreshState,
    modifier: Modifier = Modifier,
    indicator: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier.pullToRefresh(
        isRefreshing = isRefreshing, state = state,
        threshold = RefreshTravel, onRefresh = onRefresh
    )) {
        content()
        indicator()
    }
}

@Composable
internal fun FeedRefreshIndicator(isRefreshing: Boolean, progress: Float, modifier: Modifier = Modifier) {
    var spinning by remember { mutableStateOf(isRefreshing) }
    LaunchedEffect(isRefreshing, progress) {
        if (isRefreshing) spinning = true
        else if (progress <= 0f) spinning = false
    }
    val color = MaterialTheme.colorScheme.onBackground
    val fraction = progress.coerceIn(0f, 1f)

    Box(
        modifier.size(RefreshDiameter)
            .graphicsLayer {
                // Follow the drag, including its resisted overshoot. Settle 24dp below the chips.
                translationY = (RefreshTravel * progress - RefreshDiameter).toPx()
                if (spinning && !isRefreshing) {
                    scaleX = fraction
                    scaleY = fraction
                }
            }
            .shadow(2.dp, CircleShape)
            .background(MaterialTheme.colorScheme.background, CircleShape)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = if (isRefreshing) "Refreshing feed" else "Pull to refresh"
            }
            .testTag("home_refresh_indicator"),
        contentAlignment = Alignment.Center
    ) {
        if (spinning) {
            CircularProgressIndicator(
                color = color, strokeWidth = 2.dp, modifier = Modifier.size(20.dp)
            )
        } else {
            // A growing circular arrow during the pull; an indeterminate arc after release.
            Canvas(Modifier.size(20.dp)) {
                val drag = ((fraction - 0.4f).coerceAtLeast(0f) * 5f / 3f).coerceAtMost(1f)
                val sweep = 288f * drag
                val rotation = (-0.25f + 0.4f * drag) * 360f
                val alpha = if (fraction < 1f) 0.3f else 1f
                rotate(rotation) {
                    drawArc(
                        color.copy(alpha = alpha), startAngle = 0f, sweepAngle = sweep,
                        useCenter = false, topLeft = Offset(1.dp.toPx(), 1.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(size.width - 2.dp.toPx(), size.height - 2.dp.toPx()),
                        style = Stroke(2.dp.toPx())
                    )
                    if (drag > 0f) rotate(sweep) {
                        val tip = Path().apply {
                            val x = size.width - 1.dp.toPx()
                            val y = size.height / 2
                            moveTo(x - 3.dp.toPx() * drag, y)
                            lineTo(x + 3.dp.toPx() * drag, y)
                            lineTo(x, y + 4.dp.toPx() * drag)
                            close()
                        }
                        drawPath(tip, color.copy(alpha = alpha))
                    }
                }
            }
        }
    }
}
