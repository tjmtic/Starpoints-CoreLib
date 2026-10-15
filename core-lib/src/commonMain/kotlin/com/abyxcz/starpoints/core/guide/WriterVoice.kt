package com.abyxcz.starpoints.core.guide

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * A voice as the on-device writer needs it: what the prompt tells the model, and what every entry
 * it writes is checked against. Read from a voice file (`voices/<id>.json`, the same format the
 * studio's generator uses); the files themselves are content and ship with the app, not the core.
 *
 * @property voice the voice entries are written in.
 * @property summary one or two sentences on the voice, for the prompt.
 * @property rules the rules the prompt lists, one per line.
 * @property minWords the shortest an entry may be, in words.
 * @property maxWords the longest an entry may be, in words.
 * @property numbers which numbers an entry may state.
 * @property denyPhrases phrases no entry may contain; never shown to the model.
 */
data class WriterVoice(
    val voice: GuideVoice,
    val summary: String,
    val rules: List<String>,
    val minWords: Int,
    val maxWords: Int,
    val numbers: NumberRule = NumberRule.FACT_SHEET_ONLY,
    val denyPhrases: List<String> = emptyList(),
) {
    init {
        require(minWords in 1..maxWords) { "voice ${voice.id}: bad length $minWords..$maxWords" }
    }
}

/** Which numbers an entry may state. */
@Serializable
enum class NumberRule {
    /** Only numbers its fact sheet states; none at all for a topic, which has no fact sheet. */
    @SerialName("fact-sheet-only") FACT_SHEET_ONLY,

    /** Any number. */
    @SerialName("any") ANY,
}

private val VOICE_JSON = Json { ignoreUnknownKeys = false }

@Serializable
private data class VoiceProfileFile(
    val id: String,
    val name: String,
    val fiction: Boolean,
    val summary: String,
    val rules: List<String>,
    val length: LengthFile,
    val checks: ChecksFile = ChecksFile(),
)

@Serializable private data class LengthFile(val minWords: Int, val maxWords: Int)

@Serializable
private data class ChecksFile(
    val numbers: NumberRule = NumberRule.FACT_SHEET_ONLY,
    val denyPhrases: List<String> = emptyList(),
)

/**
 * Parses a voice file:
 * ```
 * {"id": "plain", "name": "Plain", "fiction": false, "summary": "...", "rules": ["..."],
 *  "length": {"minWords": 30, "maxWords": 100},
 *  "checks": {"numbers": "fact-sheet-only", "denyPhrases": []}}
 * ```
 *
 * Throws [IllegalArgumentException] for malformed JSON, an unknown key, an invalid voice id or
 * name, or a length whose minimum is not between 1 and its maximum.
 */
fun parseWriterVoice(json: String): WriterVoice {
    val file = VOICE_JSON.decodeFromString<VoiceProfileFile>(json)
    return WriterVoice(
        voice = GuideVoice(file.id, file.name, file.fiction),
        summary = file.summary,
        rules = file.rules,
        minWords = file.length.minWords,
        maxWords = file.length.maxWords,
        numbers = file.checks.numbers,
        denyPhrases = file.checks.denyPhrases,
    )
}
