package com.abyxcz.starpoints.core.ports

import com.abyxcz.starpoints.core.guide.GuideAnchor
import com.abyxcz.starpoints.core.guide.GuideEntry

/**
 * Where Guide entries come from: a bundled content pack, or the on-device writer. Implemented by
 * adapters in the app; the core only defines the port.
 */
interface GuideSource {
    /** The entry for [anchor], or null when this source has none. */
    suspend fun entryFor(anchor: GuideAnchor): GuideEntry?

    /** Every entry this source can list, for browsing and search; may be empty. */
    suspend fun entries(): List<GuideEntry>
}
