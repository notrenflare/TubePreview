package com.renflare.tubepreview.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.renflare.tubepreview.data.*

@Composable
fun ABPreviewToggle(enabled: Boolean, onEnabledChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("A/B preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Compare titles and thumbnails", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = enabled,
            onCheckedChange = onEnabledChange,
            colors = settingsSwitchColors(),
            modifier = Modifier.testTag("ab_toggle").semantics {
                contentDescription = "A/B preview"
            }
        )
    }
}

@Composable
fun ABPreviewEditor(
    settings: CreatorSettings,
    onModeChange: (ComparisonMode) -> Unit,
    onTitleChange: (Int, String) -> Unit,
    onThumbnailChange: (Int, Uri) -> Unit,
    onClearOption: (Int) -> Unit,
    onSelectOption: (Int) -> Unit
) {
    var editingIndex by rememberSaveable { mutableIntStateOf(settings.activePreviewIndex()) }
    var pickingIndex by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onThumbnailChange(pickingIndex, it) }
    }
    val mode = settings.abPreview.mode
    val variants = settings.previewVariants()
    val ready = settings.readyPreviewIndices()
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ComparisonMode.entries.forEach { option ->
                ChoiceButton(
                    label = option.label, selected = mode == option, modifier = Modifier.weight(1f).testTag("ab_mode_${option.name}"),
                    onClick = { focusManager.clearFocus(); onModeChange(option) }
                )
            }
        }
        Text(
            "Pull to refresh to cycle through ready options. Visual comparison only.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            variants.indices.forEach { index ->
                ChoiceButton(
                    label = "Option ${'A' + index}", selected = editingIndex == index,
                    modifier = Modifier.weight(1f).testTag("ab_edit_${index}"),
                    onClick = { focusManager.clearFocus(); editingIndex = index }
                )
            }
        }
        Text(
            if (ready.size < 2) "Add at least two ${when (mode) {
                ComparisonMode.TITLES -> "titles"
                ComparisonMode.THUMBNAILS -> "thumbnails"
                ComparisonMode.BOTH -> "title and thumbnail pairs"
            }} to compare." else "${ready.size} options ready · ${ready.joinToString(" → ") { ('A' + it).toString() }} → ${'A' + ready.first()}",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("ab_readiness")
        )
        val imageIndex = if (mode == ComparisonMode.TITLES) 0 else editingIndex
        Text(if (mode == ComparisonMode.TITLES) "Shared thumbnail" else "Thumbnail ${'A' + editingIndex}",
            style = MaterialTheme.typography.labelLarge)
        Box(
            Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.background).glassBorder()
                .clickable {
                    focusManager.clearFocus()
                    pickingIndex = imageIndex
                    try { picker.launch("image/*") } catch (_: Exception) {
                        Toast.makeText(context, "No photo picker application available on this device", Toast.LENGTH_LONG).show()
                    }
                }.testTag("ab_thumbnail_${imageIndex}"),
            contentAlignment = Alignment.Center
        ) {
            if (variants[imageIndex].thumbnailUri.isNotBlank()) {
                AsyncImage(model = variants[imageIndex].thumbnailUri, contentDescription = "Thumbnail ${'A' + imageIndex}",
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)) {
                    Text("Change", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(PlumpIcons.Gallery, contentDescription = null, modifier = Modifier.size(36.dp))
                    Text("Choose a thumbnail", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        Text("Up to 2 MB each · 1280 × 720 recommended (16:9)",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val titleIndex = if (mode == ComparisonMode.THUMBNAILS) 0 else editingIndex
        key(titleIndex, mode) {
            GlassTextField(
                value = variants[titleIndex].title, onValueChange = { onTitleChange(titleIndex, it) },
                label = if (mode == ComparisonMode.THUMBNAILS) "Shared title" else "Title ${'A' + editingIndex}",
                shape = RoundedCornerShape(20.dp), maxLines = 3,
                modifier = Modifier.fillMaxWidth().testTag("ab_title_${titleIndex}")
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            if (editingIndex > 0) {
                TextButton(onClick = { focusManager.clearFocus(); onClearOption(editingIndex) }) {
                    Text("Clear ${'A' + editingIndex}")
                }
            } else Spacer(Modifier.width(1.dp))
            OutlinedButton(
                enabled = editingIndex in ready,
                onClick = { focusManager.clearFocus(); onSelectOption(editingIndex) },
                shape = RoundedCornerShape(20.dp), modifier = Modifier.testTag("ab_preview_selected")
            ) {
                Text(if (settings.activePreviewIndex() == editingIndex && editingIndex in ready)
                    "Showing ${'A' + editingIndex}" else "Preview ${'A' + editingIndex}")
            }
        }
    }
}

@Composable
private fun ChoiceButton(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(16.dp), border = glassBorderStroke(),
        modifier = modifier.heightIn(min = 48.dp).semantics { this.selected = selected }
            .clip(RoundedCornerShape(16.dp)).clickable(role = Role.Tab, onClick = onClick)
    ) {
        Box(Modifier.padding(horizontal = 4.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}
