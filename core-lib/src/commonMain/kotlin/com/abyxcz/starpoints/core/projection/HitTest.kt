package com.abyxcz.starpoints.core.projection

import kotlin.math.hypot

private const val MAGNITUDE_WEIGHT = 0.15

/**
 * A star projected onto the screen.
 *
 * @property id catalog identifier.
 * @property x screen x in pixels.
 * @property y screen y in pixels.
 * @property magnitude apparent visual magnitude.
 */
data class ProjectedStar(val id: Int, val x: Double, val y: Double, val magnitude: Double)

/**
 * Picks the star under a tap.
 *
 * Among stars within [radiusPx] of the tap, the one minimizing `distance / radiusPx + 0.15 *
 * magnitude` wins, so a brighter star beats a faint neighbour in a near tie. Returns null when no
 * star is in range.
 *
 * @param stars projected stars.
 * @param tapX tap x in pixels.
 * @param tapY tap y in pixels.
 * @param radiusPx pick radius in pixels.
 * @return the picked star, or null.
 */
fun pickStar(
    stars: List<ProjectedStar>,
    tapX: Double,
    tapY: Double,
    radiusPx: Double,
): ProjectedStar? {
    return stars
        .filter { hypot(it.x - tapX, it.y - tapY) <= radiusPx }
        .minByOrNull { it.score(tapX, tapY, radiusPx) }
}

private fun ProjectedStar.score(tapX: Double, tapY: Double, radiusPx: Double): Double {
    return hypot(x - tapX, y - tapY) / radiusPx + MAGNITUDE_WEIGHT * magnitude
}
