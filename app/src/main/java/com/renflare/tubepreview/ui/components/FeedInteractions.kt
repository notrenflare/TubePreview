package com.renflare.tubepreview.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
internal fun feedFeedbackColor(): Color =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color.White else Color.Black

/** Neutral feedback, scoped to the preview feed rather than the branded settings UI. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeedInteractions(content: @Composable () -> Unit) {
    val color = feedFeedbackColor()
    CompositionLocalProvider(
        LocalIndication provides ripple(color = color),
        LocalRippleConfiguration provides RippleConfiguration(
            color = color,
            rippleAlpha = RippleAlpha(
                draggedAlpha = 0.16f, focusedAlpha = 0.16f,
                hoveredAlpha = 0.08f, pressedAlpha = 0.10f
            )
        ),
        content = content
    )
}

@Composable
internal fun FeedIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onBackground
) {
    Box(
        modifier.size(48.dp).clickable(
            role = Role.Button,
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = false, radius = 24.dp, color = feedFeedbackColor()),
            onClick = onClick
        ),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(24.dp))
    }
}

/** YouTube highlights the metadata on a video press, leaving the thumbnail unobscured. */
internal fun Modifier.feedMetadataFeedback(source: MutableInteractionSource): Modifier = composed {
    val pressed by source.collectIsPressedAsState()
    val focused by source.collectIsFocusedAsState()
    val hovered by source.collectIsHoveredAsState()
    val color = feedFeedbackColor()
    drawWithContent {
        val alpha = when {
            focused -> 0.16f
            pressed -> 0.10f
            hovered -> 0.08f
            else -> 0f
        }
        if (alpha > 0f) drawRect(color.copy(alpha = alpha))
        drawContent()
    }
}
