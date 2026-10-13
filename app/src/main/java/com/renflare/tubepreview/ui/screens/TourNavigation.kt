package com.renflare.tubepreview.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renflare.tubepreview.ui.components.PlumpIconButton
import com.renflare.tubepreview.ui.components.PlumpIcons
import com.renflare.tubepreview.ui.components.bouncyClickable
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
internal fun TourHeaderAction(
    pager: PagerState, finishing: Boolean, onSkip: () -> Unit, onDefaults: () -> Unit
) {
    val defaults = pager.settledPage == pager.pageCount - 1
    val colors = MaterialTheme.colorScheme
    Box(Modifier.testTag("onboarding_header_action")) {
        TextButton(
            onClick = { if (defaults) onDefaults() else onSkip() },
            enabled = !finishing,
            shape = CircleShape,
            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.28f)),
            colors = ButtonDefaults.textButtonColors(
                containerColor = colors.primary.copy(alpha = 0.1f),
                contentColor = colors.onSurface
            ),
            modifier = Modifier.heightIn(min = 48.dp)
                .testTag(if (defaults) "onboarding_defaults" else "onboarding_skip")
        ) {
            fun defaultsFraction() = (pager.currentPage + pager.currentPageOffsetFraction -
                (pager.pageCount - 2)).coerceIn(0f, 1f)
            Box(Modifier.clearAndSetSemantics {
                text = AnnotatedString(if (pager.currentPage == pager.pageCount - 1) "Use defaults" else "Skip tour")
            }, contentAlignment = Alignment.Center) {
                Text("Skip tour", maxLines = 1,
                    modifier = Modifier.graphicsLayer { alpha = 1f - defaultsFraction() })
                Text("Use defaults", maxLines = 1,
                    modifier = Modifier.graphicsLayer { alpha = defaultsFraction() })
            }
        }
    }
}

@Composable
internal fun TourNavigation(
    pager: PagerState, wide: Boolean, keyboardVisible: Boolean,
    finishing: Boolean, canFinish: Boolean, onStep: (Int) -> Unit, onNext: () -> Unit,
    keyboardProgress: Float = if (keyboardVisible) 1f else 0f
) {
    val page = pager.settledPage
    val lastPage = page == pager.pageCount - 1
    @Composable
    fun Back() {
        Box(
            Modifier.layout { measurable, constraints ->
                val child = measurable.measure(constraints.copy(minWidth = 0))
                val reveal = (pager.currentPage + pager.currentPageOffsetFraction).coerceIn(0f, 1f)
                layout((60.dp.roundToPx() * reveal).roundToInt(), child.height) {
                    child.placeRelative(0, 0)
                }
            }.clipToBounds(),
            contentAlignment = Alignment.CenterStart
        ) {
            if (page > 0 || pager.isScrollInProgress) {
                PlumpIconButton(PlumpIcons.Back, "Previous step", { onStep(page - 1) },
                    Modifier.graphicsLayer {
                        alpha = (pager.currentPage + pager.currentPageOffsetFraction).coerceIn(0f, 1f)
                    }.testTag("onboarding_back"))
            } else {
                Spacer(Modifier.size(48.dp))
            }
        }
    }
    @Composable
    fun Next(modifier: Modifier) {
        val enabled = !finishing && (!lastPage || canFinish)
        val colors = MaterialTheme.colorScheme
        val foreground = if (enabled) colors.onPrimary else colors.onSurface.copy(alpha = 0.38f)
        BoxWithConstraints(modifier.testTag("onboarding_primary_action")) {
            val shortLabel = maxWidth < 300.dp
            Row(
                Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(CircleShape)
                    .background(if (enabled) colors.primary else colors.onSurface.copy(alpha = 0.12f))
                    .bouncyClickable(enabled = enabled, onClick = onNext)
                    .testTag(if (lastPage) "onboarding_save_button" else "onboarding_next")
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val startLabel = if (shortLabel) "Start" else "Start previewing"
                fun startFraction() = (pager.currentPage + pager.currentPageOffsetFraction -
                    (pager.pageCount - 2)).coerceIn(0f, 1f)
                Box(Modifier.weight(1f).clearAndSetSemantics {
                    text = AnnotatedString(if (pager.currentPage == pager.pageCount - 1) startLabel else "Next")
                }) {
                    Text("Next", color = foreground, maxLines = 1,
                        fontSize = 16.sp, fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag("onboarding_next_label").graphicsLayer { alpha = 1f - startFraction() })
                    Text(startLabel, color = foreground, maxLines = 1,
                        fontSize = 16.sp, fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag("onboarding_start_label").graphicsLayer { alpha = startFraction() })
                }
                Icon(PlumpIcons.Forward, null, tint = foreground, modifier = Modifier.size(24.dp))
            }
        }
    }
    if (wide) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Back()
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                TourPagination(pager, !finishing, onStep, Modifier.tourKeyboardCollapse(keyboardProgress))
            }
            Next(Modifier.width(208.dp))
        }
    } else {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            TourPagination(pager, !finishing, onStep,
                Modifier.align(Alignment.CenterHorizontally).tourKeyboardCollapse(keyboardProgress))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Back()
                Next(Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun TourPagination(
    pager: PagerState, enabled: Boolean, onStep: (Int) -> Unit, modifier: Modifier = Modifier
) {
    val active = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Box(modifier) {
        Row {
            repeat(pager.pageCount) { index ->
                Box(Modifier.size(width = 24.dp, height = 48.dp).clip(CircleShape)
                    .clickable(enabled = enabled, role = Role.Tab, onClick = { onStep(index) })
                    .semantics {
                        selected = index == pager.settledPage
                        contentDescription = "Step ${index + 1} of ${pager.pageCount}"
                    }.testTag("onboarding_step_$index"))
            }
        }
        Canvas(Modifier.matchParentSize().testTag("onboarding_indicator").clearAndSetSemantics {}) {
            val step = 24.dp.toPx()
            val radius = 4.dp.toPx()
            repeat(pager.pageCount) { index ->
                drawCircle(inactive, radius, Offset(step * (index + 0.5f), size.height / 2f))
            }
            val position = (pager.currentPage + pager.currentPageOffsetFraction).coerceIn(0f, (pager.pageCount - 1).toFloat())
            val bounds = tourIndicatorEdges(position)
            val left = step * (bounds.first + 0.5f) - radius
            val right = step * (bounds.second + 0.5f) + radius
            drawRoundRect(active,
                topLeft = Offset(if (rtl) size.width - right else left, size.height / 2f - radius),
                size = Size(right - left, radius * 2f), cornerRadius = CornerRadius(radius))
        }
    }
}

/** The leading edge travels first, then the trailing edge catches up. Reversing a swipe retraces it. */
internal fun tourIndicatorEdges(position: Float): Pair<Float, Float> {
    val page = floor(position)
    val fraction = position - page
    return (page + (fraction * 2f - 1f).coerceAtLeast(0f)) to
        (page + (fraction * 2f).coerceAtMost(1f))
}
