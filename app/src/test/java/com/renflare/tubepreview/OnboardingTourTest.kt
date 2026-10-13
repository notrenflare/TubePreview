package com.renflare.tubepreview

import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renflare.tubepreview.ui.screens.TourIllustration
import androidx.compose.ui.test.*
import com.renflare.tubepreview.data.AvatarCrop
import com.renflare.tubepreview.ui.screens.TourProfileSetup
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import com.renflare.tubepreview.data.CreatorSettings
import com.renflare.tubepreview.ui.screens.OnboardingScreen
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24, 36])
class OnboardingTourTest {
    @get:Rule val compose = createComposeRule()
    private val settings = CreatorSettings("renflare", "", "", true, 15, "", "", "", "", 5, false, "", "", "")

    @Test fun swipeAndButtonsNavigateWithoutCompletingEarly() {
        var completions = 0
        compose.setContent {
            TubePreviewTheme {
                OnboardingScreen(settings, { _, _ -> }, { completions++ }, {})
            }
        }
        compose.onNodeWithTag("onboarding_page_0").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_pager").performTouchInput { swipeLeft() }
        compose.onNodeWithTag("onboarding_page_1").assertIsDisplayed()
        compose.onNodeWithContentDescription("Example option B").assertIsDisplayed().performClick().assertIsSelected()
        compose.onNodeWithText("The long way home").assertExists()
        compose.onNodeWithContentDescription("Previous step").performClick()
        compose.onNodeWithTag("onboarding_page_0").assertIsDisplayed()
        repeat(3) { compose.onNodeWithTag("onboarding_next").performClick() }
        compose.onNodeWithTag("onboarding_name_input").assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, completions) }
        compose.onNodeWithTag("onboarding_save_button").performClick()
        compose.runOnIdle { assertEquals(1, completions) }
    }

    @Test fun exampleChoiceUpdatesTheFeedCardAndSurvivesRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            TubePreviewTheme {
                OnboardingScreen(settings, { _, _ -> }, {}, {})
            }
        }
        compose.onNodeWithTag("onboarding_example_card").assertContentDescriptionEquals(
            "Example option A. A weekend worth remembering. 75K views. 12:15. 2 days ago.")
        compose.onNodeWithTag("onboarding_next").performClick()
        compose.onNodeWithTag("onboarding_ab_example_card").assertIsDisplayed()
        compose.onNodeWithContentDescription("Example option B").performClick().assertIsSelected()
        compose.onNodeWithContentDescription("Previous step").performClick()
        compose.onNodeWithTag("onboarding_example_card").assertContentDescriptionEquals(
            "Example option B. The long way home. 75K views. 12:15. 2 days ago.")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("onboarding_example_card").assertContentDescriptionEquals(
            "Example option B. The long way home. 75K views. 12:15. 2 days ago.")
        compose.onNodeWithTag("onboarding_next").performClick()
        compose.onNodeWithContentDescription("Example option B").assertIsSelected()
        compose.onNodeWithContentDescription("Example option C").performClick().assertIsSelected()
        compose.onNodeWithContentDescription("Previous step").performClick()
        compose.onNodeWithTag("onboarding_example_card").assertContentDescriptionEquals(
            "Example option C. One perfect day away. 75K views. 12:15. 2 days ago.")
    }

    @Test fun comparisonAndRefreshKeepTheSameVideoMetadataInBothThemes() {
        val page = mutableStateOf(1)
        val option = mutableStateOf(0)
        val dark = mutableStateOf(true)
        compose.setContent {
            TubePreviewTheme(darkTheme = dark.value) {
                TourIllustration(page.value, Modifier.fillMaxWidth().height(320.dp), option.value)
            }
        }
        val titles = listOf("A weekend worth remembering", "The long way home", "One perfect day away")
        for (theme in listOf(true, false)) {
            for (slide in listOf(1, 2)) {
                for (choice in titles.indices) {
                    compose.runOnIdle {
                        dark.value = theme
                        page.value = slide
                        option.value = choice
                    }
                    compose.onNodeWithText(titles[choice], useUnmergedTree = true).assertExists()
                    compose.onAllNodesWithContentDescription(
                        "Your channel, 75K views, 2 days ago", useUnmergedTree = true
                    ).assertCountEquals(1)
                }
            }
        }
    }

    @Test fun refreshDemoCyclesChoicesWithoutQueuingRepeatedTaps() {
        compose.setContent {
            TubePreviewTheme {
                OnboardingScreen(settings, { _, _ -> }, {}, {})
            }
        }
        repeat(2) { compose.onNodeWithTag("onboarding_next").performClick() }
        compose.onNodeWithTag("onboarding_refresh_selection")
            .assertContentDescriptionEquals("Preview option A")
        compose.mainClock.autoAdvance = false
        repeat(15) { compose.onNodeWithTag("onboarding_refresh_button").performClick() }
        compose.mainClock.advanceTimeBy(1000)
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        compose.onNodeWithTag("onboarding_refresh_selection")
            .assertContentDescriptionEquals("Preview option B")
        compose.onNodeWithContentDescription("Previous step").performClick()
        compose.onNodeWithContentDescription("Example option B").assertIsSelected()
        compose.onNodeWithContentDescription("Previous step").performClick()
        compose.onNodeWithTag("onboarding_example_card").assertContentDescriptionEquals(
            "Example option B. The long way home. 75K views. 12:15. 2 days ago.")
    }

    @Test fun pullingTheDemoRefreshesWithoutChangingTourPage() {
        compose.setContent {
            TubePreviewTheme {
                OnboardingScreen(settings, { _, _ -> }, {}, {})
            }
        }
        repeat(2) { compose.onNodeWithTag("onboarding_next").performClick() }
        compose.onNodeWithTag("onboarding_refresh_card").assertIsDisplayed()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("onboarding_refresh_pull").performTouchInput {
            swipeDown(startY = height * 0.1f, endY = height * 0.9f, durationMillis = 650)
        }
        compose.mainClock.advanceTimeBy(1200)
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        compose.onNodeWithTag("onboarding_page_2").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_refresh_selection")
            .assertContentDescriptionEquals("Preview option B")
        compose.onNodeWithTag("onboarding_pager").performTouchInput { swipeLeft() }
        compose.onNodeWithTag("onboarding_page_3").assertIsDisplayed()
    }

    @Test fun skipTourAllowsDefaultSetupEvenWithABlankDraft() {
        var savedName: String? = null
        var completions = 0
        compose.setContent {
            TubePreviewTheme(darkTheme = false) {
                OnboardingScreen(settings.copy(isDarkMode = false),
                    { name, _ -> savedName = name }, { completions++ }, {})
            }
        }
        compose.onNodeWithTag("onboarding_skip").performClick()
        compose.onNodeWithTag("onboarding_name_input").performTextReplacement(" ")
        compose.onNodeWithTag("onboarding_save_button").assertIsNotEnabled()
        compose.onNodeWithTag("onboarding_defaults").performClick()
        compose.runOnIdle {
            assertEquals("renflare", savedName)
            assertEquals(1, completions)
        }
    }

    @Test fun restoringTheScreenKeepsPageAndChannelDraft() {
        val restoration = StateRestorationTester(compose)
        var savedName: String? = null
        restoration.setContent {
            TubePreviewTheme {
                OnboardingScreen(settings, { name, _ -> savedName = name }, {}, {})
            }
        }
        compose.onNodeWithTag("onboarding_skip").performClick()
        compose.onNodeWithTag("onboarding_name_input").performTextReplacement("My studio")
        compose.onNodeWithContentDescription("Previous step").performClick()
        compose.onNodeWithTag("onboarding_page_2").assertIsDisplayed()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("onboarding_page_2").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_next").performClick()
        compose.onNodeWithTag("onboarding_name_input").assertTextContains("My studio")
        compose.onNodeWithTag("onboarding_save_button").performClick()
        compose.runOnIdle { assertEquals("My studio", savedName) }
    }

    @Test fun adaptingProfileForKeyboardPreservesInputFocusAndDraft() {
        val compact = mutableStateOf(false)
        val editing = mutableStateOf(false)
        val name = mutableStateOf("renflare")
        var photoRequests = 0
        compose.setContent {
            TubePreviewTheme {
                TourProfileSetup(name.value, { name.value = it }, "", AvatarCrop(), { photoRequests++ },
                    compact = compact.value, editing = editing.value)
            }
        }
        compose.onNodeWithTag("onboarding_name_input").performClick().assertIsFocused()
        compose.onNodeWithTag("onboarding_name_input").performTextReplacement("My studio")
        compose.runOnIdle {
            compact.value = true
            editing.value = true
        }
        compose.onNodeWithTag("onboarding_name_input").assertIsFocused().assertTextContains("My studio")
        compose.onNodeWithContentDescription("Choose channel photo").assertIsDisplayed()
        compose.onNodeWithText("Add photo").assertIsDisplayed()
        compose.onNodeWithText("Optional · Up to 2 MB").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_name_input").performTextInput("!")
        compose.runOnIdle { assertEquals("My studio!", name.value) }
        compose.runOnIdle {
            compact.value = false
            editing.value = false
        }
        compose.onNodeWithTag("onboarding_name_input").assertIsFocused().assertTextContains("My studio!")
        compose.onNodeWithText("Add photo").assertHasNoClickAction().performTouchInput { click() }
        compose.onNodeWithText("Optional · Up to 2 MB").assertHasNoClickAction().performTouchInput { click() }
        compose.runOnIdle { assertEquals(0, photoRequests) }
        compose.onNodeWithContentDescription("Choose channel photo").performClick()
        compose.runOnIdle { assertEquals(1, photoRequests) }
    }
}
