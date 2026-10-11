package com.renflare.tubepreview.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renflare.tubepreview.data.FeedVideo

@Composable
fun VideoCard(
    video: FeedVideo,
    onVideoClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null
) {
    val interactions = remember { MutableInteractionSource() }
    Column(
        modifier = modifier.fillMaxWidth().padding(bottom = 24.dp)
            .clickable(interactionSource = interactions, indication = null, onClick = onVideoClick)
            .testTag(if (video.isUserPreview) "user_video_card" else "video_card_${video.id}")
    ) {
        VideoThumbnail(video)
        Box(Modifier.fillMaxWidth().feedMetadataFeedback(interactions)) {
            Row(
                Modifier.fillMaxWidth()
                    .padding(start = 12.dp, end = 56.dp, top = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(role = Role.Button, onClick = onVideoClick)
                        .semantics { contentDescription = "Go to channel ${video.channelName}" },
                    contentAlignment = Alignment.Center
                ) {
                    if (video.channelAvatarUrl.isNotEmpty()) {
                        AvatarPhoto(
                            model = video.channelAvatarUrl,
                            crop = video.avatarCrop,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            video.avatarLetter.ifBlank { video.channelName.take(1).uppercase() },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        video.title,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium,
                        fontSize = feedTextSize(16f),
                        letterSpacing = 0.08.sp,
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 2,
                        lineHeight = 19.5.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.offset(y = 0.5.dp)
                    )
                    FeedMetadata(video)
                }
            }
            // The 24dp glyph aligns with the first title line, inside a 48dp target.
            FeedIconButton(
                Icons.Outlined.MoreHoriz,
                contentDescription = "Options for ${video.title}",
                onClick = { (onMenuClick ?: onVideoClick).invoke() },
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 12.dp).size(48.dp)
            )
        }
    }
}
