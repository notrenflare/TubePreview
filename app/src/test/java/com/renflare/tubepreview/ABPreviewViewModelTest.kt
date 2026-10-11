package com.renflare.tubepreview

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import com.renflare.tubepreview.data.*
import com.renflare.tubepreview.ui.viewmodel.CreatorViewModel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ABPreviewViewModelTest {
    @Test fun eachCompletedRefreshAdvancesOnceEvenWhenTriggeredTwice() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.dataStore.edit { it.clear() }
        val repository = SettingsRepository(app)
        val vm = CreatorViewModel(app)
        val store = ViewModelStore().apply { put("creator", vm) }
        try {
            repository.updateABMode(ComparisonMode.TITLES)
            repository.updateVariantTitle(1, "B")
            repository.updateVariantTitle(2, "C")
            repository.updateABEnabled(true)
            repeat(3) { turn ->
                vm.refreshFeed()
                vm.refreshFeed()
                assertTrue(vm.isRefreshing.value)
                vm.isRefreshing.first { !it }
                assertEquals((turn + 1) % 3, repository.settingsFlow.first().activePreviewIndex())
            }
        } finally {
            store.clear()
            app.dataStore.edit { it.clear() }
            Dispatchers.resetMain()
        }
    }

    @Test fun importsAreIndependentInvalidReplacementPreservesOldAndResetDeletesAll() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.dataStore.edit { it.clear() }
        val repository = SettingsRepository(app)
        val vm = CreatorViewModel(app)
        val store = ViewModelStore().apply { put("creator", vm) }
        val source = File.createTempFile("comparison-source", ".png", app.cacheDir)
        val invalid = File.createTempFile("comparison-invalid", ".png", app.cacheDir)
        val bitmap = Bitmap.createBitmap(32, 18, Bitmap.Config.ARGB_8888)
        source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        invalid.writeText("not an image")
        try {
            (0..2).forEach { vm.setVariantThumbnail(it, Uri.fromFile(source)).join() }
            val before = repository.settingsFlow.first().previewVariants().map { it.thumbnailUri }
            assertEquals(3, before.toSet().size)
            assertTrue(before.all { File(it).exists() })
            vm.setVariantThumbnail(1, Uri.fromFile(invalid)).join()
            assertEquals(before, repository.settingsFlow.first().previewVariants().map { it.thumbnailUri })
            assertNotNull(vm.imageWarning.value)
            vm.setVariantThumbnail(1, Uri.fromFile(source)).join()
            val after = repository.settingsFlow.first().previewVariants().map { it.thumbnailUri }
            assertEquals(before[0], after[0])
            assertEquals(before[2], after[2])
            assertNotEquals(before[1], after[1])
            assertFalse(File(before[1]).exists())
            vm.clearVariant(2).join()
            assertFalse(File(after[2]).exists())
            assertTrue(File(after[0]).exists())
            vm.resetAllSettings().join()
            assertTrue(after.none { File(it).exists() })
            assertEquals(ABPreviewSettings(), repository.settingsFlow.first().abPreview)
        } finally {
            store.clear()
            source.delete()
            invalid.delete()
            app.dataStore.edit { it.clear() }
            Dispatchers.resetMain()
        }
    }
}
