package com.intelliexpense.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class AppColorPalette(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val cardBorder: Color,
    val cardBorderGlow: Color,
    val primary: Color,
    val primaryGlow: Color,
    val secondary: Color,
    val tertiary: Color,
    val accentAmber: Color,
    val expenseRed: Color,
    val incomeGreen: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val heroGradient: Brush,
    val isDark: Boolean = true
)

// Theme 1: Cyber Obsidian (Ultra-Luxury Dark Fintech / Pitch Black & Cyber Mint)
val CyberObsidianPalette = AppColorPalette(
    background = Color(0xFF05070B),
    surface = Color(0xFF0C1018),
    surfaceElevated = Color(0xFF131A26),
    cardBorder = Color(0xFF1B2433),
    cardBorderGlow = Color(0x3300F5A0),
    primary = Color(0xFF00F5A0),
    primaryGlow = Color(0x2600F5A0),
    secondary = Color(0xFF6366F1),
    tertiary = Color(0xFF06B6D4),
    accentAmber = Color(0xFFFBBF24),
    expenseRed = Color(0xFFFF4757),
    incomeGreen = Color(0xFF00F5A0),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFF8B9BB4),
    textMuted = Color(0xFF4A5568),
    heroGradient = Brush.linearGradient(
        listOf(
            Color(0xFF10192A),
            Color(0xFF0B111D)
        )
    ),
    isDark = true
)

// Theme 2: Royal Amethyst (CRED / Velvet Vault Luxury)
val RoyalAmethystPalette = AppColorPalette(
    background = Color(0xFF07040B),
    surface = Color(0xFF120C1F),
    surfaceElevated = Color(0xFF1A122B),
    cardBorder = Color(0xFF2B1D45),
    cardBorderGlow = Color(0x33A855F7),
    primary = Color(0xFFA855F7),
    primaryGlow = Color(0x26A855F7),
    secondary = Color(0xFFEC4899),
    tertiary = Color(0xFF818CF8),
    accentAmber = Color(0xFFF59E0B),
    expenseRed = Color(0xFFF43F5E),
    incomeGreen = Color(0xFF10B981),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFA197B8),
    textMuted = Color(0xFF5A4D73),
    heroGradient = Brush.linearGradient(
        listOf(
            Color(0xFF1F1238),
            Color(0xFF120B22)
        )
    ),
    isDark = true
)

// Theme 3: Midnight Sapphire (Deep Ocean Navy & Electric Azure)
val MidnightSapphirePalette = AppColorPalette(
    background = Color(0xFF070C18),
    surface = Color(0xFF0E172A),
    surfaceElevated = Color(0xFF18233C),
    cardBorder = Color(0xFF1E2E4D),
    cardBorderGlow = Color(0x3338BDF8),
    primary = Color(0xFF38BDF8),
    primaryGlow = Color(0x2638BDF8),
    secondary = Color(0xFF818CF8),
    tertiary = Color(0xFF34D399),
    accentAmber = Color(0xFFFBBF24),
    expenseRed = Color(0xFFFB7185),
    incomeGreen = Color(0xFF34D399),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF475569),
    heroGradient = Brush.linearGradient(
        listOf(
            Color(0xFF142242),
            Color(0xFF0D172E)
        )
    ),
    isDark = true
)

// Theme 4: Alabaster Pearl (Ultra Clean Light Mode)
val AlabasterPearlPalette = AppColorPalette(
    background = Color(0xFFF6F8FB),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFEDF2F7),
    cardBorder = Color(0xFFE2E8F0),
    cardBorderGlow = Color(0x26059669),
    primary = Color(0xFF059669),
    primaryGlow = Color(0x1A059669),
    secondary = Color(0xFF2563EB),
    tertiary = Color(0xFF0284C7),
    accentAmber = Color(0xFFD97706),
    expenseRed = Color(0xFFDC2626),
    incomeGreen = Color(0xFF059669),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF94A3B8),
    heroGradient = Brush.linearGradient(
        listOf(
            Color(0xFFF0FDF4),
            Color(0xFFFFFFFF)
        )
    ),
    isDark = false
)

