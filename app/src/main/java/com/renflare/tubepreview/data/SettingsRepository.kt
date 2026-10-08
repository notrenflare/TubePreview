package com.renflare.tubepreview.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "thumbnail_preview_settings")

data class CreatorSettings(
    val channelName: String,
    val profilePictureUri: String,
    val previewThumbnailUri: String,
    val isDarkMode: Boolean,
    val feedSize: Int,
    val previewTitle: String,
    val previewViews: String,
    val previewDuration: String,
    val previewUploadTime: String,
    val previewInterval: Int,
    val isOnboarded: Boolean,
    val placeholderTitleDefault: String,
    val placeholderViewsDefault: String,
    val placeholderDurationDefault: String,
    val isLoaded: Boolean = false
)

class SettingsRepository(private val context: Context) {

    companion object {
        private val CHANNEL_NAME_KEY = stringPreferencesKey("channel_name")
        private val PROFILE_PICTURE_URI_KEY = stringPreferencesKey("profile_picture_uri")
        private val PREVIEW_THUMBNAIL_URI_KEY = stringPreferencesKey("preview_thumbnail_uri")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val FEED_SIZE_KEY = intPreferencesKey("feed_size")
        private val PREVIEW_TITLE_KEY = stringPreferencesKey("preview_title")
        private val PREVIEW_VIEWS_KEY = stringPreferencesKey("preview_views")
        private val PREVIEW_DURATION_KEY = stringPreferencesKey("preview_duration")
        private val PREVIEW_UPLOAD_TIME_KEY = stringPreferencesKey("preview_upload_time")
        private val PREVIEW_INTERVAL_KEY = intPreferencesKey("preview_interval")
        private val IS_ONBOARDED_KEY = booleanPreferencesKey("is_onboarded")

        private val PLACEHOLDER_TITLE_DEFAULT_KEY = stringPreferencesKey("placeholder_title_default")
        private val PLACEHOLDER_VIEWS_KEY = stringPreferencesKey("placeholder_views_default")
        private val PLACEHOLDER_DURATION_KEY = stringPreferencesKey("placeholder_duration_default")
    }

    val settingsFlow: Flow<CreatorSettings> = context.dataStore.data.map { preferences ->
        try {
            val intervalVal = try {
                preferences[PREVIEW_INTERVAL_KEY] ?: 5
            } catch (e: Exception) {
                try {
                    val stringKey = stringPreferencesKey("preview_interval")
                    preferences[stringKey]?.toIntOrNull() ?: 5
                } catch (e2: Exception) {
                    5
                }
            }
            CreatorSettings(
                channelName = try { preferences[CHANNEL_NAME_KEY] ?: "renflare" } catch (e: Exception) { "renflare" },
                profilePictureUri = try { preferences[PROFILE_PICTURE_URI_KEY] ?: "" } catch (e: Exception) { "" },
                previewThumbnailUri = try { preferences[PREVIEW_THUMBNAIL_URI_KEY] ?: "" } catch (e: Exception) { "" },
                isDarkMode = try { preferences[DARK_MODE_KEY] ?: true } catch (e: Exception) { true },
                feedSize = try { preferences[FEED_SIZE_KEY] ?: 15 } catch (e: Exception) { 15 },
                previewTitle = try { preferences[PREVIEW_TITLE_KEY] ?: "I Tried Something Different" } catch (e: Exception) { "I Tried Something Different" },
                previewViews = try { preferences[PREVIEW_VIEWS_KEY] ?: "75K views" } catch (e: Exception) { "75K views" },
                previewDuration = try { preferences[PREVIEW_DURATION_KEY] ?: "12:15" } catch (e: Exception) { "12:15" },
                previewUploadTime = try { preferences[PREVIEW_UPLOAD_TIME_KEY] ?: "2 hours ago" } catch (e: Exception) { "2 hours ago" },
                previewInterval = intervalVal, // preview video interval safely parsed
                isOnboarded = try { preferences[IS_ONBOARDED_KEY] ?: false } catch (e: Exception) { false },
                placeholderTitleDefault = try { preferences[PLACEHOLDER_TITLE_DEFAULT_KEY] ?: "" } catch (e: Exception) { "" },
                placeholderViewsDefault = try { preferences[PLACEHOLDER_VIEWS_KEY] ?: "" } catch (e: Exception) { "" },
                placeholderDurationDefault = try { preferences[PLACEHOLDER_DURATION_KEY] ?: "" } catch (e: Exception) { "" },
                isLoaded = true
            )
        } catch (globalEx: Exception) {
            android.util.Log.e("SettingsRepository", "Error deserializing preferences, loading defaults", globalEx)
            CreatorSettings(
                channelName = "renflare",
                profilePictureUri = "",
                previewThumbnailUri = "",
                isDarkMode = true,
                feedSize = 15,
                previewTitle = "I Tried Something Different",
                previewViews = "75K views",
                previewDuration = "12:15",
                previewUploadTime = "2 hours ago",
                previewInterval = 5,
                isOnboarded = false,
                placeholderTitleDefault = "",
                placeholderViewsDefault = "",
                placeholderDurationDefault = "",
                isLoaded = true
            )
        }
    }

