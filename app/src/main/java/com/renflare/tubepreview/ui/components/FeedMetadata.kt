package com.renflare.tubepreview.ui.components

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.renflare.tubepreview.R
import com.renflare.tubepreview.data.FeedVideo

@Composable
internal fun FeedMetadata(video: FeedVideo) {
    val views = compactViews(video.views)
    val time = compactPublishTime(video.uploadTime)
    val text = buildAnnotatedString {
        append(video.channelName)
        if (views.isNotBlank()) {
            if (length > 0) append("  ")
            appendInlineContent("play", "▷")
            append(views)
        }
        if (time.isNotBlank()) {
            if (length > 0) append("  ")
            append(time)
        }
    }
    Text(
        text, color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontFamily = FontFamily.SansSerif, fontSize = 13.sp, lineHeight = 18.sp,
        maxLines = 2, overflow = TextOverflow.Ellipsis,
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = listOf(
                video.channelName,
                if (views.isBlank()) "" else "$views views",
                video.uploadTime
            ).filter { it.isNotBlank() }.joinToString(", ")
        },
        inlineContent = mapOf(
            "play" to InlineTextContent(
                Placeholder(12.sp, 12.sp, PlaceholderVerticalAlign.TextCenter)
            ) {
                Icon(painterResource(R.drawable.feed_play_outline), null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier)
            }
        )
    )
}
