package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GoogleBluePrimaryDark,
    onPrimary = GoogleBlueOnPrimaryDark,
    primaryContainer = GoogleBlueContainerDark,
    onPrimaryContainer = GoogleBlueOnContainerDark,
    secondary = SparkPurpleSecondaryDark,
    onSecondary = SparkPurpleOnSecondaryDark,
    secondaryContainer = SparkPurpleContainerDark,
    onSecondaryContainer = SparkPurpleOnContainerDark,
    tertiary = AccentBlueTertiaryDark,
    onTertiary = AccentBlueOnTertiaryDark,
    tertiaryContainer = AccentBlueContainerDark,
    onTertiaryContainer = AccentBlueOnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = GoogleBluePrimaryLight,
    onPrimary = GoogleBlueOnPrimaryLight,
    primaryContainer = GoogleBlueContainerLight,
    onPrimaryContainer = GoogleBlueOnContainerLight,
    secondary = SparkPurpleSecondaryLight,
    onSecondary = SparkPurpleOnSecondaryLight,
    secondaryContainer = SparkPurpleContainerLight,
    onSecondaryContainer = SparkPurpleOnContainerLight,
    tertiary = AccentBlueTertiaryLight,
    onTertiary = AccentBlueOnTertiaryLight,
    tertiaryContainer = AccentBlueContainerLight,
    onTertiaryContainer = AccentBlueOnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

@Composable
fun MyApplicationTheme(
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

