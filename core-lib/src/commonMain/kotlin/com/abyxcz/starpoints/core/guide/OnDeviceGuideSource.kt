package com.abyxcz.starpoints.core.guide

import com.abyxcz.starpoints.core.ports.GuideSource
import com.abyxcz.starpoints.core.ports.TextWriter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val DEFAULT_ATTEMPTS = 3

/** Tokens allowed per word asked for: English runs about 1.3; the rest is room to finish. */
private const val TOKENS_PER_WORD = 2

/**
 * A sentence: anything up to an end mark that is followed (after closing quotes or a bracket) by
 * whitespace or the end of the text, so the point in "0.03" or "approx. 5" ends nothing.
 */
private val SENTENCE = Regex("""(?s).*?[.!?]+["'”’)]*(?=\s|$)\s*""")

/** An opening quote and the closing quote that pairs with it, when a reply is wrapped in one. */
private val WRAPPERS = mapOf('"' to '"', '“' to '”')

/**
 * A Guide source that writes entries on the device: for an anchor and a voice it renders the prompt
 * from the anchor's fact sheet, has [writer] write the body, and keeps it only when [checkEntry]
 * finds nothing wrong — the right length, whole sentences, no number the fact sheet does not state,
 * no denied phrase. A reply is first cut to its whole sentences within the voice's length
 * ([fitToLength]): a small model overruns a word limit far more often than it breaks the other
 * rules. A body that fails is written again with the next seed, up to [attempts] times; when none
 * passes the source has no entry for that anchor and voice, and does not ask again.
 *
 * An entry's title is the fact sheet's title (computed, never written) and its origin is
 * [GuideOrigin.FreshlyWritten] with [TextWriter.model]. What it wrote is kept for the source's
 * lifetime and listed by [entries]. Writing is one call at a time; a lookup that is already
 * answered never waits behind it. A star or constellation [factSheetFor] has no sheet for gets no
 * entry — the writer never writes about an object without its facts — and is asked again on the
 * next lookup, since the sheet may only be missing for now. A topic has no sheet by nature and is
 * written with [NO_FACTS].
 *
 * Exceptions from [writer] (no model on the device, say) reach the caller.
 *
 * @param template the prompt template, see [renderEntryPrompt].
 * @param voices the voices it writes in, its default first.
 * @param factSheetFor the anchor's fact sheet; runs while writing and must not call this source.
 */
class OnDeviceGuideSource(
    private val writer: TextWriter,
    private val template: String,
    private val voices: List<WriterVoice>,
    private val factSheetFor: suspend (GuideAnchor) -> FactSheet?,
    private val attempts: Int = DEFAULT_ATTEMPTS,
) : GuideSource {
    init {
        require(voices.isNotEmpty()) { "an on-device source writes in at least one voice" }
        require(voices.map { it.voice.id }.toSet().size == voices.size) { "a voice repeats" }
        require(attempts >= 1) { "attempts must be at least 1, not $attempts" }
    }

    /** Guards the two caches; held only to read or update them, never while writing. */
    private val cache = Mutex()

    /** Serialises writing: one model call at a time, and one write per anchor and voice. */
    private val writing = Mutex()
    private val written = mutableMapOf<Pair<String, String>, GuideEntry>()
    private val refused = mutableSetOf<Pair<String, String>>()

    override suspend fun entryFor(anchor: GuideAnchor): GuideEntry? =
        entryFor(anchor, voices.first().voice.id)

    override suspend fun entries(): List<GuideEntry> = cache.withLock { written.values.toList() }

    override suspend fun voices(): List<GuideVoice> = voices.map { it.voice }

    override suspend fun entryFor(anchor: GuideAnchor, voiceId: String): GuideEntry? {
        val voice = voices.firstOrNull { it.voice.id == voiceId }
        val key = voiceId to anchor.key
        return when {
            voice == null -> null
            isSettled(key) -> cache.withLock { written[key] }
            else -> writing.withLock { writeOnce(anchor, voice, key) }
        }
    }

    private suspend fun isSettled(key: Pair<String, String>): Boolean = cache.withLock {
        key in written || key in refused
    }

    /**
     * Under [writing]: the entry another write settled meanwhile, or a new one, recorded. Only a
     * write that ran and failed every attempt is recorded as refused; a missing sheet is not.
     */
    private suspend fun writeOnce(
        anchor: GuideAnchor,
        voice: WriterVoice,
        key: Pair<String, String>,
    ): GuideEntry? {
        if (isSettled(key)) return cache.withLock { written[key] }
        val sheet = factSheetFor(anchor)
        val hasFacts = sheet != null || anchor is GuideAnchor.Topic
        return if (!hasFacts) null
        else
            write(anchor, voice, sheet).also { entry ->
                cache.withLock { if (entry == null) refused += key else written[key] = entry }
            }
    }

    /** A checked entry for [anchor] in [voice] from [sheet], or null when no attempt passes. */
    private suspend fun write(
        anchor: GuideAnchor,
        voice: WriterVoice,
        sheet: FactSheet?,
    ): GuideEntry? {
        val prompt = renderEntryPrompt(template, voice, anchor, sheet)
        val maxTokens = voice.maxWords * TOKENS_PER_WORD
        val body =
            (1..attempts).firstNotNullOfOrNull { seed ->
                fitToLength(cleanBody(writer.write(prompt, maxTokens, seed)), voice).takeIf {
                    checkEntry(it, voice, sheet).isEmpty()
                }
            }
        return body?.let {
            GuideEntry(
                id = "fresh-${anchor.key}",
                anchor = anchor,
                title = sheet?.title ?: topicTitle(anchor),
                body = it,
                origin = GuideOrigin.FreshlyWritten(writer.model),
                voice = voice.voice.id,
            )
        }
    }
}

/**
 * A written body without the whitespace models like to add, and without the quotes when the whole
 * reply is wrapped in one pair; a quotation that only ends the text keeps its closing mark.
 */
internal fun cleanBody(text: String): String {
    val trimmed = text.trim()
    val close = trimmed.firstOrNull()?.let { WRAPPERS[it] }
    val wrapped = close != null && trimmed.length > 1 && trimmed.last() == close
    return if (wrapped) trimmed.substring(1, trimmed.length - 1).trim() else trimmed
}

/**
 * A topic's title: its slug as words, first letter capitalised ("the-tea-nebula" → "The tea
 * nebula").
 */
private fun topicTitle(anchor: GuideAnchor): String =
    anchor.key.substringAfter(':').replace('-', ' ').replaceFirstChar { it.uppercaseChar() }

/**
 * [body] as its whole sentences, as many as fit [voice]'s maximum length: text after the last whole
 * sentence (a reply the token limit cut off) is dropped, and so are sentences past the limit — when
 * what is kept still reaches the minimum. Otherwise [body] unchanged, for [checkEntry] to refuse.
 * Nothing is added or reworded.
 */
internal fun fitToLength(body: String, voice: WriterVoice): String {
    val kept = StringBuilder()
    for (sentence in SENTENCE.findAll(body).map { it.value }) {
        if (wordCount(kept.toString() + sentence) > voice.maxWords) break
        kept.append(sentence)
    }
    val fitted = kept.toString().trim()
    return if (wordCount(fitted) >= voice.minWords) fitted else body
}

private fun wordCount(text: String): Int = text.split(' ', '\n', '\t').count { it.isNotBlank() }
