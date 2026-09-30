package com.abyxcz.starpoints.core.ports

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OrientationTest {

    @Test
    fun needsCalibrationTrueForUnreliableAndLow() {
        assertEquals(true, HeadingAccuracy.UNRELIABLE.needsCalibration())
        assertEquals(true, HeadingAccuracy.LOW.needsCalibration())
    }

    @Test
    fun needsCalibrationFalseForMediumAndHigh() {
        assertEquals(false, HeadingAccuracy.MEDIUM.needsCalibration())
        assertEquals(false, HeadingAccuracy.HIGH.needsCalibration())
    }

    @Test
    fun deviceAttitudeWithEightValuesThrows() {
        assertFailsWith<IllegalArgumentException> {
            DeviceAttitude(
                rotationMatrix = listOf(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0),
                headingAccuracy = HeadingAccuracy.HIGH,
                magneticDeclinationDegrees = 0.0,
                timestampNanos = 0L,
            )
        }
    }

    @Test
    fun deviceAttitudeWithNineValuesSucceeds() {
        val attitude =
            DeviceAttitude(
                rotationMatrix = listOf(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0),
                headingAccuracy = HeadingAccuracy.HIGH,
                magneticDeclinationDegrees = 5.0,
                timestampNanos = 1234L,
            )
        assertEquals(9, attitude.rotationMatrix.size)
        assertEquals(5.0, attitude.magneticDeclinationDegrees, 1e-9)
    }
}