// Theme 5: Champagne Quartz (Apple Card Titanium & Burnished Gold Light)
val ChampagneQuartzPalette = AppColorPalette(
    background = Color(0xFFFAF7F2),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF3EEE5),
    cardBorder = Color(0xFFE8E0D2),
    cardBorderGlow = Color(0x33B4833E),
    primary = Color(0xFFB4833E),
    primaryGlow = Color(0x20B4833E),
    secondary = Color(0xFF8C5E26),
    tertiary = Color(0xFF6B6358),
    accentAmber = Color(0xFFD97706),
    expenseRed = Color(0xFFC53030),
    incomeGreen = Color(0xFF1B8755),
    textPrimary = Color(0xFF1A1714),
    textSecondary = Color(0xFF6E675F),
    textMuted = Color(0xFFA8A096),
    heroGradient = Brush.linearGradient(
        listOf(
            Color(0xFFFFFDF9),
            Color(0xFFF5EFE4)
        )
    ),
    isDark = false
)

// Theme 6: Nordic Cobalt (Silicon Valley Fintech / Stripe & Mercury Light)
val NordicCobaltPalette = AppColorPalette(
    background = Color(0xFFF4F7FC),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFEBF1F8),
    cardBorder = Color(0xFFDDE5F0),
    cardBorderGlow = Color(0x334F46E5),
    primary = Color(0xFF4F46E5),
    primaryGlow = Color(0x204F46E5),
    secondary = Color(0xFF0284C7),
    tertiary = Color(0xFF0F766E),
    accentAmber = Color(0xFFD97706),
    expenseRed = Color(0xFFDC2626),
    incomeGreen = Color(0xFF059669),
    textPrimary = Color(0xFF0B132B),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF94A3B8),
    heroGradient = Brush.linearGradient(
        listOf(
            Color(0xFFEEF4FF),
            Color(0xFFFFFFFF)
        )
    ),
    isDark = false
)

// Theme 7: Rose Gold Silk (Haute Horlogerie / Soft Rose Cashmere Light)
val RoseGoldSilkPalette = AppColorPalette(
    background = Color(0xFFFAF5F6),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF5EAEF),
    cardBorder = Color(0xFFEBD9E1),
    cardBorderGlow = Color(0x33BE185D),
    primary = Color(0xFFBE185D),
    primaryGlow = Color(0x20BE185D),
    secondary = Color(0xFF831843),
    tertiary = Color(0xFF7C3AED),
    accentAmber = Color(0xFFD97706),
    expenseRed = Color(0xFFBE123C),
    incomeGreen = Color(0xFF047857),
    textPrimary = Color(0xFF1F0E17),
    textSecondary = Color(0xFF6B5B65),
    textMuted = Color(0xFFA3949D),
    heroGradient = Brush.linearGradient(
        listOf(
            Color(0xFFFDF2F8),
            Color(0xFFFFFFFF)
        )
    ),
    isDark = false
)

// Legacy alias compatibility for existing components
val Slate950 = CyberObsidianPalette.background
val Slate900 = CyberObsidianPalette.background
val Slate850 = CyberObsidianPalette.surface
val Slate800 = CyberObsidianPalette.surfaceElevated
val Slate700 = CyberObsidianPalette.cardBorder
val Slate400 = CyberObsidianPalette.textSecondary
val Slate300 = CyberObsidianPalette.textPrimary
val Slate100 = CyberObsidianPalette.textPrimary

val Emerald500 = CyberObsidianPalette.primary
val Emerald400 = Color(0xFF34D399)
val Emerald600 = Color(0xFF00D485)

val Amber500 = CyberObsidianPalette.accentAmber
val Amber400 = Color(0xFFFBBF24)

val Blue500 = CyberObsidianPalette.secondary
val Blue400 = Color(0xFF60A5FA)

val Violet500 = Color(0xFF8B5CF6)
val Rose500 = CyberObsidianPalette.expenseRed
val Rose400 = Color(0xFFF87171)

val CardBorder = CyberObsidianPalette.cardBorder
