package com.renflare.tubepreview.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.renflare.tubepreview.R
import com.renflare.tubepreview.data.AvatarCrop
import com.renflare.tubepreview.data.CreatorSettings
import com.renflare.tubepreview.ui.components.*
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import kotlinx.coroutines.launch

private val tourTitles = listOf(
    "Preview your\nYouTube thumbnail.",
    "Find your\nfavorite option.",
    "Refresh.\nCompare. Repeat.",
    "Make it\nyour channel."
)

private val tourDescriptions = listOf(
    "See your thumbnail and title in a YouTube-style feed before you publish.",
    "Compare up to three titles, thumbnails, or pairs with A/B preview in settings.",
    "Pull down to refresh the feed and cycle through your ready A/B options.",
    "Add a name and optional photo."
)

private val tourHints = listOf(
    "Add yours in Your custom feed.",
    "Try A, B, and C. This is a visual preview.",
    "Pull down on the card, or tap Refresh preview.",
    "No YouTube sign-in. Your images stay on this device."
)

@Composable
fun OnboardingScreen(
    settings: CreatorSettings,
    onSaveProfile: (String, Uri?) -> Unit,
    onNavigateNext: () -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    animationsReady: Boolean = true
) {
    val pager = rememberPagerState(pageCount = { tourTitles.size })
    val scope = rememberCoroutineScope()
    var moving by remember { mutableStateOf(false) }
    var finishing by rememberSaveable { mutableStateOf(false) }
    var channelName by rememberSaveable { mutableStateOf(settings.channelName) }
    var selectedPhoto by rememberSaveable { mutableStateOf<String?>(null) }
    var exampleOption by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val keyboardProgress = rememberTourKeyboardProgress()
    val keyboardVisible = keyboardProgress > 0f
    val page = pager.settledPage
    val lastPage = page == tourTitles.lastIndex
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedPhoto = it.toString() }
    }
    val choosePhoto: () -> Unit = {
        try {
            picker.launch("image/*")
        } catch (_: Exception) {
            android.widget.Toast.makeText(context, "No photo picker application available on this device",
                android.widget.Toast.LENGTH_LONG).show()
        }
    }
    fun goTo(target: Int) {
        if (moving || pager.isScrollInProgress || finishing) return
        moving = true
        if (keyboardVisible) {
            focusManager.clearFocus()
            keyboard?.hide()
        }
        scope.launch {
            try { pager.animateScrollToPage(target.coerceIn(tourTitles.indices), animationSpec = tween(360)) }
            finally { moving = false }
        }
    }
    fun finish(useDefaults: Boolean = false) {
        if (moving || pager.isScrollInProgress || finishing) return
        finishing = true
        onSaveProfile(
            if (useDefaults) settings.channelName else channelName.trim(),
            if (useDefaults) null else selectedPhoto?.let(Uri::parse)
        )
        onNavigateNext()
    }
    BackHandler(enabled = page > 0 && !keyboardVisible && !finishing) { goTo(page - 1) }

    Scaffold(modifier.fillMaxSize(), contentWindowInsets = WindowInsets.safeDrawing) { insets ->
        BoxWithConstraints(
            Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets),
            contentAlignment = Alignment.TopCenter
        ) {
            val imeHeight = with(LocalDensity.current) { WindowInsets.ime.getBottom(this).toDp() }
            val wide = maxWidth >= 600.dp && maxWidth > maxHeight + imeHeight
            Column(Modifier.widthIn(max = if (wide) 840.dp else 560.dp).fillMaxSize()) {
                Row(Modifier.fillMaxWidth().tourKeyboardCollapse(keyboardProgress)
                    .padding(horizontal = 16.dp, vertical = if (wide) 0.dp else 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    BrandLogo(Modifier.size(32.dp))
                    Spacer(Modifier.width(8.dp))
                    if (LocalDensity.current.fontScale <= 1.25f) {
                        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    TourHeaderAction(pager, finishing,
                        onSkip = { goTo(tourTitles.lastIndex) },
                        onDefaults = { finish(useDefaults = true) })
                    Spacer(Modifier.width(8.dp))
                    PlumpIconButton(
                        if (settings.isDarkMode) PlumpIcons.Sun else PlumpIcons.Moon,
                        if (settings.isDarkMode) "Switch to light mode" else "Switch to dark mode",
                        { onToggleTheme(!settings.isDarkMode) },
                        Modifier.testTag("onboarding_theme_toggle")
                    )
                }
                HorizontalPager(
                    state = pager,
                    userScrollEnabled = !finishing && !keyboardVisible,
                    modifier = Modifier.weight(1f).fillMaxWidth().testTag("onboarding_pager")
                ) { index ->
                    TourPage(index, wide, keyboardVisible, channelName, { channelName = it },
                        selectedPhoto ?: settings.profilePictureUri,
                        if (selectedPhoto == null) settings.avatarCrop else AvatarCrop(), choosePhoto,
                        keyboardProgress = keyboardProgress,
                        entryActive = animationsReady && pager.currentPage == index,
                        exampleOption = exampleOption, onExampleOptionChange = { exampleOption = it })
                }
                TourNavigation(
                    pager = pager, wide = wide, keyboardVisible = keyboardVisible,
                    finishing = finishing, canFinish = channelName.trim().isNotEmpty(),
                    onStep = ::goTo,
                    onNext = { if (lastPage) finish() else goTo(page + 1) },
                    keyboardProgress = keyboardProgress
                )
            }
        }
    }
}

