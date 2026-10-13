package com.renflare.tubepreview

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renflare.tubepreview.data.AvatarCrop
import com.renflare.tubepreview.ui.screens.TourPage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.TextLayoutResult
import org.junit.Assert.*
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.renflare.tubepreview.data.CreatorSettings
import com.renflare.tubepreview.ui.screens.OnboardingScreen
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w393dp-h852dp-xxhdpi")
class OnboardingLayoutTest {
    @get:Rule val compose = createComposeRule()

    private fun checkLayout(label: String, fontScale: Float = 1f) {
        val dark = mutableStateOf(true)
        val settings = CreatorSettings("renflare", "", "", true, 15, "", "", "", "", 5, false, "", "", "")
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                TubePreviewTheme(darkTheme = dark.value) {
                    OnboardingScreen(settings.copy(isDarkMode = dark.value), { _, _ -> }, {}, { dark.value = it })
                }
            }
        }
        repeat(4) { page ->
            compose.onNodeWithTag("onboarding_page_$page").assertIsDisplayed()
            val action = compose.onNodeWithTag(if (page == 3) "onboarding_save_button" else "onboarding_next")
            action.assertIsDisplayed().assertIsEnabled()
            val pageNode = compose.onNodeWithTag("onboarding_page_$page")
            val pageBounds = pageNode.getUnclippedBoundsInRoot()
            assertFalse(pageNode.fetchSemanticsNode().config.contains(SemanticsProperties.VerticalScrollAxisRange))
            for (part in listOf("heading", "description", "hint")) {
                if (part == "description" && page == 3 && label != "phone") continue
                val node = compose.onNodeWithTag("onboarding_${part}_$page").assertIsDisplayed()
                val bounds = node.getUnclippedBoundsInRoot()
                assertTrue("$label $page $part starts inside page", bounds.top >= pageBounds.top)
                assertTrue("$label $page $part ends inside page", bounds.bottom <= pageBounds.bottom)
                val layouts = mutableListOf<TextLayoutResult>()
                node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
                val result = layouts.single()
                assertFalse("$label $page $part text is clipped: size=${result.size}, width=${result.didOverflowWidth}, height=${result.didOverflowHeight}, bounds=$bounds, page=$pageBounds, lines=${result.lineCount}", result.hasVisualOverflow)
            }
            val controls = when (page) {
                1 -> listOf("Example option A", "Example option B", "Example option C")
                3 -> listOf("Choose channel photo")
                else -> emptyList()
            }
            controls.forEach { description ->
                val bounds = compose.onNodeWithContentDescription(description).assertIsDisplayed().getUnclippedBoundsInRoot()
                assertTrue("$label $page $description fits", bounds.top >= pageBounds.top && bounds.bottom <= pageBounds.bottom)
            }
            if (page == 2) {
                val bounds = compose.onNodeWithTag("onboarding_refresh_button").assertIsDisplayed().getUnclippedBoundsInRoot()
                assertTrue("$label refresh action fits", bounds.top >= pageBounds.top && bounds.bottom <= pageBounds.bottom)
                assertTrue("$label refresh action retains a usable target: $bounds", (bounds.bottom - bounds.top).value + 0.001f >= 48f)
            }
            if (page == 3) {
                val bounds = compose.onNodeWithTag("onboarding_name_input").assertIsDisplayed().getUnclippedBoundsInRoot()
                assertTrue("$label profile field fits", bounds.top >= pageBounds.top && bounds.bottom <= pageBounds.bottom)
                assertTrue("$label profile field has room for its text", (bounds.bottom - bounds.top).value >= 56f)
                if ((pageBounds.right - pageBounds.left).value < 600f) {
                    val card = compose.onNodeWithTag("onboarding_profile_card").getUnclippedBoundsInRoot()
                    val description = compose.onNodeWithTag(if (label == "phone") "onboarding_description_3" else "onboarding_heading_3").getUnclippedBoundsInRoot()
                    val hint = compose.onNodeWithTag("onboarding_hint_3").getUnclippedBoundsInRoot()
                    assertTrue("$label profile follows its description", (card.top - description.bottom).value in 7f..24f)
                    assertTrue("$label privacy note follows the form", (hint.top - card.bottom).value in 7f..24f)
                }
            }
            for (theme in listOf(true, false)) {
                compose.runOnIdle { dark.value = theme }
                compose.mainClock.advanceTimeBy(500)
                compose.waitForIdle()
                System.getenv("TUBEPREVIEW_SCREENSHOTS")?.let { destination ->
                    val file = File(destination, "TubePreview-tour-balanced-$label-${if (theme) "dark" else "light"}-$page.png")
                    compose.onRoot().captureRoboImage(file.absolutePath,
                        roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record))
                }
            }
            if (page < 3) action.performClick()
        }
    }

    @Test fun allStepsAndActionsFitBothThemes() = checkLayout("phone")

    @Test
    @Config(qualifiers = "w320dp-h568dp-mdpi")
    fun compactScreenWithLargeTextKeepsActionsAndFormReachable() = checkLayout("compact", 1.5f)

    @Test
    @Config(qualifiers = "w640dp-h360dp-hdpi")
    fun landscapeKeepsAllStepsReachable() = checkLayout("landscape")

    @Test
    @Config(qualifiers = "w360dp-h440dp-xxhdpi")
    fun keyboardLayoutKeepsPhotoControlsFieldAndContextVisible() {
        val dark = mutableStateOf(true)
        compose.setContent {
            TubePreviewTheme(darkTheme = dark.value) {
                Box(Modifier.fillMaxWidth().height(280.dp)) {
                    TourPage(3, false, true, "renflare", {}, "", AvatarCrop(), {})
                }
            }
        }
        for (theme in listOf(true, false)) {
            compose.runOnIdle { dark.value = theme }
            val page = compose.onNodeWithTag("onboarding_page_3").getUnclippedBoundsInRoot()
            val nodes = listOf(
                compose.onNodeWithContentDescription("Choose channel photo"),
                compose.onNodeWithText("Add photo"),
                compose.onNodeWithText("Optional · Up to 2 MB"),
                compose.onNodeWithTag("onboarding_name_input"),
                compose.onNodeWithTag("onboarding_hint_3")
            )
            nodes.forEach { node ->
                val bounds = node.assertIsDisplayed().getUnclippedBoundsInRoot()
                assertTrue("Keyboard setup content fits", bounds.top >= page.top && bounds.bottom <= page.bottom)
            }
            compose.onNodeWithTag("onboarding_heading_3").assertDoesNotExist()
            val input = compose.onNodeWithTag("onboarding_name_input").getUnclippedBoundsInRoot()
            assertTrue("Input remains usable", (input.bottom - input.top).value >= 56f)
        }
    }
}
