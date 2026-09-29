package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.math.Horizontal
import com.abyxcz.starpoints.core.projection.LookDirection
import kotlin.test.Test
import kotlin.test.assertEquals

private const val TOLERANCE = 1e-6
private const val SCREEN_WIDTH_PX = 1000.0
private const val SCREEN_HEIGHT_PX = 500.0
private const val FIELD_OF_VIEW_DEGREES = 60.0

private val LOOK = LookDirection(azimuthDegrees = 0.0, altitudeDegrees = 0.0, rollDegrees = 0.0)

class GoToTest {
    @Test
    fun centeredOnLooksAtTarget() {
        val view = SkyView().centeredOn(Horizontal(40.0, 100.0))
        assertEquals(100.0, view.look.azimuthDegrees, TOLERANCE)
        assertEquals(40.0, view.look.altitudeDegrees, TOLERANCE)
        assertEquals(0.0, view.look.rollDegrees, TOLERANCE)
        assertEquals(60.0, view.fieldOfViewDegrees, TOLERANCE)
    }

    @Test
    fun targetAtLookDirectionIsOnScreenAtCentre() {
        val guide =
            guide(
                Horizontal(0.0, 0.0),
                LOOK,
                FIELD_OF_VIEW_DEGREES,
                SCREEN_WIDTH_PX,
                SCREEN_HEIGHT_PX,
            )
        assertEquals(true, guide.onScreen)
        assertEquals(500.0, guide.x, TOLERANCE)
        assertEquals(250.0, guide.y, TOLERANCE)
    }

    @Test
    fun targetAtAzimuth90IsOffScreenAngle90() {
        val guide =
            guide(
                Horizontal(0.0, 90.0),
                LOOK,
                FIELD_OF_VIEW_DEGREES,
                SCREEN_WIDTH_PX,
                SCREEN_HEIGHT_PX,
            )
        assertEquals(false, guide.onScreen)
        assertEquals(90.0, guide.angleDegrees, TOLERANCE)
    }

    @Test
    fun targetAtAltitude90HasAngle0() {
        val guide =
            guide(
                Horizontal(90.0, 0.0),
                LOOK,
                FIELD_OF_VIEW_DEGREES,
                SCREEN_WIDTH_PX,
                SCREEN_HEIGHT_PX,
            )
        assertEquals(0.0, guide.angleDegrees, TOLERANCE)
    }

    @Test
    fun targetAtAzimuth270HasAngle270() {
        val guide =
            guide(
                Horizontal(0.0, 270.0),
                LOOK,
                FIELD_OF_VIEW_DEGREES,
                SCREEN_WIDTH_PX,
                SCREEN_HEIGHT_PX,
            )
        assertEquals(270.0, guide.angleDegrees, TOLERANCE)
    }

    @Test
    fun targetAtAzimuth180HasAngle180() {
        val guide =
            guide(
                Horizontal(0.0, 180.0),
                LOOK,
                FIELD_OF_VIEW_DEGREES,
                SCREEN_WIDTH_PX,
                SCREEN_HEIGHT_PX,
            )
        assertEquals(180.0, guide.angleDegrees, TOLERANCE)
    }

    @Test
    fun roll180MakesAltitude90Angle180() {
        val look = LookDirection(azimuthDegrees = 0.0, altitudeDegrees = 0.0, rollDegrees = 180.0)
        val guide =
            guide(
                Horizontal(90.0, 0.0),
                look,
                FIELD_OF_VIEW_DEGREES,
                SCREEN_WIDTH_PX,
                SCREEN_HEIGHT_PX,
            )
        assertEquals(180.0, guide.angleDegrees, TOLERANCE)
    }
}
