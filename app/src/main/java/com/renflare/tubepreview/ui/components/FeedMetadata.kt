package com.renflare.tubepreview.ui.components

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renflare.tubepreview.R
import com.renflare.tubepreview.data.FeedVideo

@Composable
internal fun FeedMetadata(video: FeedVideo) {
    val views = compactViews(video.views)
    val time = compactPublishTime(video.uploadTime)
    val channelGap = with(LocalDensity.current) { 10.dp.toSp() }
    val ageGap = with(LocalDensity.current) { 8.dp.toSp() }
    val text = buildAnnotatedString {
        append(video.channelName)
        if (views.isNotBlank()) {
            if (length > 0) appendInlineContent("channelGap", " ")
            appendInlineContent("play", "▷")
            append(views)
        }
        if (time.isNotBlank()) {
            if (length > 0) appendInlineContent("ageGap", " ")
            append(time)
        }
    }
    Text(
        text, color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontFamily = FontFamily.SansSerif, fontSize = 12.sp, lineHeight = 20.sp,
        letterSpacing = 0.15.sp,
        maxLines = 1, overflow = TextOverflow.Ellipsis,
        modifier = Modifier.offset(y = 2.25.dp).clearAndSetSemantics {
            contentDescription = listOf(
                video.channelName,
                if (views.isBlank()) "" else "$views views",
                video.uploadTime
            ).filter { it.isNotBlank() }.joinToString(", ")
        },
        inlineContent = mapOf(
            "channelGap" to InlineTextContent(
                Placeholder(channelGap, 1.sp, PlaceholderVerticalAlign.TextCenter)
            ) {},
            "ageGap" to InlineTextContent(
                Placeholder(ageGap, 1.sp, PlaceholderVerticalAlign.TextCenter)
            ) {},
            "play" to InlineTextContent(
                Placeholder(11.sp, 11.sp, PlaceholderVerticalAlign.TextCenter)
            ) {
                Icon(painterResource(R.drawable.feed_play_outline), null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier)
            }
        )
    )
}
