package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.projection.LookDirection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val TOLERANCE = 1e-9

class SkyViewTest {
    @Test
    fun defaults() {
        val view = SkyView()
        assertEquals(180.0, view.look.azimuthDegrees, TOLERANCE)
        assertEquals(45.0, view.look.altitudeDegrees, TOLERANCE)
        assertEquals(0.0, view.look.rollDegrees, TOLERANCE)
        assertEquals(60.0, view.fieldOfViewDegrees, TOLERANCE)
        assertFalse(view.nightMode)
    }

    @Test
    fun panRightMovesAzimuthLeft() {
        val view = SkyView()
        val panned = view.pan(dxPx = 100.0, dyPx = 0.0, widthPx = 1000.0)
        assertEquals(174.0, panned.look.azimuthDegrees, TOLERANCE)
    }

    @Test
    fun panWrapsAroundZero() {
        val view =
            SkyView(
                look =
                    LookDirection(azimuthDegrees = 2.0, altitudeDegrees = 45.0, rollDegrees = 0.0)
            )
        val panned = view.pan(dxPx = 100.0, dyPx = 0.0, widthPx = 1000.0)
        assertEquals(356.0, panned.look.azimuthDegrees, TOLERANCE)
    }

    @Test
    fun panDownClampsAltitudeToNinety() {
        val view = SkyView()
        val panned = view.pan(dxPx = 0.0, dyPx = 2000.0, widthPx = 1000.0)
        assertEquals(90.0, panned.look.altitudeDegrees, TOLERANCE)
    }

    @Test
    fun zoomInHalvesFieldOfView() {
        val view = SkyView()
        assertEquals(30.0, view.zoom(scale = 2.0).fieldOfViewDegrees, TOLERANCE)
    }

    @Test
    fun zoomClampsToMinimum() {
        val view = SkyView()
        assertEquals(5.0, view.zoom(scale = 100.0).fieldOfViewDegrees, TOLERANCE)
    }

    @Test
    fun zoomOutClampsToMaximum() {
        val view = SkyView()
        assertEquals(120.0, view.zoom(scale = 0.1).fieldOfViewDegrees, TOLERANCE)
    }

    @Test
    fun zoomNonPositiveIsUnchanged() {
        val view = SkyView()
        assertTrue(view.zoom(scale = 0.0) == view)
    }

    @Test
    fun toggleNightModeTwiceRestoresOriginal() {
        val view = SkyView()
        assertTrue(view.toggleNightMode().toggleNightMode() == view)
    }
}
