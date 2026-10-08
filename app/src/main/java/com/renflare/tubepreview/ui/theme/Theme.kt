package com.renflare.tubepreview.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import android.app.Activity
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BrandRed,
    secondary = MinimalDarkOnPrimaryContainer,
    tertiary = MinimalDarkSecondaryText,
    background = RealBlack,
    surface = RealBlack,
    surfaceVariant = CardDarkGray,
    primaryContainer = MinimalDarkPrimaryContainer,
    onPrimaryContainer = MinimalDarkOnPrimaryContainer,
    onPrimary = PureWhite,
    onSecondary = MinimalDarkText,
    onBackground = MinimalDarkText,
    onSurface = MinimalDarkText,
    onSurfaceVariant = MinimalDarkSecondaryText,
    outline = MinimalDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = BrandRed,
    secondary = MinimalLightOnPrimaryContainer,
    tertiary = MinimalLightSecondaryText,
    background = MinimalLightBg,
    surface = MinimalLightBg,
    surfaceVariant = CardLightGray,
    primaryContainer = MinimalLightPrimaryContainer,
    onPrimaryContainer = MinimalLightOnPrimaryContainer,
    onPrimary = PureWhite,
    onSecondary = MinimalLightText,
    onBackground = MinimalLightText,
    onSurface = MinimalLightText,
    onSurfaceVariant = MinimalLightSecondaryText,
    outline = MinimalLightOutline
)

@Composable
fun TubePreviewTheme(
    darkTheme: Boolean = true,
    // Allow users to force theme styles explicitly
    dynamicColor: Boolean = false, // Preserve the TubePreview brand palette by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
