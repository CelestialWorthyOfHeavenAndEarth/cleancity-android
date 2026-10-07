package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// CleanCity: forest green, warm neutral surfaces, semantic waste colors
// Clean, shadow-free, stadium-pill controls, 24px cards, neutral tint ladder
// =========================================================================

// Brand & High-Contrast Ink System
val MobbinPrimary = Color(0xFF126B4D)
val MobbinOnPrimary = Color(0xFFFFFFFF)
val MobbinInk = Color(0xFF17372C)
val MobbinInkSoft = Color(0xFF40594E)
val MobbinTextMuted = Color(0xFF40594E)
val MobbinTextFaint = Color(0xFF52675D)

// Canvas & Surfaces (Warm off-white background with pure white cards)
val MobbinCanvas = Color(0xFFF5F7F2)
val MobbinSurface = Color(0xFFFFFFFF)
val MobbinCanvasSoft = Color(0xFFEBF1E9)
val MobbinField = Color(0xFFF8FAFC)
val MobbinHairlineSoft = Color(0xFFE3E9DF)
val MobbinHairline = Color(0xFFD4DED3)

// Clean Eco Accent (Emerald / Forest green tone, replacing harsh electric blue)
val MobbinAccent = Color(0xFF087653)

// Semantic Content Colors (For waste sorting & status badges)
val TagWetGreen = Color(0xFF047857)
val TagWetGreenBg = Color(0xFFE6F4EA)
val TagDryBlue = Color(0xFF245EA0)
val TagDryBlueBg = Color(0xFFEAF2FC)
val TagHazardRed = Color(0xFFB91C1C)
val TagHazardRedBg = Color(0xFFFEE2E2)
val TagAlertAmber = Color(0xFFB45309)
val TagAlertAmberBg = Color(0xFFFEF3C7)

// Backward Compatibility Aliases
val PaletteWhite = Color(0xFFFFFFFF)
val PaletteBlack = MobbinInk
val PaletteEmerald = Color(0xFF047857)
val PaletteMint = Color(0xFF059669)
val PaletteEmeraldDark = Color(0xFF064E3B)
val PaletteEmeraldLight = Color(0xFFD1FAE5)
val PaletteCreamGold = Color(0xFF047857)
val PaletteOlive = MobbinInk

// Surface aliases mapped to high-contrast gallery system
val DarkBaseBackground = MobbinCanvas
val DarkSurfaceCard = MobbinSurface
val DarkSurfaceElevated = MobbinCanvasSoft
val DarkSurfaceGlass = MobbinSurface
val DarkSurfaceBorder = MobbinHairline
val DarkSurfaceBorderSubtle = MobbinHairlineSoft

// Text aliases mapped to high-contrast slate ink
val TextPrimary = MobbinInk
val TextSecondary = MobbinInkSoft
val TextMuted = MobbinTextFaint

// Containers
val AccentMintContainer = TagWetGreenBg
val AccentCreamContainer = TagDryBlueBg
val AccentSlateContainer = MobbinCanvasSoft
