package com.renflare.tubepreview.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag

/** Keep system icons legible while letting the scrolling feed show through. */
@Composable
internal fun FeedStatusBarProtection(
    color: Color,
    hiddenFraction: () -> Float,
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars)
            .testTag("home_status_bar_protection")
            .drawBehind {
                // Read scroll state only in drawing; it must not recompose the feed.
                val alpha = 1f - 0.4f * hiddenFraction().coerceIn(0f, 1f)
                drawRect(color.copy(alpha = alpha))
            }
    )
}
