package com.renflare.tubepreview.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.renflare.tubepreview.data.FeedVideo

@Composable
fun VideoThumbnail(video: FeedVideo, modifier: Modifier = Modifier) {
    var painter by remember(video.previewThumbnailUrl) { mutableStateOf<Painter?>(null) }
    var thumbnailSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    // This label represents thumbnail artwork; the accessible video title below
    // follows the user's text size. Scale the artwork with its image bounds.
    val labelSize = (thumbnailSize.width / density.density / 13f).coerceIn(20f, 28f) / density.fontScale
    Box(
        modifier.fillMaxWidth().aspectRatio(16f / 9f)
            .onSizeChanged { thumbnailSize = it }
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (video.isUserPreview && video.previewThumbnailUrl.isBlank()) {
            Column(
                Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    PlumpIcons.Gallery,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    "Tap here to customize your card",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.SansSerif
                )
            }
        } else {
            // Solid neutral skeleton remains visible during loading and on image failure.
            AsyncImage(
                model = video.previewThumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onSuccess = { painter = it.painter },
                onLoading = { painter = null },
                onError = { painter = null },
                modifier = Modifier.fillMaxSize()
            )
            if (painter != null && !video.isUserPreview) {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)))
                    )
                )
                Text(
                    text = video.thumbnailLabel,
                    color = Color.White,
                    fontSize = labelSize.sp,
                    lineHeight = (labelSize * 30f / 28f).sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(0.78f).padding(16.dp)
                )
            }
        }

        DurationBadge(
            duration = video.duration,
            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
        )
    }
}

@Composable
private fun DurationBadge(duration: String, modifier: Modifier = Modifier) {
    Text(
        duration,
        color = Color.White,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.SansSerif,
        modifier = modifier.clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.65f))
            .testTag("thumbnail_duration")
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
