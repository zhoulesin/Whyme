package com.zhoulesin.whyme.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrandIndigo,
    onPrimary = PrimaryText,
    primaryContainer = BrandIndigo,
    onPrimaryContainer = PrimaryText,
    secondary = AccentViolet,
    onSecondary = PrimaryText,
    secondaryContainer = AccentViolet,
    onSecondaryContainer = PrimaryText,
    tertiary = SuccessGreen,
    onTertiary = PrimaryText,
    background = MarketingBlack,
    onBackground = PrimaryText,
    surface = PanelDark,
    onSurface = PrimaryText,
    surfaceVariant = Level3Surface,
    onSurfaceVariant = SecondaryText,
    error = Error,
    onError = PrimaryText
)

private val LightColorScheme = lightColorScheme(
    primary = BrandIndigo,
    onPrimary = PrimaryText,
    primaryContainer = BrandIndigo,
    onPrimaryContainer = PureWhite,
    secondary = AccentViolet,
    onSecondary = PrimaryText,
    secondaryContainer = AccentViolet,
    onSecondaryContainer = PureWhite,
    tertiary = SuccessGreen,
    onTertiary = PrimaryText,
    background = LightBackground,
    onBackground = PrimaryText,
    surface = LightSurface,
    onSurface = PrimaryText,
    surfaceVariant = PureWhite,
    onSurfaceVariant = SecondaryText,
    error = Error,
    onError = PrimaryText
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
