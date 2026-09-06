package com.intelliexpense.app.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val previewPrimary: Color,
    val previewBackground: Color,
    val isDarkTheme: Boolean = true
) {
    CYBER_OBSIDIAN(
        id = "cyber_obsidian",
        displayName = "Cyber Obsidian",
        subtitle = "Pitch Black & Electric Cyber Mint (Recommended)",
        previewPrimary = Color(0xFF00F5A0),
        previewBackground = Color(0xFF05070B),
        isDarkTheme = true
    ),
    ROYAL_AMETHYST(
        id = "royal_amethyst",
        displayName = "Royal Amethyst",
        subtitle = "CRED Luxury Velvet & Neon Violet",
        previewPrimary = Color(0xFFA855F7),
        previewBackground = Color(0xFF07040B),
        isDarkTheme = true
    ),
    MIDNIGHT_SAPPHIRE(
        id = "midnight_sapphire",
        displayName = "Midnight Sapphire",
        subtitle = "Deep Ocean Navy & Electric Azure",
        previewPrimary = Color(0xFF38BDF8),
        previewBackground = Color(0xFF070C18),
        isDarkTheme = true
    ),
    CHAMPAGNE_QUARTZ(
        id = "champagne_quartz",
        displayName = "Champagne Quartz",
        subtitle = "Apple Card Titanium & Burnished Gold Light",
        previewPrimary = Color(0xFFB4833E),
        previewBackground = Color(0xFFFAF7F2),
        isDarkTheme = false
    ),
    NORDIC_COBALT(
        id = "nordic_cobalt",
        displayName = "Nordic Cobalt",
        subtitle = "Stripe & Mercury Silicon Valley Indigo Light",
        previewPrimary = Color(0xFF4F46E5),
        previewBackground = Color(0xFFF4F7FC),
        isDarkTheme = false
    ),
    ROSE_GOLD_SILK(
        id = "rose_gold_silk",
        displayName = "Rose Gold Silk",
        subtitle = "Haute Horlogerie Soft Cashmere & Rose Gold",
        previewPrimary = Color(0xFFBE185D),
        previewBackground = Color(0xFFFAF5F6),
        isDarkTheme = false
    ),
    ALABASTER_PEARL(
        id = "alabaster_pearl",
        displayName = "Alabaster Emerald",
        subtitle = "Minimalist Crisp Porcelain & Pure Jade Light",
        previewPrimary = Color(0xFF059669),
        previewBackground = Color(0xFFF6F8FB),
        isDarkTheme = false
    );

    fun getPalette(): AppColorPalette {
        return when (this) {
            CYBER_OBSIDIAN -> CyberObsidianPalette
            ROYAL_AMETHYST -> RoyalAmethystPalette
            MIDNIGHT_SAPPHIRE -> MidnightSapphirePalette
            CHAMPAGNE_QUARTZ -> ChampagneQuartzPalette
            NORDIC_COBALT -> NordicCobaltPalette
            ROSE_GOLD_SILK -> RoseGoldSilkPalette
            ALABASTER_PEARL -> AlabasterPearlPalette
        }
    }

    companion object {
        fun fromId(id: String?): ThemeMode {
            return entries.firstOrNull { it.id == id } ?: CYBER_OBSIDIAN
        }
    }
}

val LocalAppColors = compositionLocalOf { CyberObsidianPalette }
