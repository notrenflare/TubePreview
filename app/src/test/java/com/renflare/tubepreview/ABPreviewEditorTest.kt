package com.renflare.tubepreview

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.renflare.tubepreview.data.*
import com.renflare.tubepreview.ui.components.ABPreviewEditor
import com.renflare.tubepreview.ui.components.ABPreviewToggle
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h780dp")
class ABPreviewEditorTest {
    @get:Rule val compose = createComposeRule()

    @Test fun toggleHasOneLabeledAccessibleSwitchAndRemembersItsState() {
        val enabled = mutableStateOf(false)
        compose.setContent {
            TubePreviewTheme {
                ABPreviewToggle(enabled.value) { enabled.value = it }
            }
        }
        compose.onNodeWithTag("ab_toggle").assertIsOff().performClick().assertIsOn()
        compose.onNodeWithText("A/B preview").assertExists()
        compose.onNodeWithTag("ab_toggle").performClick().assertIsOff()
    }

    @Test fun onlyTheSwitchRespondsToTouchesInBothThemes() {
        val enabled = mutableStateOf(false)
        val dark = mutableStateOf(false)
        var changes = 0
        compose.setContent {
            TubePreviewTheme(darkTheme = dark.value) {
                ABPreviewToggle(enabled.value) { enabled.value = it; changes++ }
            }
        }
        for (theme in listOf(false, true)) {
            compose.runOnIdle { dark.value = theme; enabled.value = false; changes = 0 }
            compose.onNodeWithText("A/B preview", useUnmergedTree = true).performTouchInput { click() }
            compose.onNodeWithText("Compare titles and thumbnails", useUnmergedTree = true)
                .performTouchInput { click() }
            compose.runOnIdle { assertFalse(enabled.value); assertEquals(0, changes) }
            compose.onNodeWithTag("ab_toggle").assertIsOff().performTouchInput { click() }
            compose.onNodeWithTag("ab_toggle").assertIsOn()
            compose.runOnIdle { assertEquals(1, changes) }
            compose.onNodeWithTag("ab_toggle").performTouchInput { click() }
            compose.onNodeWithTag("ab_toggle").assertIsOff()
            compose.runOnIdle { assertEquals(2, changes) }
        }
    }

    @Test fun editingTabsKeepEachTitleSeparateAndModesShareOnlyTheFixedInput() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val initial = runBlocking { SettingsRepository(context).settingsFlow.first() }
        val state = mutableStateOf(initial.copy(previewTitle = "Title A", abPreview = ABPreviewSettings(
            true, ComparisonMode.TITLES, PreviewVariant("Title B"), PreviewVariant("Title C"))))
        compose.setContent {
            TubePreviewTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    ABPreviewEditor(
                        state.value,
                        onModeChange = { state.value = state.value.copy(abPreview = state.value.abPreview.copy(mode = it)) },
                        onTitleChange = { index, title ->
                            state.value = when (index) {
                                0 -> state.value.copy(previewTitle = title)
                                1 -> state.value.copy(abPreview = state.value.abPreview.copy(optionB = state.value.abPreview.optionB.copy(title = title)))
                                else -> state.value.copy(abPreview = state.value.abPreview.copy(optionC = state.value.abPreview.optionC.copy(title = title)))
                            }
                        },
                        onThumbnailChange = { _, _ -> }, onClearOption = {},
                        onSelectOption = { state.value = state.value.copy(abPreview = state.value.abPreview.copy(activeIndex = it)) }
                    )
                }
            }
        }
        compose.onNodeWithTag("ab_title_0").performScrollTo().performTextReplacement("Edited A")
        compose.onNodeWithTag("ab_edit_1").performScrollTo().performClick()
        compose.onNodeWithTag("ab_title_1").performScrollTo().assertTextEquals("Title B").performTextReplacement("Edited B")
        compose.onNodeWithTag("ab_edit_2").performScrollTo().performClick()
        compose.onNodeWithTag("ab_title_2").performScrollTo().assertTextEquals("Title C")
        compose.onNodeWithTag("ab_preview_selected").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(2, state.value.abPreview.activeIndex) }
        compose.onNodeWithTag("ab_edit_0").performScrollTo().performClick()
        compose.onNodeWithTag("ab_title_0").performScrollTo().assertTextEquals("Edited A")
        compose.onNodeWithTag("ab_mode_THUMBNAILS").performScrollTo().performClick()
        compose.onNodeWithTag("ab_edit_1").performScrollTo().performClick()
        compose.onNodeWithTag("ab_title_0").performScrollTo().assertTextEquals("Edited A")
        compose.onNodeWithTag("ab_thumbnail_1").assertExists()
        compose.onNodeWithTag("ab_mode_BOTH").performScrollTo().performClick()
        compose.onNodeWithTag("ab_title_1").performScrollTo().assertTextEquals("Edited B")
        compose.onNodeWithTag("ab_preview_selected").performScrollTo().assertIsNotEnabled()
        compose.onAllNodesWithTag("ab_edit_3").assertCountEquals(0)
    }
}
