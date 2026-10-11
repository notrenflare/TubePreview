package com.renflare.tubepreview.data

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/** Replace the previous untouched placeholder once, preserving other saved titles and settings. */
internal object PreviewTitleMigration : DataMigration<Preferences> {
    private val completed = booleanPreferencesKey("asmr_default_title_migrated")
    private val title = stringPreferencesKey("preview_title")

    override suspend fun shouldMigrate(currentData: Preferences): Boolean =
        runCatching { currentData[completed] }.getOrNull() != true

    override suspend fun migrate(currentData: Preferences): Preferences =
        currentData.toMutablePreferences().apply {
            if (runCatching { this[title] }.getOrNull() == "I Tried Something Different") {
                this[title] = SettingsRepository.DEFAULT_PREVIEW_TITLE
            }
            this[completed] = true
        }

    override suspend fun cleanUp() = Unit
}
