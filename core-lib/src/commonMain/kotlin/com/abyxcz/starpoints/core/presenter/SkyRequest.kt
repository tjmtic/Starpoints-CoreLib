package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.ports.TextWriter
import com.abyxcz.starpoints.core.ports.WriteRequest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * What a plain-language request ("show me Vega", "what's up in an hour?") asks the app to do. The
 * model only labels the request; the app does the work: it resolves a [Find] or [Describe] name
 * with [search], and moves or switches what it shows.
 */
sealed interface SkyRequest {
    /** Point the user at the object called [name], as the user said it. */
    data class Find(val name: String) : SkyRequest

    /** Open the Guide entry about the object called [name]. */
    data class Describe(val name: String) : SkyRequest

    /** Move the sky time by [minutes]; negative for the past. */
    data class ShiftTime(val minutes: Int) : SkyRequest

    /** Switch [setting] on or off. */
    data class Toggle(val setting: DisplaySetting) : SkyRequest

    /** Anything else, or a reply the app could not read. */
    data object Unknown : SkyRequest
}

/** A display setting a request can switch, by the key the model answers with. */
enum class DisplaySetting(val key: String) {
    CONSTELLATION_LINES("constellation_lines"),
    STAR_NAMES("star_names"),
    CONSTELLATION_NAMES("constellation_names"),
    NIGHT_MODE("night_mode"),
}

/** Longest request the model is shown; anything after it is dropped. */
private const val MAX_REQUEST_CHARS = 200
private const val MAX_REPLY_TOKENS = 48

/** Words that put a time request in the past: the model's sign is not trusted, these are. */
private val PAST = Regex("""\b(ago|earlier|before|back|past|previous|previously|yesterday)\b""")

/**
 * The system turn that labels a request. Measured on MiniCPM5 (1B and 2B) with [REQUEST_GRAMMAR]:
 * the rules in words matter more than the examples, and the request must be the user turn.
 */
const val REQUEST_SYSTEM =
    "You label stargazing requests for an app. You never perform the request and never explain: " +
        "the app does the work. Reply with exactly one JSON object.\n" +
        "Kinds of request:\n" +
        "{\"action\": \"find\", \"target\": name} points the user at an object in the sky; name " +
        "is the object as the user said it.\n" +
        "{\"action\": \"describe\", \"target\": name} opens the Guide entry about an object.\n" +
        "{\"action\": \"advance_time\", \"minutes\": integer} moves the sky forward in time " +
        "(negative minutes for the past).\n" +
        "{\"action\": \"toggle\", \"setting\": one of \"constellation_lines\", \"star_names\", " +
        "\"constellation_names\", \"night_mode\"} switches a display setting.\n" +
        "{\"action\": \"none\"} for anything else.\n" +
        "How to choose: a request that asks where something is, or to show, find, point at or " +
        "take the user to it, is find. A request about what something is, or its story, is " +
        "describe. Only a request that names a time (in an hour, tomorrow, ago) is advance_time. " +
        "A request to turn on, turn off, show or hide lines, names or night mode is toggle.\n" +
        "Examples:\n" +
        "request: what's for dinner\n{\"action\": \"none\"}\n" +
        "request: hide the constellation lines\n" +
        "{\"action\": \"toggle\", \"setting\": \"constellation_lines\"}\n" +
        "request: what's up in two hours?\n{\"action\": \"advance_time\", \"minutes\": 120}\n" +
        "request: tell me about Betelgeuse\n{\"action\": \"describe\", \"target\": \"Betelgeuse\"}\n" +
        "request: point me at Polaris\n{\"action\": \"find\", \"target\": \"Polaris\"}"

/** The only replies the model can give: one shape per action, each field from a fixed set. */
val REQUEST_GRAMMAR =
    """
    root ::= "{\"action\": " ( find | describe | advance | toggle | none ) "}"
    find ::= "\"find\", \"target\": " name
    describe ::= "\"describe\", \"target\": " name
    advance ::= "\"advance_time\", \"minutes\": " int
    toggle ::= "\"toggle\", \"setting\": " setting
    none ::= "\"none\""
    name ::= "\"" [A-Za-z0-9 '.-]{1,40} "\""
    int ::= "-"? [1-9] [0-9]? [0-9]? [0-9]?
    setting ::= "\"constellation_lines\"" | "\"star_names\"" | "\"constellation_names\"" | "\"night_mode\""
    """
        .trimIndent()

private val REPLY_JSON = Json { ignoreUnknownKeys = true }

@Serializable
private data class ReplyFile(
    val action: String,
    val target: String? = null,
    val minutes: Int? = null,
    val setting: String? = null,
)

/**
 * The request the model's [reply] labels, for the user's [request] text. A time shift's direction
 * comes from [request] (past words such as "ago" or "earlier" make it negative), not the model's
 * sign, which small models get wrong. Anything malformed or incomplete is [SkyRequest.Unknown].
 */
fun parseSkyRequest(reply: String, request: String): SkyRequest {
    val file = runCatching { REPLY_JSON.decodeFromString<ReplyFile>(reply.trim()) }.getOrNull()
    val target = file?.target?.trim().orEmpty()
    val past = PAST.containsMatchIn(request.lowercase())
    return when (file?.action) {
        "find" -> if (target.isEmpty()) null else SkyRequest.Find(target)
        "describe" -> if (target.isEmpty()) null else SkyRequest.Describe(target)
        "advance_time" ->
            file.minutes
                ?.takeIf { it != 0 }
                ?.let { m ->
                    SkyRequest.ShiftTime(if (past) -kotlin.math.abs(m) else kotlin.math.abs(m))
                }
        "toggle" ->
            DisplaySetting.entries.firstOrNull { it.key == file.setting }?.let(SkyRequest::Toggle)
        else -> null
    } ?: SkyRequest.Unknown
}

/** Reads plain-language requests with [writer]: deterministic, grammar-shaped, then parsed. */
class SkyRequestReader(private val writer: TextWriter) {
    /**
     * Has [writer] read the system turn now, with nothing after it, so a request made soon after
     * starts from it (a writer that reuses its last prompt's prefix reads only the request then).
     * Worth calling when the reader is about to ask — a search field opening. Exceptions from
     * [writer] reach the caller.
     */
    suspend fun warmUp() {
        writer.write(
            WriteRequest(
                prompt = "",
                maxTokens = 1,
                seed = 1,
                system = REQUEST_SYSTEM,
                grammar = REQUEST_GRAMMAR,
                temperature = 0f,
            )
        )
    }

    /** What [request] asks for. Exceptions from [writer] reach the caller. */
    suspend fun read(request: String): SkyRequest {
        val text = request.trim().take(MAX_REQUEST_CHARS)
        if (text.isEmpty()) return SkyRequest.Unknown
        val reply =
            writer.write(
                WriteRequest(
                    prompt = text,
                    maxTokens = MAX_REPLY_TOKENS,
                    seed = 1,
                    system = REQUEST_SYSTEM,
                    grammar = REQUEST_GRAMMAR,
                    temperature = 0f,
                )
            )
        return parseSkyRequest(reply, text)
    }
}
