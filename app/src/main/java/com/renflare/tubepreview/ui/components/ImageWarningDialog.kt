package com.renflare.tubepreview.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun ImageWarningDialog(message: String, onDismiss: () -> Unit) {
    AppMessageDialog(
        title = "Couldn't use this image",
        onDismiss = onDismiss,
        confirmLabel = "Got it",
        modifier = Modifier.testTag("image_warning_dialog"),
        confirmModifier = Modifier.testTag("image_warning_dismiss")
    ) {
        Text(message)
    }
}
