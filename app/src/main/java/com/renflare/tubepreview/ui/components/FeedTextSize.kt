package com.renflare.tubepreview.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

/** Match the linear SP sizing of the reference feed's legacy Android text views. */
@Composable
internal fun feedTextSize(size: Float): TextUnit = with(LocalDensity.current) {
    (size * fontScale).dp.toSp()
}
