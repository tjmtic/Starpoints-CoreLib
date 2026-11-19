package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.math.Horizontal
import com.abyxcz.starpoints.core.math.normalizeDegrees
import com.abyxcz.starpoints.core.math.toDegrees
import com.abyxcz.starpoints.core.projection.LookDirection
import com.abyxcz.starpoints.core.projection.cameraBasis
import com.abyxcz.starpoints.core.projection.directionToVec
import com.abyxcz.starpoints.core.projection.project
import kotlin.math.abs
import kotlin.math.atan2

private const val BEHIND_EPSILON = 1e-9
private const val BEHIND_ANGLE_DEGREES = 180.0

/**
 * A guide to a target: where it is on screen, or where to turn to find it.
 *
 * @property onScreen whether the target projects inside the screen.
 * @property x horizontal pixel coordinate (screen centre when off screen).
 * @property y vertical pixel coordinate (screen centre when off screen).
 * @property angleDegrees where to turn, clockwise from screen-up, in [0, 360).
 */
data class Guide(val onScreen: Boolean, val x: Double, val y: Double, val angleDegrees: Double)

/**
 * Computes a guide to a target relative to the current look direction.
 *
 * @param target the sky direction to guide to.
 * @param look the camera's look direction.
 * @param fieldOfViewDegrees horizontal field of view in degrees.
 * @param widthPx screen width in pixels.
 * @param heightPx screen height in pixels.
 * @return the guide.
 */
fun guide(
    target: Horizontal,
    look: LookDirection,
    fieldOfViewDegrees: Double,
    widthPx: Double,
    heightPx: Double,
): Guide {
    val basis = cameraBasis(look)
    val v = directionToVec(target)
    val across = basis.right dot v
    val upward = basis.up dot v
    val behind = abs(across) < BEHIND_EPSILON && abs(upward) < BEHIND_EPSILON
    val angleDegrees =
        if (behind) BEHIND_ANGLE_DEGREES else atan2(across, upward).toDegrees().normalizeDegrees()
    val point = project(target, look, fieldOfViewDegrees, widthPx, heightPx)
    val onScreen = point != null && point.x in 0.0..widthPx && point.y in 0.0..heightPx
    val x = if (onScreen) point.x else widthPx / 2.0
    val y = if (onScreen) point.y else heightPx / 2.0
    return Guide(onScreen, x, y, angleDegrees)
}
