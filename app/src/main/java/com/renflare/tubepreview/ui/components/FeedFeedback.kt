package com.renflare.tubepreview.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Transient feed hints have one owner: newer hints replace older ones, never queue. */
class FeedFeedbackState(private val scope: CoroutineScope) {
    val hostState = SnackbarHostState()
    private var job: Job? = null
    private var message: String? = null
    private var request: Any? = null

    fun show(text: String) {
        if (text == message && job?.isActive == true) return
        job?.cancel()
        val owner = Any()
        request = owner
        message = text
        job = scope.launch {
            try {
                hostState.showSnackbar(text, withDismissAction = true, duration = SnackbarDuration.Short)
            } finally {
                if (request === owner) message = null
            }
        }
    }

    fun dismiss() {
        request = null
        message = null
        job?.cancel()
        job = null
    }
}

@Composable
fun rememberFeedFeedbackState(): FeedFeedbackState {
    val scope = rememberCoroutineScope()
    return remember(scope) { FeedFeedbackState(scope) }
}

@Composable
fun FeedFeedbackHost(state: FeedFeedbackState, backdrop: FeedBackdrop, darkTheme: Boolean) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(20.dp)
    SnackbarHost(state.hostState) { data ->
        Surface(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(shape)
                .feedBackdrop(backdrop, colors.surface.copy(alpha = if (darkTheme) 0.82f else 0.9f))
                .glassBorder(shape)
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                    dismiss { data.dismiss(); true }
                }
                .testTag("home_feedback"),
            shape = shape,
            color = Color.Transparent,
            contentColor = colors.onSurface
        ) {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(data.visuals.message, modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium, maxLines = 3,
                    overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { data.dismiss() }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Dismiss", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
