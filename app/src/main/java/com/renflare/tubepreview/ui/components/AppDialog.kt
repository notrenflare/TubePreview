package com.renflare.tubepreview.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AppDialogSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(28.dp),
        border = glassBorderStroke(),
        content = content
    )
}

@Composable
fun AppDialogTitle(title: String, modifier: Modifier = Modifier) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
        modifier = modifier.semantics { heading() })
}

/** Compact messages with a scrollable body, keeping their actions within the window. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppMessageDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmLabel: String,
    modifier: Modifier = Modifier,
    confirmModifier: Modifier = Modifier,
    onConfirm: () -> Unit = onDismiss,
    cancelLabel: String? = null,
    destructive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val maxHeight = (LocalConfiguration.current.screenHeightDp.dp - 96.dp).coerceAtLeast(240.dp)
    BasicAlertDialog(onDismissRequest = onDismiss) {
        AppDialogSurface(
            modifier.widthIn(max = 360.dp).fillMaxWidth().heightIn(max = maxHeight)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppDialogTitle(title)
                Column(
                    Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                        ProvideTextStyle(MaterialTheme.typography.bodyMedium) { content() }
                    }
                }
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, androidx.compose.ui.Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (cancelLabel != null) {
                        TextButton(onClick = onDismiss, shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.heightIn(min = 48.dp)) {
                            Text(cancelLabel, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(20.dp),
                        colors = if (destructive) ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) else ButtonDefaults.buttonColors(),
                        modifier = confirmModifier.heightIn(min = 48.dp)
                    ) {
                        Text(confirmLabel, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
