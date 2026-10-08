package com.renflare.tubepreview.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.renflare.tubepreview.data.CreatorSettings
import com.renflare.tubepreview.data.FeedVideo
import com.renflare.tubepreview.data.FeedComposer
import com.renflare.tubepreview.data.FeedLayout
import com.renflare.tubepreview.data.SettingsRepository
import com.renflare.tubepreview.data.ImageStorage
import com.renflare.tubepreview.data.ImageTooLargeException
import com.renflare.tubepreview.data.InvalidImageException
import com.renflare.tubepreview.data.ImageDimensionsTooLargeException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CreatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)
    private val imageStorage = ImageStorage(application)
    private val imageMutex = Mutex()
    private val _imageWarning = MutableStateFlow<String?>(null)
    val imageWarning = _imageWarning.asStateFlow()

    fun dismissImageWarning() {
        _imageWarning.value = null
    }

    // State flow of user settings loaded directly from standard DataStore Preferences
    val settingsState: StateFlow<CreatorSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CreatorSettings(
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
            placeholderDurationDefault = ""
        )
    )

    private val feedLayout = MutableStateFlow(FeedLayout())
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val homeFeedState: StateFlow<List<FeedVideo>> = combine(settingsState, feedLayout) { settings, layout ->
        FeedComposer.compose(settings, layout)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun refreshFeed() {
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        viewModelScope.launch {
            try {
                // Let the standard refresh indicator acknowledge the gesture.
                delay(450)
                feedLayout.value = FeedComposer.reshuffle(feedLayout.value, settingsState.value)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // Keep the previous image until a replacement has been validated and saved.
    private suspend fun importImage(uri: Uri, isThumbnail: Boolean) = imageMutex.withLock {
        var imported: java.io.File? = null
        var saved = false
        try {
            val previous = repository.settingsFlow.first()
            val image = withContext(Dispatchers.IO) {
                imageStorage.importImage(uri).also { imported = it }
            }
            if (isThumbnail) {
                repository.updatePreviewThumbnailUri(image.absolutePath)
            } else {
                repository.updateProfilePictureUri(image.absolutePath)
            }
            saved = true
            withContext(Dispatchers.IO) {
                imageStorage.deleteImportedImage(
                    if (isThumbnail) previous.previewThumbnailUri else previous.profilePictureUri
                )
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _imageWarning.value = when (error) {
                is ImageTooLargeException ->
                    "Choose an image that is 2 MB or smaller. Try compressing it or exporting a smaller copy, then choose it again."
                is ImageDimensionsTooLargeException ->
                    "This image\'s resolution is too large. Export a smaller copy, then choose it again."
                is InvalidImageException ->
                    "This file could not be read as an image. Try a JPG, PNG, or WebP image."
                else -> "We couldn't save this image. Please try choosing it again."
            }
            Log.e("CreatorViewModel", "Image import failed", error)
        } finally {
            if (!saved) withContext(NonCancellable + Dispatchers.IO) {
                imported?.delete()
            }
        }
    }

    fun setChannelName(name: String) = viewModelScope.launch {
        repository.updateChannelName(name)
    }

    fun setProfilePictureFromUri(uri: Uri) = viewModelScope.launch {
        importImage(uri, isThumbnail = false)
    }

    fun setPreviewThumbnailFromUri(uri: Uri) = viewModelScope.launch {
        importImage(uri, isThumbnail = true)
    }

    fun setDarkMode(isDark: Boolean) = viewModelScope.launch {
        repository.updateDarkMode(isDark)
    }

    fun setFeedSize(size: Int) = viewModelScope.launch {
        repository.updateFeedSize(size)
    }

    fun setPreviewTitle(title: String) = viewModelScope.launch {
        repository.updatePreviewTitle(title)
    }

    fun setPreviewViews(views: String) = viewModelScope.launch {
        repository.updatePreviewViews(views)
    }

    fun setPreviewDuration(duration: String) = viewModelScope.launch {
        repository.updatePreviewDuration(duration)
    }

    fun setPreviewInterval(interval: Int) = viewModelScope.launch {
        repository.updatePreviewInterval(interval)
    }

    fun setIsOnboarded(onboarded: Boolean) = viewModelScope.launch {
        repository.updateOnboarded(onboarded)
    }

    fun updatePlaceholderDefaults(title: String, views: String, duration: String) = viewModelScope.launch {
        repository.updatePlaceholderDefaults(title, views, duration)
    }

    fun resetAllSettings() = viewModelScope.launch {
        imageMutex.withLock {
            val previous = repository.settingsFlow.first()
            repository.resetToDefaults()
            withContext(Dispatchers.IO) {
                imageStorage.deleteImportedImage(previous.profilePictureUri)
                imageStorage.deleteImportedImage(previous.previewThumbnailUri)
            }
        }
    }

}
