package com.renflare.tubepreview

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.renflare.tubepreview.ui.components.StartupSplash
import com.renflare.tubepreview.ui.screens.HomeScreen
import com.renflare.tubepreview.ui.screens.OnboardingScreen
import com.renflare.tubepreview.ui.screens.SettingsScreen
import com.renflare.tubepreview.ui.theme.TubePreviewTheme
import com.renflare.tubepreview.ui.viewmodel.CreatorViewModel

class MainActivity : ComponentActivity() {
    private companion object {
        // Process-scoped: resuming or recreating the activity is not a cold launch.
        var hasStarted = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isColdLaunch = !hasStarted
        hasStarted = true
        if (Build.VERSION.SDK_INT >= 31) {
            // Hand off the platform starting window without a second exit animation.
            splashScreen.setOnExitAnimationListener { it.remove() }
        }
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced = false
            // Compose owns both palettes; system Force Dark must not recolor them.
            window.decorView.isForceDarkAllowed = false
        }
        setContent {
            val viewModel: CreatorViewModel = viewModel()
            val settings by viewModel.settingsState.collectAsState()
            val feedList by viewModel.homeFeedState.collectAsState()
            val isRefreshing by viewModel.isRefreshing.collectAsState()
            val imageWarning by viewModel.imageWarning.collectAsState()
            var splashComplete by remember { mutableStateOf(!isColdLaunch) }

            TubePreviewTheme(darkTheme = settings.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(Modifier.fillMaxSize()) {
                        if (settings.isLoaded) {
                            val navController = rememberNavController()
                            val startDestination = remember {
                                if (settings.isOnboarded) "home" else "onboarding"
                            }
                            NavHost(
                                navController = navController,
                                startDestination = startDestination
                            ) {
                                composable("onboarding") {
                                    OnboardingScreen(
                                        settings = settings,
                                        onSaveProfile = { name, uri ->
                                            viewModel.setChannelName(name)
                                            uri?.let { viewModel.setProfilePictureFromUri(it) }
                                        },
                                        onToggleTheme = viewModel::setDarkMode,
                                        onNavigateNext = {
                                            viewModel.setIsOnboarded(true)
                                            navController.navigate("home") {
                                                popUpTo("onboarding") { inclusive = true }
                                            }
                                        }
                                    )
                                }
                                composable("home") {
                                    HomeScreen(
                                        settings = settings,
                                        videoFeed = feedList,
                                        isRefreshing = isRefreshing,
                                        onRefresh = viewModel::refreshFeed,
                                        onNavigateToSettings = { navController.navigate("settings") },
                                        onToggleTheme = { viewModel.setDarkMode(it) }
                                    )
                                }
                                composable("settings") {
                                    SettingsScreen(
                                        settings = settings,
                                        onNavigateBack = { navController.popBackStack() },
                                        onUpdateChannelName = { viewModel.setChannelName(it) },
                                        onUpdateProfilePic = { viewModel.setProfilePictureFromUri(it) },
                                        onUpdateThumbnail = { viewModel.setPreviewThumbnailFromUri(it) },
                                        onUpdateDarkMode = { viewModel.setDarkMode(it) },
                                        onUpdateFeedSize = { viewModel.setFeedSize(it) },
                                        onUpdatePreviewTitle = { viewModel.setPreviewTitle(it) },
                                        onUpdatePreviewViews = { viewModel.setPreviewViews(it) },
                                        onUpdatePreviewDuration = { viewModel.setPreviewDuration(it) },
                                        onUpdatePreviewInterval = { viewModel.setPreviewInterval(it) },
                                        onUpdatePlaceholderDefaults = { title, views, duration ->
                                            viewModel.updatePlaceholderDefaults(title, views, duration)
                                        },
                                        onResetAll = { viewModel.resetAllSettings() }
                                    )
                                }
                            }
                        }
                        imageWarning?.let { message ->
                            AlertDialog(
                                onDismissRequest = viewModel::dismissImageWarning,
                                title = { Text("Couldn't use this image") },
                                text = { Text(message) },
                                confirmButton = {
                                    TextButton(onClick = viewModel::dismissImageWarning) {
                                        Text("Got it")
                                    }
                                }
                            )
                        }
                        if (!splashComplete) {
                            StartupSplash(
                                isReady = settings.isLoaded &&
                                    (!settings.isOnboarded || feedList.isNotEmpty() || settings.feedSize <= 0),
                                onComplete = { splashComplete = true }
                            )
                        }
                    }
                }
            }
        }
    }
}
