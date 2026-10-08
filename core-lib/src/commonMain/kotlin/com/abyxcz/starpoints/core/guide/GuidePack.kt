package com.abyxcz.starpoints.core.guide

import com.abyxcz.starpoints.core.ports.GuideSource
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** The content pack format this core reads. */
const val GUIDE_PACK_FORMAT = 1

/**
 * A content pack: one voice's curated Guide entries, as shipped in the app. Each entry is in the
 * pack's [voice] and came from this pack's [version]; ids and anchors are unique within the pack,
 * so an anchor has at most one entry per voice.
 */
data class GuidePack(val version: String, val voice: GuideVoice, val entries: List<GuideEntry>) {
    init {
        require(version.isNotBlank()) { "a pack has a version" }
        val notOurs = entries.filter {
            it.voice != voice.id || it.origin != GuideOrigin.Curated(version)
        }
        require(notOurs.isEmpty()) {
            "entries ${notOurs.map { it.id }} are not this pack's (voice ${voice.id}, $version)"
        }
        require(entries.map { it.id }.toSet().size == entries.size) { "an entry id repeats" }
        require(entries.map { it.anchor }.toSet().size == entries.size) { "an anchor repeats" }
    }
}

private val PACK_JSON = Json { ignoreUnknownKeys = true }

@Serializable
private data class PackFile(
    val format: Int,
    val version: String,
    val voice: VoiceFile,
    val entries: List<EntryFile>,
)

@Serializable private data class VoiceFile(val id: String, val name: String, val fiction: Boolean)

@Serializable
private data class EntryFile(
    val id: String,
    val anchor: String,
    val title: String,
    val body: String,
)

/**
 * Parses a content pack:
 * ```
 * {"format": 1, "version": "2026.10.1",
 *  "voice": {"id": "guide", "name": "The Guide", "fiction": true},
 *  "entries": [{"id": "vega", "anchor": "star:91262", "title": "Vega", "body": "..."}]}
 * ```
 *
 * Every entry becomes [GuideOrigin.Curated] with the pack's version, in the pack's voice. Throws
 * [IllegalArgumentException] for malformed JSON, a format other than [GUIDE_PACK_FORMAT], an anchor
 * key [parseAnchorKey] rejects, a blank id, title or body, or a repeated id or anchor.
 */
fun parseGuidePack(json: String): GuidePack {
    val file = PACK_JSON.decodeFromString<PackFile>(json)
    require(file.format == GUIDE_PACK_FORMAT) { "pack format ${file.format} is not supported" }
    val voice = GuideVoice(file.voice.id, file.voice.name, file.voice.fiction)
    val origin = GuideOrigin.Curated(file.version)
    val entries =
        file.entries.map {
            require(it.id.isNotBlank() && it.title.isNotBlank() && it.body.isNotBlank()) {
                "entry '${it.id}' has a blank id, title or body"
            }
            val anchor =
                requireNotNull(parseAnchorKey(it.anchor)) {
                    "entry ${it.id} has a malformed anchor '${it.anchor}'"
                }
            GuideEntry(it.id, anchor, it.title, it.body, origin, voice.id)
        }
    return GuidePack(file.version, voice, entries)
}

/**
 * A Guide source over content packs, one per voice. The first pack's voice is the default, the one
 * [entryFor] without a voice answers in.
 */
class PackGuideSource(private val packs: List<GuidePack>) : GuideSource {
    init {
        require(packs.map { it.voice.id }.toSet().size == packs.size) { "two packs share a voice" }
    }

    private val byVoice: Map<String, Map<GuideAnchor, GuideEntry>> = packs.associate { pack ->
        pack.voice.id to pack.entries.associateBy { it.anchor }
    }

    override suspend fun entryFor(anchor: GuideAnchor): GuideEntry? =
        packs.firstOrNull()?.let { entryFor(anchor, it.voice.id) }

    override suspend fun entries(): List<GuideEntry> = packs.flatMap { it.entries }

    override suspend fun voices(): List<GuideVoice> = packs.map { it.voice }

    override suspend fun entryFor(anchor: GuideAnchor, voiceId: String): GuideEntry? =
        byVoice[voiceId]?.get(anchor)
}
