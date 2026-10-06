package com.abyxcz.starpoints.core.guide

private const val STAR_PREFIX = "star:"
private const val CONSTELLATION_PREFIX = "constellation:"
private const val TOPIC_PREFIX = "topic:"

/**
 * What a Guide entry is about: a catalog star, a constellation, or a free-floating topic with no
 * real object behind it. Content packs and prompts refer to an anchor by its [key].
 */
sealed interface GuideAnchor {
    /** The anchor as text, e.g. `star:91262`, `constellation:Lyr` or `topic:the-tea-nebula`. */
    val key: String

    /** A catalog star, by its Hipparcos id (the catalog's star id). */
    data class Star(val hipId: Int) : GuideAnchor {
        init {
            require(hipId > 0) { "a Hipparcos id is positive, not $hipId" }
        }

        override val key: String
            get() = STAR_PREFIX + hipId
    }

    /**
     * A constellation, by its catalog id (`Lyr`). Serpens has two catalog entries, Caput and Cauda,
     * which share the id `Ser` and so the one anchor.
     */
    data class Constellation(val id: String) : GuideAnchor {
        init {
            require(isIdText(id)) { "a constellation id has no whitespace or colon: '$id'" }
        }

        override val key: String
            get() = CONSTELLATION_PREFIX + id
    }

    /** A free-floating topic: a fictional place, era or being, by a short slug. */
    data class Topic(val slug: String) : GuideAnchor {
        init {
            require(isIdText(slug)) { "a topic slug has no whitespace or colon: '$slug'" }
        }

        override val key: String
            get() = TOPIC_PREFIX + slug
    }
}

/**
 * The anchor a [GuideAnchor.key] names, or null when the key is malformed. Only the canonical form
 * is accepted, the one [GuideAnchor.key] writes: a star id is a positive number with no sign or
 * leading zero, and a constellation id or topic slug has no whitespace or colon.
 */
fun parseAnchorKey(key: String): GuideAnchor? {
    val anchor =
        when {
            key.startsWith(STAR_PREFIX) ->
                key.removePrefix(STAR_PREFIX)
                    .toIntOrNull()
                    ?.takeIf { it > 0 }
                    ?.let { GuideAnchor.Star(it) }
            key.startsWith(CONSTELLATION_PREFIX) ->
                key.removePrefix(CONSTELLATION_PREFIX).takeIf(::isIdText)?.let {
                    GuideAnchor.Constellation(it)
                }
            key.startsWith(TOPIC_PREFIX) ->
                key.removePrefix(TOPIC_PREFIX).takeIf(::isIdText)?.let { GuideAnchor.Topic(it) }
            else -> null
        }
    return anchor?.takeIf { it.key == key }
}

/** An id or slug: not empty, with no whitespace and no colon. */
internal fun isIdText(text: String): Boolean =
    text.isNotEmpty() && text.none { it.isWhitespace() || it == ':' }
