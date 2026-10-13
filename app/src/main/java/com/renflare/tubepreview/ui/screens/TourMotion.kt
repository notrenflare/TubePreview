package com.renflare.tubepreview.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeAnimationSource
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** Follows the platform's animation in either direction, including an interrupted dismissal. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun rememberTourKeyboardProgress(): Float {
    val density = LocalDensity.current
    val current = WindowInsets.ime.getBottom(density)
    val extent = maxOf(current,
        WindowInsets.imeAnimationSource.getBottom(density),
        WindowInsets.imeAnimationTarget.getBottom(density))
    return if (extent == 0) 0f else (current.toFloat() / extent).coerceIn(0f, 1f)
}

/** Collapse measured space without replacing the children, so an active input retains focus. */
internal fun Modifier.tourKeyboardCollapse(progress: Float, horizontal: Boolean = false): Modifier {
    val visible = (1f - progress).coerceIn(0f, 1f)
    return then(if (visible == 0f) Modifier.clearAndSetSemantics {} else Modifier)
        .clipToBounds()
        .layout { measurable, constraints ->
            val child = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            val width = if (horizontal) (child.width * visible).roundToInt() else child.width
            val height = if (horizontal) child.height else (child.height * visible).roundToInt()
            layout(width, height) {
                child.placeRelative(0, if (horizontal) 0 else (-12.dp.toPx() * progress).roundToInt())
            }
        }
        .graphicsLayer { alpha = visible }
}

/** A small staggered arrival, remembered across pager eviction, rotation, and theme changes. */
@Composable
internal fun Modifier.tourEntrance(active: Boolean, order: Int): Modifier {
    if (LocalInspectionMode.current) return this
    var entered by rememberSaveable { mutableStateOf(false) }
    val arrival = remember { Animatable(if (entered) 1f else 0f) }
    LaunchedEffect(active) {
        if (active && !entered) {
            entered = true
            arrival.animateTo(1f, tween(560, delayMillis = order * 80, easing = FastOutSlowInEasing))
        } else if (entered) {
            arrival.snapTo(1f)
        }
    }
    return graphicsLayer {
        alpha = arrival.value
        translationY = (1f - arrival.value) * 14.dp.toPx()
    }
}
