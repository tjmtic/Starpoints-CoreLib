package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.catalog.StarRecord
import com.abyxcz.starpoints.core.math.Horizontal
import com.abyxcz.starpoints.core.math.equatorialToHorizontal
import com.abyxcz.starpoints.core.math.julianDate
import com.abyxcz.starpoints.core.math.localSiderealTimeDegrees

/**
 * Caches star horizontal positions, recomputing at most once per [refreshMillis].
 *
 * @property stars the catalog records.
 * @property refreshMillis minimum interval between recomputations in milliseconds.
 */
class StarPositionCache(
    private val stars: List<StarRecord>,
    private val refreshMillis: Long = 1_000L,
) {
    private var lastObserver: Observer? = null
    private var lastEpochMillis: Long = 0L
    private var lastPositions: List<Horizontal> = emptyList()

    /** Number of times positions were recomputed. */
    var computations: Int = 0
        private set

    /**
     * Returns the apparent horizontal position of every star, in [stars] order.
     *
     * @param observer the observer's position.
     * @param epochMillis the observation instant in epoch milliseconds.
     * @return the horizontal positions.
     */
    fun positions(observer: Observer, epochMillis: Long): List<Horizontal> {
        val needsRecompute =
            lastObserver != observer ||
                kotlin.math.abs(epochMillis - lastEpochMillis) >= refreshMillis
        if (!needsRecompute) return lastPositions

        val jd = julianDate(epochMillis)
        val lst = localSiderealTimeDegrees(jd, observer.longitudeEastDegrees)
        val positions = stars.map { star ->
            equatorialToHorizontal(
                star.rightAscensionDegrees.toDouble(),
                star.declinationDegrees.toDouble(),
                observer.latitudeDegrees,
                lst,
            )
        }
        lastObserver = observer
        lastEpochMillis = epochMillis
        lastPositions = positions
        computations++
        return positions
    }
}
