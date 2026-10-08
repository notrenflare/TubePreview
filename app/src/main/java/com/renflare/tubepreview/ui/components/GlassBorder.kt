package com.renflare.tubepreview.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

@Composable
fun glassBorderStroke(): BorderStroke {
    val colors = MaterialTheme.colorScheme
    val isDark = colors.background.luminance() < 0.5f
    return BorderStroke(
        1.dp,
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = if (isDark) 0.36f else 0.95f),
                colors.onSurface.copy(alpha = 0.10f),
                colors.onSurface.copy(alpha = 0.04f),
                colors.onSurface.copy(alpha = 0.22f)
            )
        )
    )
}

@Composable
fun Modifier.glassBorder(shape: Shape = RoundedCornerShape(12.dp)): Modifier =
    border(glassBorderStroke(), shape)
