package com.abyxcz.starpoints.core.catalog

private const val WIDE_FOV_DEGREES = 60.0
private const val NARROW_FOV_DEGREES = 15.0
private const val WIDE_MAGNITUDE = 6.0
private const val NARROW_MAGNITUDE = 8.0

/**
 * Faintest magnitude drawn at a given field of view.
 *
 * 6.0 at 60° and wider, 8.0 at 15° and narrower, linear in between.
 *
 * @param fieldOfViewDegrees field of view in degrees.
 * @return faintest magnitude to draw.
 */
fun magnitudeLimitFor(fieldOfViewDegrees: Double): Double {
    val t =
        ((WIDE_FOV_DEGREES - fieldOfViewDegrees) / (WIDE_FOV_DEGREES - NARROW_FOV_DEGREES))
            .coerceIn(0.0, 1.0)
    return WIDE_MAGNITUDE + t * (NARROW_MAGNITUDE - WIDE_MAGNITUDE)
}
