package com.abyxcz.starpoints.core.ports

import com.abyxcz.starpoints.core.presenter.Observer
import kotlinx.coroutines.flow.Flow

/**
 * Source of the observer's geographic position.
 *
 * World frame (ENU): x = east, y = north, z = up.
 */
interface LocationSource {
    val observer: Flow<Observer>
}
