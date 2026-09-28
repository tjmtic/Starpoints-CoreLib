package com.abyxcz.starpoints.core.projection

import com.abyxcz.starpoints.core.math.Horizontal
import com.abyxcz.starpoints.core.math.toRadians
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * The camera's look direction.
 *
 * @property azimuthDegrees azimuth from north, eastward.
 * @property altitudeDegrees altitude above the horizon.
 * @property rollDegrees roll about the forward axis.
 */
data class LookDirection(
    val azimuthDegrees: Double,
    val altitudeDegrees: Double,
    val rollDegrees: Double,
)

/**
 * A point on the screen in pixels.
 *
 * @property x horizontal pixel coordinate.
 * @property y vertical pixel coordinate.
 */
data class ScreenPoint(val x: Double, val y: Double)

private const val DEGENERATE_LENGTH = 1e-12

/**
 * Projects a horizontal sky direction to a screen pixel using a pinhole model.
 *
 * @param target the sky direction to project.
 * @param look the camera's look direction.
 * @param horizontalFieldOfViewDegrees horizontal field of view in degrees.
 * @param widthPx screen width in pixels.
 * @param heightPx screen height in pixels.
 * @return the screen point, or null if the target is behind the camera.
 */
fun project(
    target: Horizontal,
    look: LookDirection,
    horizontalFieldOfViewDegrees: Double,
    widthPx: Double,
    heightPx: Double,
): ScreenPoint? {
    val targetVec = directionToVec(target)
    val forward = directionToVec(Horizontal(look.altitudeDegrees, look.azimuthDegrees)).normalized()

    val dotForward = forward dot targetVec
    if (dotForward <= 0.0) return null

    val right = computeRight(forward, look.azimuthDegrees)
    val up = right cross forward

    val rollRad = look.rollDegrees.toRadians()
    val cosRoll = cos(rollRad)
    val sinRoll = sin(rollRad)

    val rotatedRight = right * cosRoll + up * sinRoll
    val rotatedUp = up * cosRoll - right * sinRoll

    val f = (widthPx / 2.0) / tan(horizontalFieldOfViewDegrees.toRadians() / 2.0)
    val x = widthPx / 2.0 + f * (rotatedRight dot targetVec) / dotForward
    val y = heightPx / 2.0 - f * (rotatedUp dot targetVec) / dotForward

    return ScreenPoint(x, y)
}

private fun directionToVec(h: Horizontal): Vec3 {
    val az = h.azimuthDegrees.toRadians()
    val alt = h.altitudeDegrees.toRadians()
    return Vec3(sin(az) * cos(alt), cos(az) * cos(alt), sin(alt))
}

private fun computeRight(forward: Vec3, azimuthDegrees: Double): Vec3 {
    val up = Vec3(0.0, 0.0, 1.0)
    val cross = forward cross up
    if (cross.length() < DEGENERATE_LENGTH) {
        val az = azimuthDegrees.toRadians()
        return Vec3(sin(az), cos(az), 0.0)
    }
    return cross.normalized()
}
