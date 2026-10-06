package com.abyxcz.starpoints.core.guide

/**
 * A Guide entry: a short, humorous, openly FICTIONAL piece about an [anchor]. It is never a fact.
 * Computed facts live in fact sheets and the star card's facts; no type holds both, and nothing
 * converts one into the other.
 *
 * @property id the entry's id, unique within its source.
 * @property anchor what the entry is about.
 * @property title the entry's heading.
 * @property body the entry's text: fiction, always shown labelled as such.
 * @property origin where the entry came from.
 */
data class GuideEntry(
    val id: String,
    val anchor: GuideAnchor,
    val title: String,
    val body: String,
    val origin: GuideOrigin,
)

/** Where a Guide entry came from. */
sealed interface GuideOrigin {
    /** Written in the studio, reviewed by a person, and shipped in a content pack. */
    data class Curated(val packVersion: String) : GuideOrigin

    /**
     * Written on the device by [model] because no curated entry exists; shown as freshly written.
     */
    data class FreshlyWritten(val model: String) : GuideOrigin
}
