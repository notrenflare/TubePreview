package com.renflare.tubepreview.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.renflare.tubepreview.data.FeedVideo
import kotlin.math.max

@Composable
fun VideoThumbnail(video: FeedVideo, modifier: Modifier = Modifier) {
    var painter by remember(video.previewThumbnailUrl) { mutableStateOf<Painter?>(null) }
    var thumbnailSize by remember { mutableStateOf(IntSize.Zero) }
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
                    Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    "Tap here to customize your card",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    fontSize = 28.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(0.78f).padding(16.dp)
                )
            }
        }

        DurationBadge(
            duration = video.duration,
            painter = painter,
            thumbnailSize = thumbnailSize,
            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
        )
    }
}

@Composable
private fun DurationBadge(
    duration: String,
    painter: Painter?,
    thumbnailSize: IntSize,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(5.dp)
    Box(modifier.clip(shape).testTag("thumbnail_duration")) {
        if (painter != null && thumbnailSize.width > 0) {
            // Reuse the loaded painter to blur only the image region underneath this badge.
            // No second download or full-thumbnail offscreen buffer is needed.
            Canvas(
                Modifier.matchParentSize().blur(6.dp, BlurredEdgeTreatment.Rectangle)
            ) {
                val intrinsic = painter.intrinsicSize
                if (intrinsic.width.isFinite() && intrinsic.height.isFinite() &&
                    intrinsic.width > 0f && intrinsic.height > 0f
                ) {
                    val scale = max(thumbnailSize.width / intrinsic.width, thumbnailSize.height / intrinsic.height)
                    val imageSize = Size(intrinsic.width * scale, intrinsic.height * scale)
                    val left = (thumbnailSize.width - imageSize.width) / 2f -
                        (thumbnailSize.width - size.width - 8.dp.toPx())
                    val top = (thumbnailSize.height - imageSize.height) / 2f -
                        (thumbnailSize.height - size.height - 8.dp.toPx())
                    translate(left, top) { with(painter) { draw(imageSize) } }
                }
            }
        }
        Box(
            Modifier.matchParentSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .border(0.5.dp, Color.White.copy(alpha = 0.10f), shape)
        )
        Text(
            duration,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}
