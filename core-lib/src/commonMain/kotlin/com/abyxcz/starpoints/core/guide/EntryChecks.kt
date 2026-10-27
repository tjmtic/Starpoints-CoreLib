package com.abyxcz.starpoints.core.guide

private val THOUSANDS = Regex("""(?<=\d),(?=\d{3}(?!\d))""")

/** A number with its sign, when the sign is not part of a word or a range ("5-10" is 5 and 10). */
private val NUMBER = Regex("""(?<![\w.])[-+]?\d+(?:\.\d+)?|\d+(?:\.\d+)?""")
private val WORD = Regex("""\S+""")
private val SPACES = Regex("""\s+""")
/** The end of a sentence: an end mark, then any closing quotes or bracket, then nothing. */
private val SENTENCE_END = Regex("""[.!?]["'\u201d\u2019)]*$""")
/**
 * Common English words. In English prose they are about a third of the words; in another language
 * almost none. The share is how [checkEntry] tells a voice that wants English it got something
 * else.
 */
private val ENGLISH_WORDS =
    setOf(
        "the",
        "of",
        "and",
        "to",
        "a",
        "in",
        "is",
        "it",
        "you",
        "that",
        "with",
        "for",
        "as",
        "on",
        "are",
        "this",
        "be",
        "at",
        "by",
        "your",
        "from",
        "its",
        "an",
        "or",
        "but",
        "not",
        "can",
        "will",
        "has",
        "have",
        "their",
        "they",
        "was",
        "were",
        "which",
        "who",
        "here",
        "there",
    )

/** Below this share of [ENGLISH_WORDS], a body is not English. */
private const val MIN_ENGLISH_SHARE = 0.1

/** Shorter than this, a body is too little to tell its language ("Nice job, well done."). */
private const val MIN_WORDS_FOR_LANGUAGE = 8
private const val ENGLISH = "en"
private val LETTERS = Regex("""[^\p{L}']+""")

private val APOSTROPHES = setOf('’', '‘', '´', '`')

/**
 * The numbers in [text] with their signs, as written but with thousands separators dropped, an
 * number's leading zeros and a decimal's trailing zeros removed, and a plus sign (or none) read as
 * positive: "41,411" is `41411`, "+38°" and "38" are `38`, "01″" is `1`, "05.30" is `5.3`, "0.0" is
 * `0`, "−1.44" and "-1.44" are `-1.44`. Numbers spelled as words are not seen.
 */
fun numbersIn(text: String): Set<String> =
    NUMBER.findAll(THOUSANDS.replace(text.replace('−', '-'), ""))
        .map { canonical(it.value) }
        .toSet()

private fun canonical(number: String): String {
    val negative = number.startsWith('-')
    val digits = number.trimStart('-', '+')
    val whole = digits.substringBefore('.').trimStart('0').ifEmpty { "0" }
    val fraction = digits.substringAfter('.', "").trimEnd('0')
    val value = if (fraction.isEmpty()) whole else "$whole.$fraction"
    return if (negative && value.any { it in '1'..'9' }) "-$value" else value
}

/**
 * [text] as denied phrases are matched against it: curly quotes and accents used as apostrophes
 * made plain, runs of whitespace (newlines too) made one space, lower case. (The studio's generator
 * also NFKC-normalises; common Kotlin has no Normalizer, and the deny lists are plain ASCII.)
 */
fun plainText(text: String): String =
    SPACES.replace(text.map { if (it in APOSTROPHES) '\'' else it }.joinToString(""), " ")
        .lowercase()

/**
 * What is wrong with [body] as an entry in [voice] about the object of [sheet] (null for a topic):
 * its length in words; whether it ends a sentence (a reply cut off mid-sentence does not); any
 * number, with its sign, that appears nowhere in the fact sheet (when the voice allows only those;
 * none at all for a topic); whether it is in English, when the voice's [WriterVoice.language] is
 * `en`; and any of the voice's denied phrases, matched on [plainText]. Empty when the entry passes.
 * The number check compares numbers, not what they measure: a fact sheet's 38 (degrees) lets "38
 * stars" through.
 */
fun checkEntry(body: String, voice: WriterVoice, sheet: FactSheet?): List<String> {
    val problems = mutableListOf<String>()
    val words = WORD.findAll(body).count()
    if (words !in voice.minWords..voice.maxWords) {
        problems += "$words words; ${voice.voice.id} wants ${voice.minWords} to ${voice.maxWords}"
    }
    if (!SENTENCE_END.containsMatchIn(body.trimEnd())) problems += "does not end a sentence"
    if (voice.numbers == NumberRule.FACT_SHEET_ONLY) {
        val allowed = sheet?.facts?.flatMap { numbersIn(it.value) }?.toSet().orEmpty()
        val stray = numbersIn(body) - allowed
        if (stray.isNotEmpty())
            problems += "states ${stray.sorted()}, which the fact sheet does not"
    }
    val judged = words >= MIN_WORDS_FOR_LANGUAGE
    if (voice.language == ENGLISH && judged && englishShare(body) < MIN_ENGLISH_SHARE) {
        problems += "is not in English"
    }
    val plain = plainText(body)
    voice.denyPhrases
        .filter { plainText(it) in plain }
        .forEach { problems += "uses the denied phrase '$it'" }
    return problems
}

/** The share of [text]'s words that are common English words; 0 for no words. */
internal fun englishShare(text: String): Double {
    val words = text.lowercase().split(LETTERS).filter { it.isNotEmpty() }
    return if (words.isEmpty()) 0.0 else words.count { it in ENGLISH_WORDS }.toDouble() / words.size
}
