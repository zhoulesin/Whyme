package com.zhoulesin.whyme.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue,
    onPrimary = TextPrimaryDark,
    primaryContainer = SkyBlueDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = Orange,
    onSecondary = TextPrimaryDark,
    secondaryContainer = OrangeDark,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = Success,
    onTertiary = TextPrimaryDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardBackgroundDark,
    onSurfaceVariant = TextSecondaryDark,
    error = Error,
    onError = TextPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = SkyBlue,
    onPrimary = TextPrimaryDark,
    primaryContainer = SkyBlueLight,
    onPrimaryContainer = TextPrimary,
    secondary = Orange,
    onSecondary = TextPrimaryDark,
    secondaryContainer = OrangeLight,
    onSecondaryContainer = TextPrimary,
    tertiary = Success,
    onTertiary = TextPrimaryDark,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = TextPrimaryDark
)

@Composable
fun WhyMeEnglishTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
