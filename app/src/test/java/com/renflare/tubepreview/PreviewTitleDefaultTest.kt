package com.renflare.tubepreview

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.renflare.tubepreview.data.PreviewTitleMigration
import androidx.test.core.app.ApplicationProvider
import com.renflare.tubepreview.data.SettingsRepository
import com.renflare.tubepreview.data.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PreviewTitleDefaultTest {
    @Test fun migrationReplacesOnlyTheOldDefaultAndRunsOnce() = runBlocking {
        val title = stringPreferencesKey("preview_title")
        val channel = stringPreferencesKey("channel_name")
        val migrated = PreviewTitleMigration.migrate(preferencesOf(
            title to "I Tried Something Different", channel to "My channel"
        ))
        assertEquals("My first ASMR video :)", migrated[title])
        assertEquals("My channel", migrated[channel])
        assertFalse(PreviewTitleMigration.shouldMigrate(migrated))
        val custom = PreviewTitleMigration.migrate(preferencesOf(title to "My own title"))
        assertEquals("My own title", custom[title])
    }

    @Test fun asmrTitleIsTheFreshAndResetDefaultWhileSavedTitlesArePreserved() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.dataStore.edit { it.clear() }
        try {
            val repository = SettingsRepository(context)
            assertEquals("My first ASMR video :)", repository.settingsFlow.first().previewTitle)
            repository.updatePreviewTitle("My own title")
            assertEquals("My own title", repository.settingsFlow.first().previewTitle)
            repository.resetToDefaults()
            assertEquals("My first ASMR video :)", repository.settingsFlow.first().previewTitle)
        } finally {
            context.dataStore.edit { it.clear() }
        }
    }
}
