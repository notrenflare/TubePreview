package com.renflare.tubepreview.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renflare.tubepreview.data.FeedVideo
import com.renflare.tubepreview.ui.components.FeedMetadata
import com.renflare.tubepreview.ui.components.glassBorderStroke
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val tourVideo = FeedVideo(
    "tour_video", "A weekend worth remembering", "Your channel",
    views = "75K", duration = "12:15", uploadTime = "2 days ago"
)

private val tourExamples = listOf(
    tourVideo,
    tourVideo.copy(title = "The long way home"),
    tourVideo.copy(title = "One perfect day away")
)

/** Local illustrations never fetch images or change the creator's saved options. */
@Composable
internal fun TourIllustration(
    page: Int, modifier: Modifier = Modifier, option: Int = 0, onOptionChange: (Int) -> Unit = {}
) {
    val video = tourExamples[option]
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val availableHeight = maxHeight
        val roomy = availableHeight >= 240.dp
        when (page) {
            0 -> {
                Surface(Modifier.fillMaxWidth().height(minOf(availableHeight, (maxWidth - 48.dp) * (9f / 16f) + if (roomy) 176.dp else 76.dp)), shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f), border = glassBorderStroke()) {
                    Column(Modifier.padding(if (roomy) 16.dp else 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (roomy) ContextRow()
                        Surface(Modifier.weight(1f).fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)) {
                            Column(Modifier.padding(8.dp).testTag("onboarding_example_card").clearAndSetSemantics {
                                contentDescription = "Example option ${'A' + option}. ${video.title}. " +
                                    "${video.views} views. ${video.duration}. ${video.uploadTime}."
                            }) {
                                TourThumbnail(option, Modifier.weight(1f).fillMaxWidth(), showDuration = true)
                                if (availableHeight >= 140.dp) {
                                    Spacer(Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(28.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape))
                                        Spacer(Modifier.width(8.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(video.title, style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            FeedMetadata(video)
                                        }
                                    }
                                }
                            }
                        }
                        if (roomy) ContextRow()
                    }
                }
            }
            1 -> {
                val padding = if (roomy) 16.dp else 8.dp
                Surface(
                    Modifier.fillMaxWidth().height(minOf(availableHeight,
                        (maxWidth - padding * 2) * (9f / 16f) + if (roomy) 140.dp else 100.dp))
                        .testTag("onboarding_ab_example_card"),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
                    border = glassBorderStroke()
                ) {
                    Column(Modifier.padding(padding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (availableHeight >= 160.dp) {
                            TourThumbnail(option, Modifier.weight(1f).fillMaxWidth().clearAndSetSemantics {},
                                showDuration = true)
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                        Column(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
                            Text(video.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (roomy) FeedMetadata(video)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(3) { index ->
                                val active = option == index
                                OutlinedButton(
                                    onClick = { onOptionChange(index) }, shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    border = BorderStroke(1.dp,
                                        if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        contentColor = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface),
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                        .semantics { selected = active; contentDescription = "Example option ${'A' + index}" }
                                ) { Text(('A' + index).toString(), fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }
            }
            else -> TourRefreshExample(option, onOptionChange, minOf(availableHeight, 320.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TourRefreshExample(option: Int, onOptionChange: (Int) -> Unit, height: androidx.compose.ui.unit.Dp) {
    val pull = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    val currentOption by rememberUpdatedState(option)
    val updateOption by rememberUpdatedState(onOptionChange)
    fun refresh() {
        if (refreshing) return
        refreshing = true
        scope.launch {
            delay(450)
            updateOption((currentOption + 1) % tourExamples.size)
            refreshing = false
        }
    }
    val roomy = height >= 220.dp
    val video = tourExamples[option]
    Surface(Modifier.fillMaxWidth().height(height).testTag("onboarding_refresh_card"),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f), border = glassBorderStroke()) {
        FeedPullToRefreshBox(
            isRefreshing = refreshing, onRefresh = ::refresh, state = pull,
            threshold = if (height < 200.dp) 32.dp else 64.dp,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp))
                .testTag("onboarding_refresh_pull"),
            indicator = {
                if (refreshing || pull.distanceFraction > 0f) {
                    FeedRefreshIndicator(refreshing, pull.distanceFraction, Modifier.align(Alignment.TopCenter))
                }
            }
        ) {
            Column(
                Modifier.fillMaxSize().padding(if (roomy) 16.dp else 8.dp)
                    .scrollable(rememberScrollableState { 0f }, Orientation.Vertical),
                verticalArrangement = Arrangement.spacedBy(if (height < 160.dp) 4.dp else 8.dp,
                    Alignment.CenterVertically)
            ) {
                if (height >= 160.dp) {
                    Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .clearAndSetSemantics {}) {
                        TourThumbnail(option, Modifier.fillMaxSize(),
                            showTitle = height >= 280.dp, showDuration = true, showOptions = true)
                    }
                }
                Column(Modifier.fillMaxWidth().testTag("onboarding_refresh_selection")
                    .semantics(mergeDescendants = true) {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = "Preview option ${'A' + option}"
                    }) {
                    Text(video.title,
                        style = if (height < 160.dp) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (roomy) FeedMetadata(video)
                }
                OutlinedButton(onClick = ::refresh, shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .semantics { stateDescription = if (refreshing) "Refreshing" else "Ready" }
                        .testTag("onboarding_refresh_button")) {
                    Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Refresh preview", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ContextRow() {
    Row(Modifier.fillMaxWidth().clearAndSetSemantics {}, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(width = 48.dp, height = 28.dp).clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.fillMaxWidth(0.8f).height(6.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape))
            Box(Modifier.fillMaxWidth(0.5f).height(5.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), CircleShape))
        }
    }
}

@Composable
private fun TourThumbnail(
    option: Int, modifier: Modifier = Modifier, showTitle: Boolean = true,
    showDuration: Boolean = false, showOptions: Boolean = false
) {
    val sky = listOf(Color(0xFF183B46), Color(0xFF72533E), Color(0xFF233949))[option]
    val hill = listOf(Color(0xFF42776A), Color(0xFFC47D52), Color(0xFF627F94))[option]
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val availableHeight = maxHeight
        val width = minOf(maxWidth, availableHeight * (16f / 9f))
        Box(Modifier.width(width).aspectRatio(16f / 9f).clip(RoundedCornerShape(12.dp))) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Brush.verticalGradient(listOf(sky, hill)))
                drawCircle(Color(0xFFFFDFAC), size.width * 0.075f,
                    androidx.compose.ui.geometry.Offset(size.width * 0.76f, size.height * 0.28f))
                val ridge = Path().apply {
                    moveTo(0f, size.height * 0.76f)
                    cubicTo(size.width * 0.18f, size.height * 0.3f, size.width * 0.3f, size.height * 0.94f,
                        size.width * 0.55f, size.height * 0.64f)
                    cubicTo(size.width * 0.75f, size.height * 0.4f, size.width * 0.85f, size.height * 0.9f,
                        size.width, size.height * 0.6f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(ridge, hill)
                drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))))
            }
            if (showOptions && width >= 104.dp && availableHeight >= 70.dp) {
                Row(Modifier.align(Alignment.TopEnd).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { index ->
                        val active = index == option
                        Box(Modifier.size(24.dp).background(
                            if (active) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.65f),
                            CircleShape), contentAlignment = Alignment.Center) {
                            Text(('A' + index).toString(), style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (active) MaterialTheme.colorScheme.onPrimary else Color.White)
                        }
                    }
                }
            }
            if (showTitle && availableHeight >= 70.dp) {
                val textSize = if (width < 240.dp) 16.sp else 22.sp
                Text(listOf("WEEKEND\nESCAPE", "TAKE THE\nLONG WAY", "ONE\nPERFECT DAY")[option],
                    Modifier.align(Alignment.BottomStart).padding(12.dp), color = Color.White,
                    fontSize = textSize, lineHeight = textSize * 1.1f, fontWeight = FontWeight.ExtraBold)
            }
            if (showDuration && availableHeight >= 70.dp) {
                Text(tourExamples[option].duration,
                    Modifier.align(Alignment.BottomEnd).padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    color = Color.White, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
