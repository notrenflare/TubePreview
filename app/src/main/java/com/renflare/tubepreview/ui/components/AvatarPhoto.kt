package com.renflare.tubepreview.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import coil.compose.AsyncImage
import com.renflare.tubepreview.data.AvatarCrop
import kotlin.math.roundToInt

/** Render the saved framing without modifying or recompressing the original photo. */
@Composable
fun AvatarPhoto(
    model: String,
    crop: AvatarCrop,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onLoaded: (Float) -> Unit = {},
    onError: () -> Unit = {}
) {
    val framing = crop.sanitized()
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        alignment = BiasAlignment(framing.horizontalBias, framing.verticalBias),
        onSuccess = {
            val drawable = it.result.drawable
            if (drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
                onLoaded(drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight)
            }
        },
        onError = { onError() },
        modifier = modifier.clip(CircleShape).layout { measurable, constraints ->
            val width = constraints.maxWidth
            val height = constraints.maxHeight
            val scaledWidth = (width * framing.zoom).roundToInt().coerceAtLeast(1)
            val scaledHeight = (height * framing.zoom).roundToInt().coerceAtLeast(1)
            val photo = measurable.measure(Constraints.fixed(scaledWidth, scaledHeight))
            layout(width, height) {
                photo.place(
                    (-(scaledWidth - width) * (1f + framing.horizontalBias) / 2f).roundToInt(),
                    (-(scaledHeight - height) * (1f + framing.verticalBias) / 2f).roundToInt()
                )
            }
        }
    )
}
