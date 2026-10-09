package com.renflare.tubepreview.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.renflare.tubepreview.ui.components.ShortsIcon
import androidx.compose.ui.res.vectorResource
import com.renflare.tubepreview.R
import com.renflare.tubepreview.data.CreatorSettings
import com.renflare.tubepreview.data.FeedVideo
import com.renflare.tubepreview.ui.components.VideoCard
import com.renflare.tubepreview.ui.theme.BrandRed
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
    modifier: Modifier = Modifier
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
    val animatedBottomOffset by animateFloatAsState(
        bottomBarOffsetHeightPx, tween(120), label = "bottomBarOffset"
    )
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

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .onSizeChanged { topBarHeightPx = it.height.toFloat() }
                    .offset { IntOffset(x = 0, y = if (isRefreshing || pullToRefreshState.distanceFraction > 0f) 0 else topBarOffsetHeightPx.roundToInt()) }
                    .testTag("home_top_bar")
                    .background(MaterialTheme.colorScheme.background)
            ) {
                BoxWithConstraints(Modifier.statusBarsPadding().fillMaxWidth()) {
                    val compactHeader = maxWidth < 380.dp
                    val wordmarkSize = when {
                        maxWidth < 340.dp -> 18.sp
                        compactHeader -> 20.sp
                        else -> 22.sp
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                            .padding(start = 12.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            BrandLogo(
                                modifier = Modifier.size(if (compactHeader) 28.dp else 32.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "TubePreview",
                                fontSize = wordmarkSize,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        FeedIconButton(
                            icon = if (settings.isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = "Toggle theme",
                            onClick = { onToggleTheme(!settings.isDarkMode) },
                            modifier = Modifier.testTag("home_theme_toggle")
                        )
                        FeedIconButton(
                            icon = Icons.Outlined.NotificationsNone,
                            contentDescription = "Notifications",
                            onClick = { scope.launch { snackbarHostState.showSnackbar("No notifications yet") } },
                            modifier = Modifier.testTag("home_notifications")
                        )
                        FeedIconButton(
                            icon = Icons.Outlined.Search,
                            contentDescription = "Search videos",
                            onClick = { scope.launch { snackbarHostState.showSnackbar("Search is not available in the preview feed") } },
                            modifier = Modifier.testTag("home_search")
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item(key = "explore") {
                        FeedCategoryChip(
                            selected = selectedCategory == "Explore",
                            onClick = { selectedCategory = "Explore" },
                            modifier = Modifier.testTag("home_explore_tab"),
                            icon = ImageVector.vectorResource(R.drawable.feed_explore)
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
                Spacer(modifier = Modifier.height(4.dp))
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .onSizeChanged { bottomBarHeightPx = it.height.toFloat() }
                    .offset { IntOffset(x = 0, y = if (isRefreshing || pullToRefreshState.distanceFraction > 0f) 0 else animatedBottomOffset.roundToInt()) }
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("home_bottom_bar")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val navItems = listOf(
                        "Home" to Icons.Filled.Home,
                        "Shorts" to Icons.Outlined.SmartDisplay,
                        "Create" to Icons.Outlined.Add,
                        "Subscriptions" to Icons.Outlined.Subscriptions,
                        "You" to Icons.Outlined.AccountCircle
                    )
                    navItems.forEach { (title, icon) ->
                        val isCreate = title == "Create"
                        val isYou = title == "You"
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(role = Role.Tab) {
                                    when {
                                        isYou || isCreate -> onNavigateToSettings()
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
                                    modifier = Modifier.size(40.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, "Edit your preview", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                                }
                            } else {
                                if (isYou && settings.profilePictureUri.isNotEmpty()) {
                                    AsyncImage(
                                        model = settings.profilePictureUri,
                                        contentDescription = "Your profile",
                                        contentScale = ContentScale.Crop,
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
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = if (title == "Subscriptions" && LocalDensity.current.fontScale > 1.15f) "Subs" else title,
                                    modifier = Modifier.semantics { if (title == "Subscriptions") contentDescription = title },
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 11.sp,
                                    lineHeight = 12.sp,
                                    fontWeight = if (title == "Home") FontWeight.Medium else FontWeight.Normal,
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
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                refreshRequested = true
                onRefresh()
            },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            indicator = {
                if (isRefreshing || pullToRefreshState.distanceFraction > 0f) {
                    FeedRefreshIndicator(
                        isRefreshing = isRefreshing,
                        progress = pullToRefreshState.distanceFraction,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = innerPadding.calculateTopPadding() + 72.dp)
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
                    CircularProgressIndicator(color = BrandRed)
                }
            } else {
                LazyColumn(
                    state = listState,
                    userScrollEnabled = !isRefreshing,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection)
                        .testTag("home_video_feed_list"),
                    contentPadding = innerPadding,
                    verticalArrangement = Arrangement.spacedBy(0.dp) // seamless vertical card flow matching YouTube!
                ) {
                    // Keep the Shorts shelf near the top while refresh varies the preview position.
                    val topVideos = videoFeed.take(2)
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
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Two-column Shorts shelf.
                    item {
                        ShortsShelfView(
                            onShortClick = { shortTitle ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Opening Short: $shortTitle")
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 3. The remaining items of the stream (which includes user's trailing previews)
                    if (videoFeed.size > 2) {
                        val trailingVideos = videoFeed.drop(2)
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
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
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
    icon: ImageVector? = null
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { this.selected = selected },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.clip(RoundedCornerShape(8.dp))
                .background(if (selected) colors.onSurface else colors.surfaceVariant)
                .then(
                    if (icon != null) Modifier.size(width = 42.dp, height = 32.dp)
                    else Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(icon, "Explore", tint = if (selected) colors.surface else colors.onSurface,
                    modifier = Modifier.size(24.dp))
            } else {
                Text(text.orEmpty(), color = if (selected) colors.surface else colors.onSurface,
                    fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun FeedIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier.size(48.dp)) {
        Icon(icon, contentDescription, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun ShortsShelfView(
    onShortClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp)
            .testTag("home_shorts_shelf")
    ) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShortsIcon(modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                "Shorts", fontSize = 20.sp, fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            FeedIconButton(
                Icons.Outlined.MoreHoriz, "Shorts options",
                onClick = { onShortClick("Shorts options") }
            )
        }
        val titles = listOf(
            "This camera fits in my pocket",
            "My favorite five-minute breakfast",
            "I could listen to this keyboard all day",
            "The city after everyone goes home"
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            titles.chunked(2).forEachIndexed { row, pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEachIndexed { column, title ->
                        ShortCardItem(
                            title = title, gradientIndex = row * 2 + column,
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
    title: String,
    gradientIndex: Int,
    onShortClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxWidth().aspectRatio(2f / 3f)
            .clickable { onShortClick(title) }
    ) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
            AsyncImage(
                model = com.renflare.tubepreview.data.MockFeedProvider.photoUrl(
                    listOf(
                        "1516035069371-29a1b244cc32",
                        "1490645935967-10de6ba17061",
                        "1587829741301-dc798b83add3",
                        "1480714378408-67cf0d13bc1b"
                    )[gradientIndex.coerceIn(0, 3)], 480, 720
                ),
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
                fontSize = 14.sp, lineHeight = 18.sp, fontFamily = FontFamily.SansSerif,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
            )
            IconButton(
                onClick = { onShortClick(title) },
                modifier = Modifier.align(Alignment.TopEnd).size(48.dp)
            ) {
                Icon(Icons.Outlined.MoreHoriz, "Options for $title",
                    tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}
