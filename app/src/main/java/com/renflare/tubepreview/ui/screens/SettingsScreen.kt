package com.renflare.tubepreview.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.renflare.tubepreview.data.CreatorSettings
import com.renflare.tubepreview.ui.theme.BrandRed
import com.renflare.tubepreview.ui.components.glassBorder
import com.renflare.tubepreview.ui.components.glassBorderStroke
import com.renflare.tubepreview.ui.components.GlassTextField
import com.renflare.tubepreview.ui.components.PlumpIcons
import com.renflare.tubepreview.ui.components.PlumpIconButton
import com.renflare.tubepreview.ui.components.bouncyClickable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: CreatorSettings,
    onNavigateBack: () -> Unit,
    onUpdateChannelName: (String) -> Unit,
    onUpdateProfilePic: (Uri) -> Unit,
    onUpdateThumbnail: (Uri) -> Unit,
    onUpdateDarkMode: (Boolean) -> Unit,
    onUpdateFeedSize: (Int) -> Unit,
    onUpdatePreviewTitle: (String) -> Unit,
    onUpdatePreviewViews: (String) -> Unit,
    onUpdatePreviewDuration: (String) -> Unit,
    onUpdatePreviewInterval: (Int) -> Unit,
    onUpdatePlaceholderDefaults: (String, String, String) -> Unit,
    onResetAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var showFontLicense by remember { mutableStateOf(false) }
    if (showFontLicense) {
        val license = remember { context.assets.open("licenses/nunito.txt").bufferedReader().use { it.readText() } }
        AlertDialog(
            onDismissRequest = { showFontLicense = false },
            title = { Text("Nunito font license") },
            text = { Text(license, modifier = Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = { TextButton(onClick = { showFontLicense = false }) { Text("Done") } }
        )
    }
    var showAssetLicenses by remember { mutableStateOf(false) }
    if (showAssetLicenses) {
        AlertDialog(
            onDismissRequest = { showAssetLicenses = false },
            title = { Text("Third-party licenses") },
            text = {
                Column {
                    Text("Free icons from Streamline, adapted for TubePreview. Licensed under CC BY 4.0.")
                    TextButton(onClick = { uriHandler.openUri("https://streamlinehq.com") }) {
                        Text("Streamline")
                    }
                    TextButton(onClick = { uriHandler.openUri("https://creativecommons.org/licenses/by/4.0/") }) {
                        Text("Creative Commons Attribution 4.0")
                    }
                    TextButton(onClick = {
                        showAssetLicenses = false
                        showFontLicense = true
                    }) { Text("Nunito · Open Font License") }
                }
            },
            confirmButton = { TextButton(onClick = { showAssetLicenses = false }) { Text("Done") } }
        )
    }
    val focusManager = LocalFocusManager.current
    var showResetConfirmation by rememberSaveable { mutableStateOf(false) }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Reset your preview?") },
            text = { Text("Your uploaded images and custom preview settings will be removed.") },
            confirmButton = {
                TextButton(onClick = {
                    focusManager.clearFocus(force = true)
                    showResetConfirmation = false
                    onResetAll()
                }) { Text("Reset", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    // Form inputs initialized from settings
    var channelNameInput by remember(settings.channelName) { mutableStateOf(settings.channelName) }
    var previewTitleInput by remember(settings.previewTitle) { mutableStateOf(settings.previewTitle) }
    var previewViewsInput by remember(settings.previewViews) { mutableStateOf(settings.previewViews) }
    var previewDurationInput by remember(settings.previewDuration) { mutableStateOf(settings.previewDuration) }

    // Custom placeholder overrides
    var placeholderTitleInput by remember(settings.placeholderTitleDefault) { mutableStateOf(settings.placeholderTitleDefault) }
    var placeholderViewsInput by remember(settings.placeholderViewsDefault) { mutableStateOf(settings.placeholderViewsDefault) }
    var placeholderDurationInput by remember(settings.placeholderDurationDefault) { mutableStateOf(settings.placeholderDurationDefault) }

    // Media launchers
    val profilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { onUpdateProfilePic(it) } }

    val thumbnailPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { onUpdateThumbnail(it) } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlumpIconButton(
                    icon = PlumpIcons.Back,
                    contentDescription = "Return to home feed list screen",
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("settings_back_button")
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Make it yours",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // Section 1: Channel Branding Settings
            SectionHeader(icon = PlumpIcons.UserSolid, title = "Channel")
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                border = glassBorderStroke(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile picture avatar edit row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .glassBorder(CircleShape)
                                .clickable {
                                    try {
                                        profilePicker.launch("image/*")
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "No photo picker application available on this device", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                                .testTag("settings_avatar_box"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (settings.profilePictureUri.isNotEmpty()) {
                                AsyncImage(
                                    model = settings.profilePictureUri,
                                    contentDescription = "Your channel pic placeholder",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = PlumpIcons.Camera,
                                    contentDescription = "Pick photo icon",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            FilledTonalButton(
                                onClick = {
                                    try {
                                        profilePicker.launch("image/*")
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "No photo picker application available on this device", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = glassBorderStroke(),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("change_avatar_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = PlumpIcons.Gallery,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Choose photo",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        "Profile photos: up to 2 MB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Channel Name Outlined text box
                    GlassTextField(
                        value = channelNameInput,
                        onValueChange = {
                            channelNameInput = it
                            onUpdateChannelName(it)
                        },
                        label = "Channel name",
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth().testTag("settings_channel_name_input")
                    )
                }
            }

            // Section 2: Custom Preview Videocard details
            SectionHeader(icon = PlumpIcons.PlaySolid, title = "Video")
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                border = glassBorderStroke(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Thumbnail selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .glassBorder()
                                .clickable {
                                    try {
                                        thumbnailPicker.launch("image/*")
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "No photo picker application available on this device", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                                .testTag("settings_thumbnail_box"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (settings.previewThumbnailUri.isNotEmpty()) {
                                AsyncImage(
                                    model = settings.previewThumbnailUri,
                                    contentDescription = "Selected custom thumbnail image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Tap indicator
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = PlumpIcons.Edit,
                                        contentDescription = "Change Image overlay",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = PlumpIcons.Gallery,
                                        contentDescription = "Pick thumbnail icon",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Tap to choose a thumbnail",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        "Up to 2 MB • Recommended: 1280 × 720 (16:9)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("thumbnail_upload_guidance")
                    )

                    // Outlined Textfields for Title, views, duration
                    GlassTextField(
                        value = previewTitleInput,
                        onValueChange = {
                            previewTitleInput = it
                            onUpdatePreviewTitle(it)
                        },
                        label = "Title",
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth().testTag("settings_title_input"),
                        maxLines = 3
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassTextField(
                            value = previewViewsInput,
                            onValueChange = {
                                previewViewsInput = it
                                onUpdatePreviewViews(it)
                            },
                            label = "Views",
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth().testTag("settings_views_input"),
                            singleLine = true
                        )

                        GlassTextField(
                            value = previewDurationInput,
                            onValueChange = {
                                previewDurationInput = it
                                onUpdatePreviewDuration(it)
                            },
                            label = "Duration",
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth().testTag("settings_duration_input"),
                            singleLine = true
                        )
                    }
                }
            }

            // Section 3: Feed Configuration
            SectionHeader(icon = PlumpIcons.Feed, title = "Feed")
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                border = glassBorderStroke(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Large Card Item limits (10, 15, 20, 25, 30)
                    Column {
                        Text(
                            text = "Surrounding videos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val optionLimits = listOf(10, 15, 20, 25, 30)
                            optionLimits.forEach { limit ->
                                val isSelected = settings.feedSize == limit
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .background(if (isSelected) BrandRed else MaterialTheme.colorScheme.surface)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) BrandRed else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                        .heightIn(min = 48.dp)
                                        .bouncyClickable(role = Role.RadioButton) { onUpdateFeedSize(limit) }
                                        .semantics { selected = isSelected; contentDescription = "$limit surrounding videos" }
                                        .padding(vertical = 12.dp)
                                        .testTag("feed_size_capsule_$limit"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$limit",
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    // Configurable custom thumbnail recurrence period interval
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Preview every",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = { onUpdatePreviewInterval((settings.previewInterval - 1).coerceAtLeast(2)) },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f), CircleShape).glassBorder(CircleShape).size(48.dp).semantics { contentDescription = "Decrease preview interval" }.testTag("interval_decrease")
                            ) {
                                Text("−", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = "${settings.previewInterval} videos",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.testTag("interval_text_display")
                            )

                            IconButton(
                                onClick = { onUpdatePreviewInterval((settings.previewInterval + 1).coerceAtMost(10)) },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f), CircleShape).glassBorder(CircleShape).size(48.dp).semantics { contentDescription = "Increase preview interval" }.testTag("interval_increase")
                            ) {
                                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Mode theme switcher line
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (settings.isDarkMode) PlumpIcons.Moon else PlumpIcons.Sun,
                                contentDescription = "Active color icon"
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "Dark theme",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Switch(
                            checked = settings.isDarkMode,
                            onCheckedChange = onUpdateDarkMode,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BrandRed,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag("theme_switch_toggle").semantics { contentDescription = "Dark theme" }
                        )
                    }
                }
            }

            // Section 4: General Template Overrides
            SectionHeader(icon = PlumpIcons.Stars, title = "Surrounding video overrides")
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                border = glassBorderStroke(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Leave blank to use sample videos.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )

                    GlassTextField(
                        value = placeholderTitleInput,
                        onValueChange = {
                            placeholderTitleInput = it
                            onUpdatePlaceholderDefaults(it, placeholderViewsInput, placeholderDurationInput)
                        },
                        label = "Title",
                        placeholder = { Text("e.g. Recommended Video") },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth().testTag("placeholder_title_input")
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassTextField(
                            value = placeholderViewsInput,
                            onValueChange = {
                                placeholderViewsInput = it
                                onUpdatePlaceholderDefaults(placeholderTitleInput, it, placeholderDurationInput)
                            },
                            label = "Views",
                            placeholder = { Text("e.g. 100K views") },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth().testTag("placeholder_views_input"),
                            singleLine = true
                        )

                        GlassTextField(
                            value = placeholderDurationInput,
                            onValueChange = {
                                placeholderDurationInput = it
                                onUpdatePlaceholderDefaults(placeholderTitleInput, placeholderViewsInput, it)
                            },
                            label = "Duration",
                            placeholder = { Text("e.g. 10:00") },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth().testTag("placeholder_time_input"),
                            singleLine = true
                        )
                    }
                }
            }

            // Section 5: Reset Operations / System panel
            SectionHeader(icon = PlumpIcons.Warning, title = "Reset preview")
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                border = glassBorderStroke(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Remove your uploaded images and restore the default preview and feed settings.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { showResetConfirmation = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("reset_metadata_btn")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(PlumpIcons.Reset, "Trash icon")
                            Text("Reset to defaults", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            TextButton(
                onClick = { showAssetLicenses = true },
                modifier = Modifier.fillMaxWidth().testTag("settings_asset_licenses")
            ) {
                Icon(PlumpIcons.Feed, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Third-party licenses")
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier.width(36.dp),
                    color = BrandRed.copy(alpha = 0.4f)
                )
                Text(
                    text = buildAnnotatedString {
                        append("Made with ❤️ by ")
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append("renflare")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(BrandRed.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = BrandRed, modifier = Modifier.size(24.dp))
        }
        Text(title, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
    }
}
