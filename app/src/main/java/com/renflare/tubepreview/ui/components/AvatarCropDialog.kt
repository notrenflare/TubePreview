package com.renflare.tubepreview.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.renflare.tubepreview.data.AvatarCrop

private val AvatarCropSaver = listSaver<AvatarCrop, Float>(
    save = { listOf(it.horizontalBias, it.verticalBias, it.zoom) },
    restore = { AvatarCrop(it[0], it[1], it[2]).sanitized() }
)

@Composable
fun AvatarCropDialog(
    photoUri: String,
    initialCrop: AvatarCrop,
    onDismiss: () -> Unit,
    onSave: (AvatarCrop) -> Unit
) {
    var crop by rememberSaveable(photoUri, stateSaver = AvatarCropSaver) { mutableStateOf(initialCrop.sanitized()) }
    var aspectRatio by remember(photoUri) { mutableFloatStateOf(0f) }
    var failed by remember(photoUri) { mutableStateOf(false) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    fun transform(panX: Float = 0f, panY: Float = 0f, scale: Float = 1f) {
        crop = crop.transform(viewport.width.toFloat(), viewport.height.toFloat(), aspectRatio, panX, panY, scale)
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxWidth().safeDrawingPadding().padding(16.dp), contentAlignment = Alignment.Center) {
            AppDialogSurface(
                modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().testTag("avatar_crop_dialog")
            ) {
                Column(
                    Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppDialogTitle("Adjust photo", Modifier.fillMaxWidth())
                    Box(
                        Modifier.widthIn(max = 280.dp).fillMaxWidth().aspectRatio(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .onSizeChanged { viewport = it }
                            .pointerInput(photoUri, aspectRatio) {
                                detectTransformGestures { centroid, pan, zoom, _ ->
                                    crop = crop.transform(
                                        size.width.toFloat(), size.height.toFloat(), aspectRatio,
                                        pan.x, pan.y, zoom, centroid.x, centroid.y
                                    )
                                }
                            }
                            .semantics {
                                contentDescription = "Channel photo crop preview"
                                customActions = listOf(
                                    CustomAccessibilityAction("Move photo left") { transform(panX = -viewport.width * 0.1f); true },
                                    CustomAccessibilityAction("Move photo right") { transform(panX = viewport.width * 0.1f); true },
                                    CustomAccessibilityAction("Move photo up") { transform(panY = -viewport.height * 0.1f); true },
                                    CustomAccessibilityAction("Move photo down") { transform(panY = viewport.height * 0.1f); true },
                                    CustomAccessibilityAction("Zoom in") { transform(scale = 1.1f); true },
                                    CustomAccessibilityAction("Zoom out") { transform(scale = 1f / 1.1f); true }
                                )
                            }.testTag("avatar_crop_preview")
                    ) {
                        AvatarPhoto(
                            photoUri, crop, null, Modifier.fillMaxSize(),
                            onLoaded = { aspectRatio = it; failed = false },
                            onError = { failed = true }
                        )
                        if (aspectRatio == 0f && !failed) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center).size(32.dp))
                        }
                        Box(Modifier.fillMaxSize().glassBorder(CircleShape))
                    }
                    Text(
                        if (failed) "Couldn't load this photo. Try choosing it again."
                        else "Drag to reposition. Pinch or use the slider to zoom.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Zoom", fontWeight = FontWeight.Bold)
                            Text("${(crop.zoom * 100).toInt()}%")
                        }
                        Slider(
                            value = crop.zoom,
                            onValueChange = { transform(scale = it / crop.zoom) },
                            valueRange = 1f..4f,
                            colors = SliderDefaults.colors(
                                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            ),
                            enabled = aspectRatio > 0f && !failed,
                            modifier = Modifier.semantics { contentDescription = "Photo zoom" }.testTag("avatar_crop_zoom")
                        )
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { crop = AvatarCrop() }, shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("avatar_crop_reset")) {
                            Text("Reset", style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = onDismiss, shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("avatar_crop_cancel")) {
                            Text("Cancel", style = MaterialTheme.typography.labelLarge)
                        }
                        Button(
                            onClick = { onSave(crop.sanitized()) },
                            enabled = aspectRatio > 0f && !failed,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("avatar_crop_save")
                        ) { Text("Save", style = MaterialTheme.typography.labelLarge) }
                    }
                }
            }
        }
    }
}
