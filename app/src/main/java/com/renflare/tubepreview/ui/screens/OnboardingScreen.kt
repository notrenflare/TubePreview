package com.renflare.tubepreview.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.renflare.tubepreview.R
import com.renflare.tubepreview.data.CreatorSettings
import com.renflare.tubepreview.ui.components.BrandLogo
import com.renflare.tubepreview.ui.components.PlumpIcons
import com.renflare.tubepreview.ui.components.PlumpIconButton
import com.renflare.tubepreview.ui.components.PlumpButton
import com.renflare.tubepreview.ui.components.GlassTextField
import com.renflare.tubepreview.ui.components.glassBorder
import com.renflare.tubepreview.ui.components.glassBorderStroke
import com.renflare.tubepreview.ui.theme.TubePreviewTheme

@Composable
fun OnboardingScreen(
    settings: CreatorSettings,
    onSaveProfile: (String, Uri?) -> Unit,
    onNavigateNext: () -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var channelNameInput by rememberSaveable { mutableStateOf(settings.channelName) }
    var selectedProfileUriString by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedProfileUri = selectedProfileUriString?.let(Uri::parse)
    val profileImage = selectedProfileUriString ?: settings.profilePictureUri
    val context = LocalContext.current

    val profilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedProfileUriString = it.toString() }
    }
    val choosePhoto: () -> Unit = {
        try {
            profilePickerLauncher.launch("image/*")
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                context,
                "No photo picker application available on this device",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BrandLogo(
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = stringResource(R.string.app_name),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                        PlumpIconButton(
                            icon = if (settings.isDarkMode) PlumpIcons.Sun else PlumpIcons.Moon,
                            contentDescription = if (settings.isDarkMode) "Switch to light mode" else "Switch to dark mode",
                            onClick = { onToggleTheme(!settings.isDarkMode) },
                            modifier = Modifier.testTag("onboarding_theme_toggle")
                        )
                    }
                    Text(
                        text = "Preview before\nyou publish.",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "See how your thumbnails look in a YouTube-style home feed.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)
                    ),
                    border = glassBorderStroke(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Your preview channel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "No YouTube sign-in needed.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = if (profileImage.isEmpty()) "Add channel photo" else "Change channel photo",
                                    onClick = choosePhoto
                                )
                                .testTag("avatar_picker_box")
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .glassBorder(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (profileImage.isNotEmpty()) {
                                    AsyncImage(
                                        model = profileImage,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = channelNameInput.trim().take(1).uppercase().ifEmpty { "R" },
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (profileImage.isEmpty()) "Add photo" else "Change photo",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Optional · Up to 2 MB",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = PlumpIcons.Gallery,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        GlassTextField(
                            value = channelNameInput,
                            onValueChange = { channelNameInput = it },
                            label = "Channel name",
                            placeholder = { Text("renflare") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_name_input")
                        )
                    }
                }
                PlumpButton(
                    text = "Start previewing",
                    onClick = {
                        onSaveProfile(channelNameInput.trim(), selectedProfileUri)
                        onNavigateNext()
                    },
                    icon = PlumpIcons.Forward,
                    enabled = channelNameInput.trim().isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_save_button")
                )
            }
        }
    }
}

@Preview(name = "First launch · Dark", widthDp = 393, heightDp = 852)
@Composable
private fun OnboardingDarkPreview() {
    OnboardingPreview(darkTheme = true)
}

@Preview(name = "First launch · Light", widthDp = 393, heightDp = 852)
@Composable
private fun OnboardingLightPreview() {
    OnboardingPreview(darkTheme = false)
}

@Preview(name = "First launch · Compact", widthDp = 320, heightDp = 568, fontScale = 1.3f)
@Composable
private fun OnboardingCompactPreview() {
    OnboardingPreview(darkTheme = true)
}

@Composable
private fun OnboardingPreview(darkTheme: Boolean) {
    TubePreviewTheme(darkTheme = darkTheme) {
        OnboardingScreen(
            settings = CreatorSettings(
                channelName = "renflare",
                profilePictureUri = "",
                previewThumbnailUri = "",
                isDarkMode = darkTheme,
                feedSize = 15,
                previewTitle = "",
                previewViews = "",
                previewDuration = "",
                previewUploadTime = "",
                previewInterval = 5,
                isOnboarded = false,
                placeholderTitleDefault = "",
                placeholderViewsDefault = "",
                placeholderDurationDefault = ""
            ),
            onSaveProfile = { _, _ -> },
            onNavigateNext = {},
            onToggleTheme = {}
        )
    }
}
