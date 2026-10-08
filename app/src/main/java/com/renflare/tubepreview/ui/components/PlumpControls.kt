package com.renflare.tubepreview.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** A brief press response; Compose respects the device's animation duration scale. */
@Composable
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    role: Role = Role.Button,
    onClick: () -> Unit
): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed && enabled) 0.94f else 1f,
        spring(dampingRatio = 0.65f, stiffness = 450f),
        label = "pressScale"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
        .clickable(
            enabled = enabled,
            role = role,
            interactionSource = source,
            indication = ripple(),
            onClick = onClick
        )
}

@Composable
fun PlumpIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier.size(48.dp).clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .bouncyClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun PlumpButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val colors = MaterialTheme.colorScheme
    val foreground = if (enabled) colors.onPrimary else colors.onSurface.copy(alpha = 0.38f)
    Row(
        modifier.heightIn(min = 56.dp).clip(CircleShape)
            .background(if (enabled) colors.primary else colors.onSurface.copy(alpha = 0.12f))
            .bouncyClickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text, color = foreground, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f))
        icon?.let { Icon(it, null, tint = foreground, modifier = Modifier.size(24.dp)) }
    }
}