    suspend fun updateChannelName(name: String) {
        context.dataStore.edit { prefs -> prefs[CHANNEL_NAME_KEY] = name }
    }

    suspend fun updateProfilePictureUri(uri: String) {
        context.dataStore.edit { prefs -> prefs[PROFILE_PICTURE_URI_KEY] = uri }
    }

    suspend fun updatePreviewThumbnailUri(uri: String) {
        context.dataStore.edit { prefs -> prefs[PREVIEW_THUMBNAIL_URI_KEY] = uri }
    }

    suspend fun updateDarkMode(isDark: Boolean) {
        context.dataStore.edit { prefs -> prefs[DARK_MODE_KEY] = isDark }
    }

    suspend fun updateFeedSize(size: Int) {
        context.dataStore.edit { prefs -> prefs[FEED_SIZE_KEY] = size }
    }

    suspend fun updatePreviewTitle(title: String) {
        context.dataStore.edit { prefs -> prefs[PREVIEW_TITLE_KEY] = title }
    }

    suspend fun updatePreviewViews(views: String) {
        context.dataStore.edit { prefs -> prefs[PREVIEW_VIEWS_KEY] = views }
    }

    suspend fun updatePreviewDuration(duration: String) {
        context.dataStore.edit { prefs -> prefs[PREVIEW_DURATION_KEY] = duration }
    }

    suspend fun updatePreviewUploadTime(time: String) {
        context.dataStore.edit { prefs -> prefs[PREVIEW_UPLOAD_TIME_KEY] = time }
    }

    suspend fun updatePreviewInterval(interval: Int) {
        context.dataStore.edit { prefs -> prefs[PREVIEW_INTERVAL_KEY] = interval }
    }

    suspend fun updateOnboarded(isOnboarded: Boolean) {
        context.dataStore.edit { prefs -> prefs[IS_ONBOARDED_KEY] = isOnboarded }
    }

    suspend fun updatePlaceholderDefaults(title: String, views: String, duration: String) {
        context.dataStore.edit { prefs ->
            prefs[PLACEHOLDER_TITLE_DEFAULT_KEY] = title
            prefs[PLACEHOLDER_VIEWS_KEY] = views
            prefs[PLACEHOLDER_DURATION_KEY] = duration
        }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { prefs ->
            prefs.remove(CHANNEL_NAME_KEY)
            prefs.remove(PROFILE_PICTURE_URI_KEY)
            prefs.remove(PREVIEW_THUMBNAIL_URI_KEY)
            prefs.remove(DARK_MODE_KEY)
            prefs.remove(FEED_SIZE_KEY)
            prefs.remove(PREVIEW_TITLE_KEY)
            prefs.remove(PREVIEW_VIEWS_KEY)
            prefs.remove(PREVIEW_DURATION_KEY)
            prefs.remove(PREVIEW_UPLOAD_TIME_KEY)
            prefs.remove(PREVIEW_INTERVAL_KEY)
            // Note: keep onboarding state intact to avoid forcing them back to onboarding screen unless desired
            prefs.remove(PLACEHOLDER_TITLE_DEFAULT_KEY)
            prefs.remove(PLACEHOLDER_VIEWS_KEY)
            prefs.remove(PLACEHOLDER_DURATION_KEY)
        }
    }
}
