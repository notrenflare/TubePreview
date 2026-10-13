package com.renflare.tubepreview

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import com.renflare.tubepreview.data.SettingsRepository
import com.renflare.tubepreview.data.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OnboardingPersistenceTest {
    @Test fun completionSurvivesRelaunchAndPreviewReset() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.dataStore.edit { it.clear() }
        try {
            val repository = SettingsRepository(context)
            assertFalse(repository.settingsFlow.first().isOnboarded)
            repository.updateChannelName("My studio")
            repository.updateOnboarded(true)
            val reloaded = SettingsRepository(context).settingsFlow.first()
            assertTrue(reloaded.isOnboarded)
            assertEquals("My studio", reloaded.channelName)
            repository.resetToDefaults()
            assertTrue(SettingsRepository(context).settingsFlow.first().isOnboarded)
        } finally {
            context.dataStore.edit { it.clear() }
        }
    }
}
