package com.renflare.tubepreview.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun FeedRefreshIndicator(isRefreshing: Boolean, progress: Float, modifier: Modifier = Modifier) {
    val rotation = if (isRefreshing) {
        val transition = rememberInfiniteTransition(label = "feedRefresh")
        val angle by transition.animateFloat(
            initialValue = 0f, targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing)),
            label = "refreshRotation"
        )
        angle
    } else progress.coerceIn(0f, 1f) * 180f

    Box(
        modifier.size(40.dp)
            .graphicsLayer {
                val fraction = if (isRefreshing) 1f else progress.coerceIn(0f, 1f)
                alpha = fraction
                scaleX = 0.8f + 0.2f * fraction
                scaleY = scaleX
            }
            .background(Color(0xFF121212).copy(alpha = 0.96f), CircleShape)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .testTag("home_refresh_indicator"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Outlined.Refresh,
            contentDescription = if (isRefreshing) "Refreshing feed" else "Pull to shuffle",
            tint = Color.White,
            modifier = Modifier.size(28.dp).graphicsLayer { rotationZ = rotation }
        )
    }
}
