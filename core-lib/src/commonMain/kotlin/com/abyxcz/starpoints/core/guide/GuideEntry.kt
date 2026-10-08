package com.abyxcz.starpoints.core.guide

/**
 * A Guide entry: a short written piece about an [anchor], in one [voice]. It is never a computed
 * fact. Computed facts live in fact sheets and the star card's facts; no type holds both, and
 * nothing converts one into the other. In a fiction voice, such as the default Guide voice, the
 * entry is openly fictional; in a plain voice it is a straight note grounded in the anchor's fact
 * sheet.
 *
 * @property id the entry's id: unique among its source's entries in the same voice, so one object
 *   can have an entry with the same id in each voice.
 * @property anchor what the entry is about.
 * @property title the entry's heading.
 * @property body the entry's text, always shown apart from the facts.
 * @property origin where the entry came from.
 * @property voice the id of the [GuideVoice] it is written in.
 */
data class GuideEntry(
    val id: String,
    val anchor: GuideAnchor,
    val title: String,
    val body: String,
    val origin: GuideOrigin,
    val voice: String = GuideVoice.GUIDE_ID,
) {
    init {
        require(isIdText(voice)) { "a voice id has no whitespace or colon: '$voice'" }
    }
}

/** Where a Guide entry came from. */
sealed interface GuideOrigin {
    /** Written in the studio, reviewed by a person, and shipped in a content pack. */
    data class Curated(val packVersion: String) : GuideOrigin

    /**
     * Written on the device by [model] because no curated entry exists; shown as freshly written.
     */
    data class FreshlyWritten(val model: String) : GuideOrigin
}

/**
 * A voice Guide entries are written in. A content pack declares its voice, so the set is open; the
 * app starts with two: [GUIDE_ID], the humorous default, and [PLAIN_ID], straight and educational.
 *
 * @property id the voice's id, which its entries carry: not empty, no whitespace and no colon.
 * @property name the name a reader picks the voice by.
 * @property fiction whether its entries are fiction, shown with the fiction badge, or plain notes
 *   grounded in the anchor's fact sheet.
 */
data class GuideVoice(val id: String, val name: String, val fiction: Boolean) {
    init {
        require(isIdText(id)) { "a voice id has no whitespace or colon: '$id'" }
        require(name.isNotBlank()) { "voice $id has no name" }
    }

    companion object {
        /** The default voice: humorous fiction. */
        const val GUIDE_ID = "guide"

        /** The plain voice: straight, educational notes. */
        const val PLAIN_ID = "plain"
    }
}
