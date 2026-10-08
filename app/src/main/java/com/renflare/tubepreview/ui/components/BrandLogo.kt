package com.renflare.tubepreview.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.renflare.tubepreview.R

/** Uses the original transparent artwork, without launcher safe-zone padding. */
@Composable
fun BrandLogo(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(R.drawable.brand_logo),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}
