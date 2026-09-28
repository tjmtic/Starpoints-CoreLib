package com.abyxcz.starpoints.core.catalog

/**
 * A single star in the catalog.
 *
 * @property id catalog identifier.
 * @property rightAscensionDegrees right ascension in degrees.
 * @property declinationDegrees declination in degrees.
 * @property magnitude apparent visual magnitude.
 * @property colorIndex color index (B−V).
 */
data class StarRecord(
    val id: Int,
    val rightAscensionDegrees: Float,
    val declinationDegrees: Float,
    val magnitude: Float,
    val colorIndex: Float,
)
