package com.abyxcz.starpoints.core.ports

import kotlinx.coroutines.flow.Flow

/**
 * Device frame (phone in portrait): x = right edge, y = top edge (up the screen), z = out of the
 * screen toward the user. The back camera looks along −z.
 *
 * World frame (ENU): x = east, y = north, z = up.
 *
 * The rotation matrix R maps device → world, row-major, 9 values. Its columns are the device axes
 * expressed in the world frame.
 */
enum class HeadingAccuracy {
    UNRELIABLE,
    LOW,
    MEDIUM,
    HIGH,
}

/** True when the heading is not trustworthy enough to rely on without calibration. */
fun HeadingAccuracy.needsCalibration(): Boolean =
    this == HeadingAccuracy.UNRELIABLE || this == HeadingAccuracy.LOW

/**
 * A single device attitude sample.
 *
 * @property rotationMatrix R (device → world), row-major, exactly 9 values.
 * @property headingAccuracy how reliable the heading is.
 * @property magneticDeclinationDegrees to add to a magnetic azimuth to get true north (east
 *   positive); 0.0 when R is already true-north referenced.
 * @property timestampNanos sample time in nanoseconds.
 */
private const val ROTATION_MATRIX_SIZE = 9

data class DeviceAttitude(
    val rotationMatrix: List<Double>,
    val headingAccuracy: HeadingAccuracy,
    val magneticDeclinationDegrees: Double,
    val timestampNanos: Long,
) {
    init {
        require(rotationMatrix.size == ROTATION_MATRIX_SIZE) {
            "rotationMatrix must have exactly $ROTATION_MATRIX_SIZE values, got ${rotationMatrix.size}"
        }
    }
}

/** Source of device attitude samples. */
interface OrientationSource {
    val attitude: Flow<DeviceAttitude>
}
