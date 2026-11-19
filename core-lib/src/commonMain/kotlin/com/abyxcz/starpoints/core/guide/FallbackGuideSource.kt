package com.abyxcz.starpoints.core.guide

import com.abyxcz.starpoints.core.ports.GuideSource

/**
 * Several Guide sources asked in order: an anchor's entry comes from the first that has one — the
 * curated packs before the on-device writer, so the writer only fills what the packs leave out.
 * [entries] lists every source's entries, and [voices] every voice once, in the order the sources
 * first name them. The first source's default voice is this source's default: [entryFor] without a
 * voice asks every source in that voice, and when the first source names no voices it answers
 * alone.
 */
class FallbackGuideSource(private val sources: List<GuideSource>) : GuideSource {
    override suspend fun entryFor(anchor: GuideAnchor): GuideEntry? {
        val first = sources.firstOrNull()
        val default = first?.voices()?.firstOrNull()
        return when {
            first == null -> null
            default == null -> first.entryFor(anchor)
            else -> entryFor(anchor, default.id)
        }
    }

    override suspend fun entries(): List<GuideEntry> = sources.flatMap { it.entries() }

    override suspend fun voices(): List<GuideVoice> =
        sources.flatMap { it.voices() }.distinctBy { it.id }

    override suspend fun entryFor(anchor: GuideAnchor, voiceId: String): GuideEntry? =
        sources.firstNotNullOfOrNull {
            it.entryFor(anchor, voiceId)
        }
}
