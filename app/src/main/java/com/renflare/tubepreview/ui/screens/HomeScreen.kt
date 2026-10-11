package com.renflare.tubepreview.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.renflare.tubepreview.ui.components.BrandLogo
import com.renflare.tubepreview.ui.components.FeedStatusBarProtection
import com.renflare.tubepreview.ui.components.FeedInteractions
import com.renflare.tubepreview.ui.components.FeedIconButton
import com.renflare.tubepreview.ui.components.feedFeedbackColor
import com.renflare.tubepreview.ui.components.feedBackdrop
import com.renflare.tubepreview.ui.components.feedBackdropSource
import com.renflare.tubepreview.ui.components.rememberFeedBackdrop
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.renflare.tubepreview.ui.components.ShortsIcon
import androidx.compose.ui.res.vectorResource
import com.renflare.tubepreview.R
import com.renflare.tubepreview.data.activePreviewIndex
import com.renflare.tubepreview.data.readyPreviewIndices
import com.renflare.tubepreview.data.CreatorSettings
import com.renflare.tubepreview.data.FeedVideo
import com.renflare.tubepreview.data.FeedShort
import com.renflare.tubepreview.data.MockShortFeed
import com.renflare.tubepreview.ui.components.AvatarPhoto
import com.renflare.tubepreview.ui.components.VideoCard
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    settings: CreatorSettings,
    videoFeed: List<FeedVideo>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    shortsFeed: List<FeedShort> = MockShortFeed.initial(),
    onSelectVariant: (Int) -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()
    var refreshRequested by remember { mutableStateOf(false) }
    LaunchedEffect(videoFeed) {
        if (refreshRequested) {
            listState.scrollToItem(0)
            refreshRequested = false
        }
    }

    // Category chips list state
    val categories = listOf("All", "Podcasts", "Music", "Shorts", "Tech", "Gaming", "DIY", "Comedy", "Cooking")
    var selectedCategory by remember { mutableStateOf("All") }

    var topBarHeightPx by remember { mutableFloatStateOf(1f) }
    var bottomBarHeightPx by remember { mutableFloatStateOf(1f) }
    var topBarOffsetHeightPx by remember { mutableFloatStateOf(0f) }
    var bottomBarOffsetHeightPx by remember { mutableFloatStateOf(0f) }

    val refreshing = rememberUpdatedState(isRefreshing)
    val hasFeed = rememberUpdatedState(videoFeed.isNotEmpty())
    var leavingFeedEnd by remember { mutableStateOf(false) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.canScrollForward to listState.canScrollBackward }.collect { (forward, backward) ->
            if (!forward && hasFeed.value && !leavingFeedEnd) {
                bottomBarOffsetHeightPx = 0f
            }
            if (!backward) {
                topBarOffsetHeightPx = 0f
                bottomBarOffsetHeightPx = 0f
                leavingFeedEnd = false
            }
        }
    }

    val nestedScrollConnection = remember(listState, pullToRefreshState) {
        object : NestedScrollConnection {
            private fun pinBars() {
                topBarOffsetHeightPx = 0f
                bottomBarOffsetHeightPx = 0f
                leavingFeedEnd = false
            }

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (refreshing.value || pullToRefreshState.distanceFraction > 0f ||
                    (!listState.canScrollBackward && available.y > 0f)
                ) {
                    pinBars()
                } else if (!listState.canScrollForward && available.y > 0f) {
                    leavingFeedEnd = true
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (refreshing.value || pullToRefreshState.distanceFraction > 0f || !listState.canScrollBackward) {
                    pinBars()
                    return Offset.Zero
                }
                // Only actual list movement can hide chrome; unconsumed refresh pulls cannot.
                val delta = consumed.y
                if (delta < 0f) leavingFeedEnd = false
                topBarOffsetHeightPx = (topBarOffsetHeightPx + delta).coerceIn(-topBarHeightPx, 0f)
                bottomBarOffsetHeightPx = when {
                    !listState.canScrollForward && delta <= 0f && !leavingFeedEnd -> {
                        leavingFeedEnd = false
                        0f
                    }
                    leavingFeedEnd -> (bottomBarOffsetHeightPx + kotlin.math.abs(delta)).coerceAtMost(bottomBarHeightPx)
                    else -> (bottomBarOffsetHeightPx - delta).coerceIn(0f, bottomBarHeightPx)
                }
                return Offset.Zero
            }
        }
    }

    val backdrop = rememberFeedBackdrop()
    // Match the preview canvas without changing the branding/settings palette.
    val feedBackground = if (settings.isDarkMode) Color(0xFF0F0F0F) else Color.White
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = feedBackground,
            surface = feedBackground
        )
    ) {
        FeedInteractions {
            Box(modifier.fillMaxSize()) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Column(
                            modifier = Modifier
                                .onSizeChanged { topBarHeightPx = it.height.toFloat() }
                                .offset { IntOffset(x = 0, y = if (isRefreshing || pullToRefreshState.distanceFraction > 0f) 0 else topBarOffsetHeightPx.roundToInt()) }
                                .testTag("home_top_bar")
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            BoxWithConstraints(Modifier.statusBarsPadding().fillMaxWidth()) {
                                val compactHeader = maxWidth < 340.dp
                                Row(
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                        .padding(start = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        BrandLogo(modifier = Modifier.size(28.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "TubePreview",
                                            fontSize = if (compactHeader) 18.sp else 20.sp,
                                            fontFamily = remember {
                                                FontFamily(android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD))
                                            },
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.5).sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    FeedIconButton(
                                        icon = ImageVector.vectorResource(R.drawable.feed_chats),
                                        contentDescription = "Your preview settings",
                                        onClick = onNavigateToSettings,
                                        modifier = Modifier.testTag("home_preview_settings")
                                    )
                                    FeedIconButton(
                                        icon = ImageVector.vectorResource(R.drawable.feed_notifications),
                                        contentDescription = "Notifications",
                                        onClick = { scope.launch { snackbarHostState.showSnackbar("No notifications yet") } },
                                        modifier = Modifier.testTag("home_notifications")
                                    )
                                    FeedIconButton(
                                        icon = ImageVector.vectorResource(R.drawable.feed_search),
                                        contentDescription = "Search videos",
                                        onClick = { scope.launch { snackbarHostState.showSnackbar("Search is not available in the preview feed") } },
                                        modifier = Modifier.testTag("home_search")
                                    )
                                }
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            ) {
                                item(key = "explore") {
                                    FeedCategoryChip(
                                        selected = selectedCategory == "Explore",
                                        onClick = { selectedCategory = "Explore" },
                                        modifier = Modifier.testTag("home_explore_tab"),
                                        icon = ImageVector.vectorResource(R.drawable.feed_explore)
                                    )
                                }
                                item(key = "explore_gap") { Spacer(Modifier.width(0.dp)) }
                                item(key = "custom_feed") {
                                    FeedCategoryChip(
                                        selected = false,
                                        onClick = onNavigateToSettings,
                                        text = "Your custom feed",
                                        leadingIcon = ImageVector.vectorResource(R.drawable.feed_custom),
                                        modifier = Modifier.testTag("home_custom_feed")
                                    )
                                }
                                items(categories) { category ->
                                    FeedCategoryChip(
                                        selected = selectedCategory == category,
                                        onClick = { selectedCategory = category },
                                        text = category
                                    )
                                }
                            }
                        }
                    },
                    bottomBar = {
                        Column(
                            modifier = Modifier
                                .onSizeChanged { bottomBarHeightPx = it.height.toFloat() }
                                .offset { IntOffset(x = 0, y = if (isRefreshing || pullToRefreshState.distanceFraction > 0f) 0 else bottomBarOffsetHeightPx.roundToInt()) }
                                .fillMaxWidth()
                                .clipToBounds()
                                .feedBackdrop(
                                    backdrop,
                                    MaterialTheme.colorScheme.background.copy(alpha = if (settings.isDarkMode) 0.72f else 0.82f)
                                )
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("home_bottom_bar")
                        ) {
                            if (settings.abPreview.enabled) {
                                val ready = settings.readyPreviewIndices()
                                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        if (ready.size >= 2) "A/B · ${'A' + settings.activePreviewIndex()}" else "Add options",
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite }.testTag("home_ab_status")
                                    )
                                    (0..2).forEach { index ->
                                        TextButton(
                                            onClick = { onSelectVariant(index) }, enabled = index in ready,
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = MaterialTheme.colorScheme.onSurface,
                                                containerColor = if (settings.activePreviewIndex() == index && index in ready)
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f) else Color.Transparent
                                            ),
                                            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                                                .semantics {
                                                    selected = settings.activePreviewIndex() == index && index in ready
                                                    contentDescription = "Preview option ${'A' + index}"
                                                }
                                                .testTag("home_ab_${index}")
                                        ) {
                                            Text(('A' + index).toString(),
                                                fontWeight = if (settings.activePreviewIndex() == index) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
                                    FeedIconButton(Icons.Outlined.Edit, "Edit A/B options", onNavigateToSettings)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val navItems = listOf(
                                    "Home" to ImageVector.vectorResource(R.drawable.feed_home),
                                    "Shorts" to Icons.Outlined.SmartDisplay,
                                    "Create" to ImageVector.vectorResource(R.drawable.feed_add),
                                    "Subscriptions" to ImageVector.vectorResource(R.drawable.feed_subscriptions),
                                    "You" to Icons.Outlined.AccountCircle
                                )
                                navItems.forEach { (title, icon) ->
                                    val isCreate = title == "Create"
                                    val isYou = title == "You"
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .padding(vertical = 4.dp)
                                            .clickable(
                                                role = Role.Tab,
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = ripple(bounded = false, radius = 36.dp, color = feedFeedbackColor())
                                            ) {
                                                when {
                                                    isYou || isCreate -> onNavigateToSettings()
                                                    title == "Home" -> scope.launch { listState.animateScrollToItem(0) }
                                                    title != "Home" -> scope.launch {
                                                        snackbarHostState.showSnackbar("$title is a preview section")
                                                    }
                                                }
                                            }
                                            .semantics { selected = title == "Home" }
                                            .then(if (isYou) Modifier.testTag("home_settings_button") else Modifier),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        if (isCreate) {
                                            Box(
                                                modifier = Modifier.size(36.dp)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(icon, "Edit your preview", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                                            }
                                        } else {
                                            if (isYou && settings.profilePictureUri.isNotEmpty()) {
                                                AvatarPhoto(
                                                    model = settings.profilePictureUri,
                                                    crop = settings.avatarCrop,
                                                    contentDescription = "Your profile",
                                                    modifier = Modifier.size(24.dp).clip(CircleShape)
                                                )
                                            } else if (title == "Shorts") {
                                                ShortsIcon(outlined = true, modifier = Modifier.size(24.dp))
                                            } else {
                                                Icon(
                                                    icon,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onBackground,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = title,
                                                modifier = Modifier.semantics { if (title == "Subscriptions") contentDescription = title },
                                                color = MaterialTheme.colorScheme.onBackground,
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = with(LocalDensity.current) { 10.dp.toSp() },
                                                lineHeight = with(LocalDensity.current) { 11.5.dp.toSp() },
                                                fontWeight = FontWeight.Normal,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    FeedPullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            refreshRequested = true
                            onRefresh()
                        },
                        state = pullToRefreshState,
                        modifier = Modifier
                            .fillMaxSize()
                            .feedBackdropSource(backdrop, listState)
                            .background(MaterialTheme.colorScheme.background),
                        indicator = {
                            if (isRefreshing || pullToRefreshState.distanceFraction > 0f) {
                                FeedRefreshIndicator(
                                    isRefreshing = isRefreshing,
                                    progress = pullToRefreshState.distanceFraction,
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = innerPadding.calculateTopPadding())
                                )
                            }
                        }
                    ) {
                        if (videoFeed.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    strokeWidth = 2.dp, modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .nestedScroll(nestedScrollConnection)
                                    .testTag("home_video_feed_list"),
                                contentPadding = innerPadding,
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                // Keep the opening viewport focused on long-form previews.
                                val topVideos = videoFeed.take(6)
                                items(
                                    items = topVideos,
                                    key = { video -> "top_${video.id}" }
                                ) { video ->
                                    VideoCard(
                                        video = video,
                                        onVideoClick = {
                                            if (video.isUserPreview) {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Opening Settings to edit your preview...")
                                                }
                                                onNavigateToSettings()
                                            } else {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Demo item: '${video.title}'",
                                                        withDismissAction = true
                                                    )
                                                }
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // Two-column Shorts shelf.
                                item {
                                    ShortsShelfView(
                                        shorts = shortsFeed,
                                        onShortClick = { shortTitle ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Opening Short: $shortTitle")
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                // 3. The remaining items of the stream (which includes user's trailing previews)
                                if (videoFeed.size > 6) {
                                    val trailingVideos = videoFeed.drop(6)
                                    items(
                                        items = trailingVideos,
                                        key = { video -> "trail_${video.id}" }
                                    ) { video ->
                                        VideoCard(
                                            video = video,
                                            onVideoClick = {
                                                if (video.isUserPreview) {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Opening Settings to edit your preview...")
                                                    }
                                                    onNavigateToSettings()
                                                } else {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Demo item: '${video.title}'",
                                                            withDismissAction = true
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                FeedStatusBarProtection(
                    color = feedBackground,
                    hiddenFraction = {
                        if (isRefreshing || pullToRefreshState.distanceFraction > 0f) 0f
                        else -topBarOffsetHeightPx / topBarHeightPx.coerceAtLeast(1f)
                    },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
private fun FeedCategoryChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    leadingIcon: ImageVector? = null
) {
    val colors = MaterialTheme.colorScheme
    val interactions = remember { MutableInteractionSource() }
    Box(
        modifier.heightIn(min = 48.dp)
            .clickable(interactionSource = interactions, indication = null, role = Role.Tab, onClick = onClick)
            .semantics { this.selected = selected },
        contentAlignment = Alignment.Center
    ) {
        Row(
            Modifier.background(
                if (selected) colors.onSurface else colors.surfaceVariant, RoundedCornerShape(8.dp)
            ).indication(interactions, ripple(color = if (selected) colors.surface else feedFeedbackColor()))
                .then(
                    if (icon != null) Modifier.size(width = 42.dp, height = 32.dp)
                    else Modifier.heightIn(min = 32.dp)
                        .padding(start = if (leadingIcon != null) 10.dp else 12.dp, end = 12.dp)
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, "Explore", tint = if (selected) colors.surface else colors.onSurface, modifier = Modifier.size(24.dp))
            } else {
                if (leadingIcon != null) {
                    Icon(
                        leadingIcon,
                        null, tint = if (selected) colors.surface else colors.onSurface, modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text.orEmpty(), color = if (selected) colors.surface else colors.onSurface,
                    fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 18.sp,
                    letterSpacing = 0.2.sp,
                    fontWeight = FontWeight.Medium, maxLines = 1,
                    modifier = Modifier.padding(vertical = 6.dp).offset(y = 0.5.dp)
                )
            }
        }
    }
}

@Composable
fun ShortsShelfView(
    onShortClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    shorts: List<FeedShort> = MockShortFeed.initial()
) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("home_shorts_shelf")
    ) {
        Row(
            Modifier.fillMaxWidth().height(44.dp).padding(start = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShortsIcon(modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                "Shorts", fontSize = 18.sp, lineHeight = 21.sp, fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.12.sp,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f).offset(y = 1.dp)
            )
            FeedIconButton(
                Icons.Outlined.MoreHoriz, "Shorts options",
                onClick = { onShortClick("Shorts options") },
                modifier = Modifier.offset(x = 2.dp).size(36.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            shorts.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { short ->
                        ShortCardItem(
                            short = short,
                            onShortClick = onShortClick, modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShortCardItem(
    short: FeedShort,
    onShortClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val title = short.title
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxWidth().aspectRatio(2f / 3f)
            .clickable { onShortClick(title) }
    ) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
            AsyncImage(
                model = short.thumbnailUrl,
                contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color.Transparent, 0.5f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.8f)
                    )
                )
            )
            Text(
                title, color = Color.White, fontWeight = FontWeight.Medium,
                fontSize = 14.sp, lineHeight = 17.75.sp, fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.15.sp,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
            )
            FeedIconButton(
                Icons.Outlined.MoreHoriz, "Options for $title",
                onClick = { onShortClick(title) },
                modifier = Modifier.align(Alignment.TopEnd).size(48.dp),
                tint = Color.White
            )
        }
    }
}
