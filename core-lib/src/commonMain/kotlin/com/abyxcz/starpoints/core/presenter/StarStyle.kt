package com.abyxcz.starpoints.core.presenter

import kotlin.math.max
import kotlin.math.roundToInt

private const val OPAQUE = 0xFF000000.toInt()
private const val MIN_RADIUS_PX = 0.8
private const val BASE_RADIUS_PX = 3.5
private const val RADIUS_PER_MAGNITUDE = 0.5
private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val LUMA_RED = 0.299
private const val LUMA_GREEN = 0.587
private const val LUMA_BLUE = 0.114
private const val NIGHT_MIN_RED = 90

private data class ColorStop(val bv: Double, val r: Int, val g: Int, val b: Int)

private val STOPS =
    listOf(
        ColorStop(bv = -0.4, r = 155, g = 176, b = 255),
        ColorStop(bv = 0.0, r = 248, g = 247, b = 255),
        ColorStop(bv = 0.6, r = 255, g = 244, b = 234),
        ColorStop(bv = 1.0, r = 255, g = 210, b = 161),
        ColorStop(bv = 1.6, r = 255, g = 160, b = 90),
        ColorStop(bv = 2.0, r = 255, g = 120, b = 70),
    )

/**
 * Star radius in pixels for a given magnitude.
 *
 * @param magnitude apparent visual magnitude.
 * @return radius in pixels, at least [MIN_RADIUS_PX].
 */
fun starRadiusPx(magnitude: Double): Double =
    max(MIN_RADIUS_PX, BASE_RADIUS_PX - RADIUS_PER_MAGNITUDE * magnitude)

/**
 * Opaque ARGB color for a star given its B−V color index.
 *
 * @param colorIndex B−V color index.
 * @param nightMode when true, only the red channel is kept (red = luminance).
 * @return opaque ARGB int (0xFFRRGGBB).
 */
fun starColor(colorIndex: Double, nightMode: Boolean): Int {
    val bv = colorIndex.coerceIn(STOPS.first().bv, STOPS.last().bv)
    val upper = STOPS.indexOfFirst { it.bv >= bv }.coerceAtLeast(1)
    val lo = STOPS[upper - 1]
    val hi = STOPS[upper]
    val t = (bv - lo.bv) / (hi.bv - lo.bv)
    val r = lerp(lo.r, hi.r, t)
    val g = lerp(lo.g, hi.g, t)
    val b = lerp(lo.b, hi.b, t)
    return if (nightMode) {
        val lum = max(NIGHT_MIN_RED, (LUMA_RED * r + LUMA_GREEN * g + LUMA_BLUE * b).roundToInt())
        OPAQUE or (lum shl RED_SHIFT)
    } else {
        OPAQUE or (r shl RED_SHIFT) or (g shl GREEN_SHIFT) or b
    }
}

private fun lerp(a: Int, b: Int, t: Double): Int = (a + (b - a) * t).roundToInt()
