package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val YemeBingoColorScheme = darkColorScheme(
  primary = BingoGoldPrimary,
  onPrimary = BingoOnGold,
  primaryContainer = BingoCardSurfaceVariant,
  onPrimaryContainer = BingoGoldPrimary,
  secondary = BingoNeonPurple,
  onSecondary = Color.White,
  secondaryContainer = BingoBorderPurple,
  onSecondaryContainer = BingoTextLight,
  tertiary = BingoVibrantGreen,
  onTertiary = Color.Black,
  background = BingoDarkBg,
  onBackground = BingoTextLight,
  surface = BingoCardSurface,
  onSurface = BingoTextLight,
  surfaceVariant = BingoCardSurfaceVariant,
  onSurfaceVariant = BingoTextMuted,
  outline = BingoBorderPurple,
  error = BingoBrightRed,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = YemeBingoColorScheme,
    typography = Typography,
    content = content
  )
}
