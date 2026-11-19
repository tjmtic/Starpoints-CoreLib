package com.abyxcz.starpoints.core.guide

private val PLACEHOLDER = Regex("""\{\{([^{}]*)}}""")

/** What a topic's prompt says in place of facts: it has none, so it may state no numbers. */
const val NO_FACTS =
    "(none: this is a made-up topic with no real object behind it, so state no numbers at all)"

/**
 * The prompt for one entry about [anchor] in [voice], from [template] (the studio's prompt
 * template, shipped with the app): the voice's name, summary, rules and length, the subject, and
 * its fact sheet as `- key: value` lines ([NO_FACTS] for a topic, whose [sheet] is null).
 *
 * Placeholders: `{{voice.name}}`, `{{voice.summary}}`, `{{voice.rules}}`, `{{length.min}}`,
 * `{{length.max}}`, `{{subject}}`, `{{anchor}}`, `{{facts}}`. Throws [IllegalArgumentException] for
 * any other placeholder, or a `{{` left over.
 */
fun renderEntryPrompt(
    template: String,
    voice: WriterVoice,
    anchor: GuideAnchor,
    sheet: FactSheet?,
): String {
    val values =
        mapOf(
            "voice.name" to voice.voice.name,
            "voice.summary" to voice.summary,
            "voice.rules" to voice.rules.joinToString("\n") { "- $it" },
            "length.min" to voice.minWords.toString(),
            "length.max" to voice.maxWords.toString(),
            "subject" to (sheet?.title ?: subjectOf(anchor)),
            "anchor" to anchor.key,
            "facts" to
                (sheet?.facts?.joinToString("\n") { "- ${it.key}: ${it.value}" } ?: NO_FACTS),
        )
    val prompt =
        PLACEHOLDER.replace(template) { match ->
            val name = match.groupValues[1].trim()
            requireNotNull(values[name]) {
                "the template has an unknown placeholder ${match.value}"
            }
        }
    require("{{" !in prompt) { "the template has a malformed placeholder" }
    return prompt
}

/** The subject when there is no fact sheet: a topic's slug as words, else the anchor's key. */
private fun subjectOf(anchor: GuideAnchor): String =
    when (anchor) {
        is GuideAnchor.Topic -> anchor.slug.replace('-', ' ')
        else -> anchor.key
    }
