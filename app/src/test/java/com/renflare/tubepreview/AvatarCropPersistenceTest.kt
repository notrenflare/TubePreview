package com.renflare.tubepreview

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import com.renflare.tubepreview.data.AvatarCrop
import com.renflare.tubepreview.data.SettingsRepository
import com.renflare.tubepreview.data.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AvatarCropPersistenceTest {
    @Test fun cropPersistsAndReplacingOrResettingPhotoClearsIt() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.dataStore.edit { it.clear() }
        try {
            val repository = SettingsRepository(context)
            val crop = AvatarCrop(0.6f, -0.4f, 2.5f)
            repository.updateProfilePictureUri("/files/first.jpg")
            repository.updateAvatarCrop("/files/first.jpg", crop)
            assertEquals(crop, SettingsRepository(context).settingsFlow.first().avatarCrop)
            repository.updateProfilePictureUri("/files/second.jpg")
            assertEquals(AvatarCrop(), repository.settingsFlow.first().avatarCrop)
            repository.updateAvatarCrop("/files/first.jpg", crop)
            assertEquals(AvatarCrop(), repository.settingsFlow.first().avatarCrop)
            repository.updateAvatarCrop("/files/second.jpg", crop)
            repository.resetToDefaults()
            val reset = repository.settingsFlow.first()
            assertEquals("", reset.profilePictureUri)
            assertEquals(AvatarCrop(), reset.avatarCrop)
        } finally {
            context.dataStore.edit { it.clear() }
        }
    }
}
