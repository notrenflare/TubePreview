package com.renflare.tubepreview

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.renflare.tubepreview.ui.components.GlassTextField
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingsTextInputTest {
    @get:Rule val compose = createComposeRule()

    @Test fun delayedSavedValueDoesNotReplaceNewerTyping() {
        val saved = mutableStateOf("")
        val writes = mutableListOf<String>()
        compose.setContent {
            TubePreviewTheme {
                GlassTextField(
                    value = saved.value,
                    onValueChange = { saved.value = it; writes.add(it) },
                    label = "Title",
                    modifier = Modifier.testTag("input")
                )
            }
        }

        val input = compose.onNodeWithTag("input")
        input.performTextInput("Release")
        input.performTextInput(" test")
        // An earlier DataStore write completes after the user has typed more.
        compose.runOnIdle { saved.value = "Release" }
        input.assertTextEquals("Release test")
        input.performTextInput("!")
        compose.runOnIdle { assertEquals("Release test!", writes.last()) }
    }

    @Test fun resetValueAppliesAfterEditingEnds() {
        val saved = mutableStateOf("Original")
        compose.setContent {
            TubePreviewTheme {
                Column {
                    GlassTextField(
                        value = saved.value,
                        onValueChange = { saved.value = it },
                        label = "Title",
                        modifier = Modifier.testTag("input")
                    )
                    Box(Modifier.size(24.dp).focusable().testTag("finish"))
                }
            }
        }

        val input = compose.onNodeWithTag("input")
        input.performTextReplacement("Edited")
        compose.onNodeWithTag("finish").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        input.assertIsNotFocused()
        compose.runOnIdle { saved.value = "Default title" }
        input.assertTextEquals("Default title")
    }
}
