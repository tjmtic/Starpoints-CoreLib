package com.abyxcz.starpoints.core.ports

import com.abyxcz.starpoints.core.guide.GuideAnchor
import com.abyxcz.starpoints.core.guide.GuideEntry
import com.abyxcz.starpoints.core.guide.GuideVoice

/**
 * Where Guide entries come from: a bundled content pack, or the on-device writer. Implemented by
 * adapters in the app; the core only defines the port. A source may hold entries in several voices;
 * the reader picks one.
 */
interface GuideSource {
    /** The entry for [anchor] in this source's default voice, or null when it has none. */
    suspend fun entryFor(anchor: GuideAnchor): GuideEntry?

    /** Every entry this source can list, in every voice, for browsing and search; may be empty. */
    suspend fun entries(): List<GuideEntry>

    /** The voices this source has entries in, its default voice first; none by default. */
    suspend fun voices(): List<GuideVoice> = emptyList()

    /**
     * The entry for [anchor] in the voice [voiceId], or null when this source has none. By default
     * it is [entryFor]'s entry when that is in [voiceId], which is right for a one-voice source.
     */
    suspend fun entryFor(anchor: GuideAnchor, voiceId: String): GuideEntry? =
        entryFor(anchor)?.takeIf { it.voice == voiceId }
}
