package com.intelliexpense.app.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val previewPrimary: Color,
    val previewBackground: Color
) {
    CYBER_OBSIDIAN(
        id = "cyber_obsidian",
        displayName = "Cyber Obsidian",
        subtitle = "Pitch Black & Electric Cyber Mint (Recommended)",
        previewPrimary = Color(0xFF00F5A0),
        previewBackground = Color(0xFF05070B)
    ),
    ROYAL_AMETHYST(
        id = "royal_amethyst",
        displayName = "Royal Amethyst",
        subtitle = "CRED Luxury Velvet & Neon Violet",
        previewPrimary = Color(0xFFA855F7),
        previewBackground = Color(0xFF07040B)
    ),
    MIDNIGHT_SAPPHIRE(
        id = "midnight_sapphire",
        displayName = "Midnight Sapphire",
        subtitle = "Deep Ocean Navy & Electric Azure",
        previewPrimary = Color(0xFF38BDF8),
        previewBackground = Color(0xFF070C18)
    ),
    ALABASTER_PEARL(
        id = "alabaster_pearl",
        displayName = "Alabaster Pearl",
        subtitle = "Minimalist Crisp Porcelain Light Mode",
        previewPrimary = Color(0xFF059669),
        previewBackground = Color(0xFFF6F8FB)
    );

    fun getPalette(): AppColorPalette {
        return when (this) {
            CYBER_OBSIDIAN -> CyberObsidianPalette
            ROYAL_AMETHYST -> RoyalAmethystPalette
            MIDNIGHT_SAPPHIRE -> MidnightSapphirePalette
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
