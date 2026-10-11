package com.renflare.tubepreview

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.test.core.app.ApplicationProvider
import com.renflare.tubepreview.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ABPreviewPersistenceTest {
    private fun withRepository(block: suspend (SettingsRepository, Context) -> Unit) = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.dataStore.edit { it.clear() }
        try { block(SettingsRepository(context), context) }
        finally { context.dataStore.edit { it.clear() } }
    }

    @Test fun savesThreePairsAndRefreshSequenceAcrossRepositoryRecreation() = withRepository { repository, context ->
        (0..2).forEach {
            repository.updateVariantTitle(it, "Title ${'A' + it}")
            repository.updateVariantThumbnail(it, "${'a' + it}.png")
        }
        repository.updateABEnabled(true)
        repeat(7) { turn ->
            val settings = SettingsRepository(context).settingsFlow.first()
            assertEquals(turn % 3, settings.activePreviewIndex())
            assertEquals("Title ${'A' + turn % 3}", settings.activePreview().title)
            repository.advanceABOption()
        }
        repository.updateABMode(ComparisonMode.TITLES)
        assertEquals("c.png", repository.settingsFlow.first().abPreview.optionC.thumbnailUri)
        repository.updateABMode(ComparisonMode.THUMBNAILS)
        assertEquals("Title B", repository.settingsFlow.first().abPreview.optionB.title)
    }

    @Test fun partialOptionsSkipAndDisabledRefreshDoesNotAdvance() = withRepository { repository, _ ->
        repository.updateABMode(ComparisonMode.TITLES)
        repository.updateVariantTitle(2, "Third title")
        repository.updateABEnabled(true)
        repository.advanceABOption()
        assertEquals(2, repository.settingsFlow.first().activePreviewIndex())
        repository.updateABEnabled(false)
        repository.advanceABOption()
        assertEquals(2, repository.settingsFlow.first().abPreview.activeIndex)
        assertEquals(SettingsRepository.DEFAULT_PREVIEW_TITLE, repository.settingsFlow.first().activePreview().title)
        repository.updateABEnabled(true)
        repository.advanceABOption()
        assertEquals(0, repository.settingsFlow.first().activePreviewIndex())
    }

    @Test fun clearAndResetRemoveAllVariantsButKeepOnboarding() = withRepository { repository, _ ->
        repository.updateOnboarded(true)
        repository.updateVariantTitle(1, "Second")
        repository.updateVariantThumbnail(1, "b.png")
        repository.updateVariantTitle(2, "Third")
        repository.updateVariantThumbnail(2, "c.png")
        repository.updateABEnabled(true)
        repository.clearVariant(1)
        assertEquals(PreviewVariant(), repository.settingsFlow.first().abPreview.optionB)
        assertEquals("c.png", repository.settingsFlow.first().abPreview.optionC.thumbnailUri)
        repository.resetToDefaults()
        assertEquals(ABPreviewSettings(), repository.settingsFlow.first().abPreview)
        assertTrue(repository.settingsFlow.first().isOnboarded)
    }

    @Test fun malformedModeAndIndexHaveSafeDefaults() = withRepository { repository, context ->
        context.dataStore.edit {
            it[stringPreferencesKey("ab_mode")] = "unknown"
            it[intPreferencesKey("ab_active")] = 100
        }
        assertEquals(ComparisonMode.BOTH, repository.settingsFlow.first().abPreview.mode)
        assertEquals(2, repository.settingsFlow.first().abPreview.activeIndex)
        assertEquals(0, repository.settingsFlow.first().activePreviewIndex())
    }

    @Test fun malformedPreferencesPreserveOtherValidSettings() = withRepository { repository, context ->
        context.dataStore.edit {
            it[stringPreferencesKey("channel_name")] = "My channel"
            it[stringPreferencesKey("dark_mode")] = "invalid"
            it[stringPreferencesKey("ab_active")] = "invalid"
            it[stringPreferencesKey("ab_title_b")] = "Second title"
            it[stringPreferencesKey("preview_interval")] = "7"
        }
        val settings = repository.settingsFlow.first()
        assertEquals("My channel", settings.channelName)
        assertTrue(settings.isDarkMode)
        assertEquals(0, settings.abPreview.activeIndex)
        assertEquals("Second title", settings.abPreview.optionB.title)
        assertEquals(7, settings.previewInterval)
    }

    @Test fun rejectsFourthOptionWithoutChangingSavedData() = withRepository { repository, _ ->
        try { repository.updateVariantTitle(3, "Fourth"); fail("Fourth option accepted") }
        catch (_: IllegalArgumentException) {}
        assertEquals(ABPreviewSettings(), repository.settingsFlow.first().abPreview)
    }
}
