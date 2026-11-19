package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.math.normalizeDegrees
import com.abyxcz.starpoints.core.math.toDegrees
import com.abyxcz.starpoints.core.ports.DeviceAttitude
import com.abyxcz.starpoints.core.projection.LookDirection
import com.abyxcz.starpoints.core.projection.Vec3
import com.abyxcz.starpoints.core.projection.cameraBasis
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2

private const val ROW_SIZE = 3
private const val VERTICAL_DEGREES = 90.0
private const val VERTICAL_LIMIT_DEGREES = 0.5

fun lookDirectionFrom(attitude: DeviceAttitude): LookDirection {
    val (row0, row1, row2) = attitude.rotationMatrix.chunked(ROW_SIZE)
    val look =
        lookFrom(
            right = Vec3(row0[0], row1[0], row2[0]),
            up = Vec3(row0[1], row1[1], row2[1]),
            forward = -Vec3(row0[2], row1[2], row2[2]),
        )
    // In R's own frame first: the declination turns only the returned azimuth.
    return look.copy(
        azimuthDegrees =
            (look.azimuthDegrees + attitude.magneticDeclinationDegrees).normalizeDegrees()
    )
}

/**
 * The look direction of a camera whose axes are [right], [up] and [forward] (unit vectors in ENU).
 * The roll is measured against the basis [project] builds for the same look, so [project]
 * reproduces that camera. Shared by [lookDirectionFrom] and [smooth].
 */
internal fun lookFrom(right: Vec3, up: Vec3, forward: Vec3): LookDirection {
    val altitude = asin(forward.z.coerceIn(-1.0, 1.0)).toDegrees()
    val heading = if (abs(altitude) > VERTICAL_DEGREES - VERTICAL_LIMIT_DEGREES) up else forward
    val azimuth = atan2(heading.x, heading.y).toDegrees().normalizeDegrees()
    val basis = cameraBasis(LookDirection(azimuth, altitude, 0.0))
    val roll = atan2(right dot basis.up, right dot basis.right).toDegrees()
    return LookDirection(azimuthDegrees = azimuth, altitudeDegrees = altitude, rollDegrees = roll)
}