@Composable
internal fun TourPage(
    index: Int, wide: Boolean, keyboardVisible: Boolean,
    channelName: String, onNameChange: (String) -> Unit, photo: String,
    crop: AvatarCrop, onChoosePhoto: () -> Unit,
    keyboardProgress: Float = if (keyboardVisible) 1f else 0f,
    entryActive: Boolean = true,
    exampleOption: Int = 0,
    onExampleOptionChange: (Int) -> Unit = {}
) {
    BoxWithConstraints(Modifier.fillMaxSize().testTag("onboarding_page_$index")) {
        val availableWidth = maxWidth
        val fontScale = LocalDensity.current.fontScale
        val compact = remember(maxWidth, fontScale) { maxHeight < 440.dp || fontScale > 1.25f }
        val padding = if (compact) 16.dp else 24.dp
        val gap = if (compact) 8.dp else 16.dp
        val profile = index == tourTitles.lastIndex
        val headingSize = if (fontScale > 1.25f) 20.sp else if (compact) 26.sp else 34.sp
        val headingStyle = MaterialTheme.typography.headlineLarge.copy(fontSize = headingSize, lineHeight = headingSize * 1.15f)
        val bodyStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = if (compact) 14.sp else 16.sp, lineHeight = if (compact) 20.sp else 24.sp)
        val hintStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp)
        @Composable
        fun Copy() {
            Column(Modifier.fillMaxWidth().testTag("onboarding_copy_$index").tourKeyboardCollapse(keyboardProgress),
                verticalArrangement = Arrangement.spacedBy(gap)) {
                Text(tourTitles[index], style = headingStyle,
                    modifier = Modifier.fillMaxWidth().tourEntrance(entryActive, 0)
                        .semantics { heading() }.testTag("onboarding_heading_$index"))
                if (!profile || !compact) {
                    Text(tourDescriptions[index], style = bodyStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().tourEntrance(entryActive, 1).testTag("onboarding_description_$index"))
                }
            }
        }
        @Composable
        fun Hint() {
            Text(tourHints[index], style = hintStyle, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().tourEntrance(entryActive, 3).testTag("onboarding_hint_$index"))
        }
        @Composable
        fun Visual(modifier: Modifier) {
            if (index < tourTitles.lastIndex) {
                TourIllustration(index, modifier.testTag("onboarding_visual_$index").tourEntrance(entryActive, 2),
                    option = exampleOption, onOptionChange = onExampleOptionChange)
            } else {
                BoxWithConstraints(modifier, contentAlignment = Alignment.TopCenter) {
                    val compactProfile = compact
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TourProfileSetup(channelName, onNameChange, photo, crop, onChoosePhoto,
                            compact = compactProfile, editing = keyboardVisible,
                            modifier = Modifier.tourEntrance(entryActive, 2), keyboardProgress = keyboardProgress)
                        if (!wide) Hint()
                    }
                }
            }
        }
        if (wide) {
            Row(Modifier.fillMaxSize().padding(horizontal = padding, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(Modifier.tourKeyboardCollapse(keyboardProgress, horizontal = true)
                    .width((availableWidth - padding * 2 - 24.dp) / 2).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(gap)) {
                    Copy()
                    Spacer(Modifier.weight(1f))
                    Hint()
                }
                Visual(Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            Column(Modifier.fillMaxSize().padding(horizontal = padding, vertical = if (compact) 8.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(gap,
                    if (profile) Alignment.CenterVertically else Alignment.Top)) {
                Copy()
                Visual(if (profile) Modifier.fillMaxWidth() else Modifier.weight(1f).fillMaxWidth())
                if (!profile) Hint()
            }
        }
    }
}

@Composable
internal fun TourProfileSetup(
    name: String, onNameChange: (String) -> Unit, photo: String, crop: AvatarCrop,
    onChoosePhoto: () -> Unit, compact: Boolean, editing: Boolean,
    modifier: Modifier = Modifier,
    keyboardProgress: Float = if (editing) 1f else 0f
) {
    @Composable
    fun Photo() {
        Box(Modifier.size(if (compact) 48.dp else lerp(64.dp, 48.dp, keyboardProgress)).clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer).glassBorder(CircleShape)
            .clickable(role = Role.Button, onClickLabel = "Choose channel photo", onClick = onChoosePhoto)
            .semantics { contentDescription = "Choose channel photo" }.testTag("avatar_picker_box"),
            contentAlignment = Alignment.Center) {
            if (photo.isNotEmpty()) {
                AvatarPhoto(photo, crop, null, modifier = Modifier.fillMaxSize())
            } else {
                Icon(PlumpIcons.Gallery, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
            }
        }
    }
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)),
        border = glassBorderStroke(), modifier = modifier.fillMaxWidth().testTag("onboarding_profile_card")
    ) {
        Column(Modifier.padding(if (compact) 12.dp else lerp(20.dp, 12.dp, keyboardProgress)),
            verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else lerp(16.dp, 12.dp, keyboardProgress))) {
                Row(Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Photo()
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(if (photo.isEmpty()) "Add photo" else "Change photo",
                            style = if (compact) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium)
                        Text("Optional · Up to 2 MB",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    GlassTextField(name, onNameChange, label = "Channel name", placeholder = { Text("Your channel") },
                        singleLine = true, modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input"))
                }
            }
        }
    }
}

@Preview(name = "First launch · Dark", widthDp = 393, heightDp = 852)
@Composable
private fun OnboardingDarkPreview() = OnboardingPreview(true)

@Preview(name = "First launch · Light", widthDp = 393, heightDp = 852)
@Composable
private fun OnboardingLightPreview() = OnboardingPreview(false)

@Preview(name = "First launch · Compact", widthDp = 320, heightDp = 568, fontScale = 1.5f)
@Composable
private fun OnboardingCompactPreview() = OnboardingPreview(true)

@Preview(name = "First launch · Landscape", widthDp = 640, heightDp = 360)
@Composable
private fun OnboardingLandscapePreview() = OnboardingPreview(true)

@Composable
private fun OnboardingPreview(darkTheme: Boolean) {
    TubePreviewTheme(darkTheme = darkTheme) {
        OnboardingScreen(
            CreatorSettings("renflare", "", "", darkTheme, 15, "", "", "", "", 5, false, "", "", ""),
            onSaveProfile = { _, _ -> }, onNavigateNext = {}, onToggleTheme = {}
        )
    }
}
