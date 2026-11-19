package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.math.Equatorial
import com.abyxcz.starpoints.core.math.horizontalToEquatorial
import com.abyxcz.starpoints.core.math.julianDate
import com.abyxcz.starpoints.core.math.localSiderealTimeDegrees

/**
 * Converts the view's look direction to an equatorial sky position.
 *
 * @param view the current sky view.
 * @param observer the observer's geographic position.
 * @param epochMillis the UTC instant in epoch milliseconds.
 * @return the equatorial coordinates of the view centre.
 */
fun viewCenter(view: SkyView, observer: Observer, epochMillis: Long): Equatorial =
    horizontalToEquatorial(
        view.look.altitudeDegrees,
        view.look.azimuthDegrees,
        observer.latitudeDegrees,
        localSiderealTimeDegrees(julianDate(epochMillis), observer.longitudeEastDegrees),
    )
