package com.renflare.tubepreview.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "thumbnail_preview_settings",
    produceMigrations = { listOf(PreviewTitleMigration) }
)

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
    val isLoaded: Boolean = false,
    val avatarCrop: AvatarCrop = AvatarCrop(),
    val abPreview: ABPreviewSettings = ABPreviewSettings()
)

class SettingsRepository(private val context: Context) {

    companion object {
        const val DEFAULT_PREVIEW_TITLE = "My first ASMR video :)"
        private val AB_ENABLED = booleanPreferencesKey("ab_enabled")
        private val AB_MODE = stringPreferencesKey("ab_mode")
        private val AB_ACTIVE = intPreferencesKey("ab_active")
        private val CHANNEL_NAME_KEY = stringPreferencesKey("channel_name")
        private val PROFILE_PICTURE_URI_KEY = stringPreferencesKey("profile_picture_uri")
        private val AVATAR_CROP_X_KEY = floatPreferencesKey("avatar_crop_x")
        private val AVATAR_CROP_Y_KEY = floatPreferencesKey("avatar_crop_y")
        private val AVATAR_CROP_ZOOM_KEY = floatPreferencesKey("avatar_crop_zoom")
        private val PREVIEW_THUMBNAIL_URI_KEY = stringPreferencesKey("preview_thumbnail_uri")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val FEED_SIZE_KEY = intPreferencesKey("feed_size")
        private val PREVIEW_TITLE_KEY = stringPreferencesKey("preview_title")
        private val VARIANT_TITLES = listOf(PREVIEW_TITLE_KEY, stringPreferencesKey("ab_title_b"), stringPreferencesKey("ab_title_c"))
        private val VARIANT_IMAGES = listOf(PREVIEW_THUMBNAIL_URI_KEY, stringPreferencesKey("ab_thumbnail_b"), stringPreferencesKey("ab_thumbnail_c"))
        private val PREVIEW_VIEWS_KEY = stringPreferencesKey("preview_views")
        private val PREVIEW_DURATION_KEY = stringPreferencesKey("preview_duration")
        private val PREVIEW_UPLOAD_TIME_KEY = stringPreferencesKey("preview_upload_time")
        private val PREVIEW_INTERVAL_KEY = intPreferencesKey("preview_interval")
        private val IS_ONBOARDED_KEY = booleanPreferencesKey("is_onboarded")

        private val PLACEHOLDER_TITLE_DEFAULT_KEY = stringPreferencesKey("placeholder_title_default")
        private val PLACEHOLDER_VIEWS_KEY = stringPreferencesKey("placeholder_views_default")
        private val PLACEHOLDER_DURATION_KEY = stringPreferencesKey("placeholder_duration_default")
    }

    private inline fun <reified T> Preferences.value(key: Preferences.Key<T>, fallback: T): T =
        (asMap()[key] as? T) ?: fallback

    val settingsFlow: Flow<CreatorSettings> = context.dataStore.data.map { preferences ->
        CreatorSettings(
            channelName = preferences.value(CHANNEL_NAME_KEY, "renflare"),
            profilePictureUri = preferences.value(PROFILE_PICTURE_URI_KEY, ""),
            previewThumbnailUri = preferences.value(PREVIEW_THUMBNAIL_URI_KEY, ""),
            isDarkMode = preferences.value(DARK_MODE_KEY, true),
            feedSize = preferences.value(FEED_SIZE_KEY, 15),
            previewTitle = preferences.value(PREVIEW_TITLE_KEY, DEFAULT_PREVIEW_TITLE),
            previewViews = preferences.value(PREVIEW_VIEWS_KEY, "75K views"),
            previewDuration = preferences.value(PREVIEW_DURATION_KEY, "12:15"),
            previewUploadTime = preferences.value(PREVIEW_UPLOAD_TIME_KEY, "2 hours ago"),
            // Older installations stored this preference as text.
            previewInterval = preferences.value(PREVIEW_INTERVAL_KEY,
                preferences.value(stringPreferencesKey("preview_interval"), "").toIntOrNull() ?: 5),
            isOnboarded = preferences.value(IS_ONBOARDED_KEY, false),
            placeholderTitleDefault = preferences.value(PLACEHOLDER_TITLE_DEFAULT_KEY, ""),
            placeholderViewsDefault = preferences.value(PLACEHOLDER_VIEWS_KEY, ""),
            placeholderDurationDefault = preferences.value(PLACEHOLDER_DURATION_KEY, ""),
            isLoaded = true,
            avatarCrop = AvatarCrop(
                preferences.value(AVATAR_CROP_X_KEY, 0f),
                preferences.value(AVATAR_CROP_Y_KEY, 0f),
                preferences.value(AVATAR_CROP_ZOOM_KEY, 1f)
            ).sanitized(),
            abPreview = preferences.readABPreview()
        )
    }

