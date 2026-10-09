package com.renflare.tubepreview.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.renflare.tubepreview.R

/** The supplied Shorts artwork, with an outline variant for feed navigation. */
@Composable
fun ShortsIcon(modifier: Modifier = Modifier, outlined: Boolean = false) {
    Icon(
        painterResource(if (outlined) R.drawable.shorts_outline else R.drawable.shorts_logo),
        contentDescription = null,
        tint = if (outlined) MaterialTheme.colorScheme.onBackground else Color.Unspecified,
        modifier = modifier
    )
}
