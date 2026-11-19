package com.abyxcz.starpoints.core.quiz

import com.abyxcz.starpoints.core.guide.Fact
import com.abyxcz.starpoints.core.guide.FactSheet
import com.abyxcz.starpoints.core.guide.GuideAnchor
import com.abyxcz.starpoints.core.guide.WriterVoice
import com.abyxcz.starpoints.core.guide.checkEntry
import com.abyxcz.starpoints.core.guide.cleanBody
import com.abyxcz.starpoints.core.guide.fitToLength
import com.abyxcz.starpoints.core.ports.TextWriter
import com.abyxcz.starpoints.core.ports.WriteRequest

private val PLACEHOLDER = Regex("""\{\{([^{}]*)}}""")
private const val DEFAULT_ATTEMPTS = 2

/**
 * The prompt for a reaction to the reader's [chosen] answer to [question], from [template] (content
 * shipped with the app). Placeholders: `{{star}}`, `{{question}}`, `{{chosen}}`, `{{answer}}`,
 * `{{result}}` (`right` or `wrong`), `{{fact}}`. Throws [IllegalArgumentException] for any other.
 */
fun reactionPrompt(template: String, question: QuizQuestion, chosen: Int): String {
    val values =
        mapOf(
            "star" to question.starName,
            "question" to question.text,
            "chosen" to question.choices[chosen],
            "answer" to question.choices[question.answer],
            "result" to if (chosen == question.answer) "right" else "wrong",
            "fact" to question.fact,
        )
    val prompt =
        PLACEHOLDER.replace(template) { match ->
            requireNotNull(values[match.groupValues[1].trim()]) {
                "the template has an unknown placeholder ${match.value}"
            }
        }
    require("{{" !in prompt) { "the template has a malformed placeholder" }
    return prompt
}

/**
 * A short reaction, written by [writer] in [voice], to the reader's answer: the quiz's personality.
 * It never decides the answer — the question already holds it — and it is held to the Guide's
 * checks: [voice]'s length and language, whole sentences, no denied phrase, and no number but the
 * computed fact's and the choices'. Null when no attempt passes; the app then shows the fact alone.
 * Exceptions from [writer] reach the caller.
 */
class QuizReactor(
    private val writer: TextWriter,
    private val template: String,
    private val voice: WriterVoice,
    private val attempts: Int = DEFAULT_ATTEMPTS,
) {
    init {
        require(attempts >= 1) { "attempts must be at least 1, not $attempts" }
    }

    /** A reaction to [chosen] (an index into [question]'s choices), or null. */
    suspend fun react(question: QuizQuestion, chosen: Int): String? {
        require(chosen in question.choices.indices) { "choice $chosen is not one of the choices" }
        val prompt = reactionPrompt(template, question, chosen)
        val sheet =
            FactSheet(
                GuideAnchor.Star(question.starId),
                question.starName,
                listOf(
                    Fact("fact", question.fact),
                    Fact("choices", question.choices.joinToString()),
                ),
            )
        return (1..attempts).firstNotNullOfOrNull { seed ->
            val reply = writer.write(WriteRequest(prompt, voice.maxWords * 2, seed))
            fitToLength(cleanBody(reply), voice).takeIf { checkEntry(it, voice, sheet).isEmpty() }
        }
    }
}