    private fun Preferences.readABPreview() = ABPreviewSettings(
        enabled = value(AB_ENABLED, false),
        mode = ComparisonMode.entries.firstOrNull { it.name == value(AB_MODE, "") } ?: ComparisonMode.BOTH,
        optionB = PreviewVariant(value(VARIANT_TITLES[1], ""), value(VARIANT_IMAGES[1], "")),
        optionC = PreviewVariant(value(VARIANT_TITLES[2], ""), value(VARIANT_IMAGES[2], "")),
        activeIndex = value(AB_ACTIVE, 0).coerceIn(0, 2)
    )

    suspend fun updateABEnabled(enabled: Boolean) {
        context.dataStore.edit { it[AB_ENABLED] = enabled }
    }

    suspend fun updateABMode(mode: ComparisonMode) {
        context.dataStore.edit {
            it[AB_MODE] = mode.name
            it[AB_ACTIVE] = 0
        }
    }

    suspend fun updateVariantTitle(index: Int, title: String) {
        require(index in 0..2)
        context.dataStore.edit { it[VARIANT_TITLES[index]] = title }
    }

    suspend fun updateVariantThumbnail(index: Int, uri: String) {
        require(index in 0..2)
        context.dataStore.edit { it[VARIANT_IMAGES[index]] = uri }
    }

    suspend fun clearVariant(index: Int) {
        require(index in 0..2)
        context.dataStore.edit {
            it[VARIANT_TITLES[index]] = ""
            it[VARIANT_IMAGES[index]] = ""
        }
    }

    suspend fun selectABOption(index: Int) {
        require(index in 0..2)
        context.dataStore.edit { it[AB_ACTIVE] = index }
    }

    suspend fun advanceABOption() {
        // Read and advance in one transaction, including edits made during refresh.
        context.dataStore.edit { prefs ->
            val comparison = prefs.readABPreview()
            if (comparison.enabled) {
                val base = PreviewVariant(
                    prefs[PREVIEW_TITLE_KEY] ?: DEFAULT_PREVIEW_TITLE,
                    prefs[PREVIEW_THUMBNAIL_URI_KEY] ?: ""
                )
                prefs[AB_ACTIVE] = comparison.nextIndex(base)
            }
        }
    }

    suspend fun updateChannelName(name: String) {
        context.dataStore.edit { prefs -> prefs[CHANNEL_NAME_KEY] = name }
    }

    suspend fun updateProfilePictureUri(uri: String) {
        context.dataStore.edit { prefs ->
            prefs[PROFILE_PICTURE_URI_KEY] = uri
            prefs.remove(AVATAR_CROP_X_KEY)
            prefs.remove(AVATAR_CROP_Y_KEY)
            prefs.remove(AVATAR_CROP_ZOOM_KEY)
        }
    }

    suspend fun updateAvatarCrop(profileUri: String, crop: AvatarCrop) {
        context.dataStore.edit { prefs ->
            // A late Save must never apply an old photo's crop to a replacement.
            if (profileUri.isNotBlank() && prefs[PROFILE_PICTURE_URI_KEY] == profileUri) {
                val framing = crop.sanitized()
                prefs[AVATAR_CROP_X_KEY] = framing.horizontalBias
                prefs[AVATAR_CROP_Y_KEY] = framing.verticalBias
                prefs[AVATAR_CROP_ZOOM_KEY] = framing.zoom
            }
        }
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
            prefs.remove(AB_ENABLED)
            prefs.remove(AB_MODE)
            prefs.remove(AB_ACTIVE)
            VARIANT_TITLES.drop(1).forEach { prefs.remove(it) }
            VARIANT_IMAGES.drop(1).forEach { prefs.remove(it) }
            prefs.remove(CHANNEL_NAME_KEY)
            prefs.remove(PROFILE_PICTURE_URI_KEY)
            prefs.remove(AVATAR_CROP_X_KEY)
            prefs.remove(AVATAR_CROP_Y_KEY)
            prefs.remove(AVATAR_CROP_ZOOM_KEY)
            prefs.remove(PREVIEW_THUMBNAIL_URI_KEY)
            prefs.remove(DARK_MODE_KEY)
            prefs.remove(FEED_SIZE_KEY)
            prefs.remove(PREVIEW_TITLE_KEY)
            prefs.remove(PREVIEW_VIEWS_KEY)
            prefs.remove(PREVIEW_DURATION_KEY)
            prefs.remove(PREVIEW_UPLOAD_TIME_KEY)
            prefs.remove(PREVIEW_INTERVAL_KEY)
            // Reset previews without repeating first-run setup.
            prefs.remove(PLACEHOLDER_TITLE_DEFAULT_KEY)
            prefs.remove(PLACEHOLDER_VIEWS_KEY)
            prefs.remove(PLACEHOLDER_DURATION_KEY)
        }
    }
}
