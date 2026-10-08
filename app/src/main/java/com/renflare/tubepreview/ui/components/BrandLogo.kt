package com.renflare.tubepreview.ui.components

import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.renflare.tubepreview.R

/** Uses the original transparent artwork, without launcher safe-zone padding. */
@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f
) {
    Image(
        painter = painterResource(if (darkTheme) R.drawable.brand_logo_dark else R.drawable.brand_logo_light),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}
