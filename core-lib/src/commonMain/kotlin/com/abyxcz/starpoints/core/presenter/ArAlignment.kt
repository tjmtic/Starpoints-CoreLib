package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.math.normalizeDegrees
import com.abyxcz.starpoints.core.math.toDegrees
import com.abyxcz.starpoints.core.math.toRadians
import com.abyxcz.starpoints.core.projection.LookDirection
import kotlin.math.atan
import kotlin.math.tan

private const val AZIMUTH_LIMIT_DEGREES = 30.0
private const val ALTITUDE_LIMIT_DEGREES = 15.0
private const val VERTICAL_DEGREES = 90.0

data class AlignmentOffset(
    val azimuthDegrees: Double = 0.0,
    val altitudeDegrees: Double = 0.0,
)

fun AlignmentOffset.nudge(
    dxPx: Double,
    dyPx: Double,
    widthPx: Double,
    fieldOfViewDegrees: Double,
): AlignmentOffset {
    val scale = fieldOfViewDegrees / widthPx
    val azimuth =
        (azimuthDegrees - dxPx * scale).coerceIn(-AZIMUTH_LIMIT_DEGREES, AZIMUTH_LIMIT_DEGREES)
    val altitude =
        (altitudeDegrees + dyPx * scale).coerceIn(-ALTITUDE_LIMIT_DEGREES, ALTITUDE_LIMIT_DEGREES)
    return AlignmentOffset(azimuth, altitude)
}

fun LookDirection.aligned(offset: AlignmentOffset): LookDirection =
    LookDirection(
        azimuthDegrees = (azimuthDegrees + offset.azimuthDegrees).normalizeDegrees(),
        altitudeDegrees =
            (altitudeDegrees + offset.altitudeDegrees).coerceIn(
                -VERTICAL_DEGREES,
                VERTICAL_DEGREES,
            ),
        rollDegrees = rollDegrees,
    )

fun fieldOfViewAtZoom(horizontalFieldOfViewDegrees: Double, zoomRatio: Double): Double {
    if (zoomRatio <= 0.0) return horizontalFieldOfViewDegrees
    val half = horizontalFieldOfViewDegrees.toRadians() / 2.0
    return (2.0 * atan(tan(half) / zoomRatio)).toDegrees()
}
