package com.renflare.tubepreview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.renflare.tubepreview.ui.components.FeedIconButton
import com.renflare.tubepreview.ui.components.FeedInteractions
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FeedInteractionTest {
    @get:Rule val compose = createComposeRule()
    private var clicks = 0

    private fun showButton() {
        compose.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                FeedInteractions {
                    Box(Modifier.fillMaxSize()) {
                        FeedIconButton(Icons.Outlined.Search, "Search", { clicks++ }, Modifier.testTag("target"))
                    }
                }
            }
        }
    }

    @Test fun mouseHoverDoesNotActivateButMouseClickActivatesOnce() {
        showButton()
        val button = compose.onNodeWithTag("target")
        button.performMouseInput { enter(center) }
        compose.waitForIdle()
        assertEquals(0, clicks)
        button.performMouseInput { click(center) }
        assertEquals(1, clicks)
        button.performMouseInput { exit() }
        assertEquals(1, clicks)
    }

    @Test fun keyboardFocusDoesNotActivateButEnterActivatesOnce() {
        showButton()
        val button = compose.onNodeWithTag("target")
        button.performSemanticsAction(SemanticsActions.RequestFocus)
        button.assertIsFocused()
        assertEquals(0, clicks)
        button.performKeyInput { pressKey(Key.Enter) }
        assertEquals(1, clicks)
    }
}
