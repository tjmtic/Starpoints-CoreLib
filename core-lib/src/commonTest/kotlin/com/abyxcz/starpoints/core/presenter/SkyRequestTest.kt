package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.ports.TextWriter
import com.abyxcz.starpoints.core.ports.WriteRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SkyRequestTest {
    @Test
    fun eachActionParses() {
        assertEquals(
            SkyRequest.Find("Vega"),
            parseSkyRequest("""{"action": "find", "target": "Vega"}""", "show me Vega"),
        )
        assertEquals(
            SkyRequest.Describe("Deneb"),
            parseSkyRequest("""{"action": "describe", "target": " Deneb "}""", "about Deneb"),
        )
        assertEquals(
            SkyRequest.ShiftTime(60),
            parseSkyRequest("""{"action": "advance_time", "minutes": 60}""", "in an hour"),
        )
        assertEquals(
            SkyRequest.Toggle(DisplaySetting.NIGHT_MODE),
            parseSkyRequest("""{"action": "toggle", "setting": "night_mode"}""", "night mode on"),
        )
        assertEquals(SkyRequest.Unknown, parseSkyRequest("""{"action": "none"}""", "play music"))
    }

    @Test
    fun theRequestsWordsSetATimeShiftsDirection() {
        val reply = """{"action": "advance_time", "minutes": -30}"""
        assertEquals(SkyRequest.ShiftTime(30), parseSkyRequest(reply, "go forward 30 minutes"))
        val back = """{"action": "advance_time", "minutes": 120}"""
        assertEquals(SkyRequest.ShiftTime(-120), parseSkyRequest(back, "two hours ago"))
        assertEquals(
            SkyRequest.ShiftTime(-15),
            parseSkyRequest(back.replace("120", "15"), "15 minutes EARLIER"),
        )
    }

    @Test
    fun anythingMalformedOrIncompleteIsUnknown() {
        listOf(
                "not json",
                "",
                """{"action": "find"}""",
                """{"action": "find", "target": "  "}""",
                """{"action": "advance_time", "minutes": 0}""",
                """{"action": "advance_time"}""",
                """{"action": "toggle", "setting": "volume"}""",
                """{"action": "dance"}""",
            )
            .forEach { assertEquals(SkyRequest.Unknown, parseSkyRequest(it, "x"), it) }
    }

    @Test
    fun theReaderAsksDeterministicallyWithTheSystemTurnAndGrammar() = runTest {
        val seen = mutableListOf<WriteRequest>()
        val writer =
            object : TextWriter {
                override val model = "Test"

                override suspend fun write(request: WriteRequest): String {
                    seen += request
                    return """{"action": "find", "target": "Sirius"}"""
                }
            }
        val reader = SkyRequestReader(writer)
        assertEquals(SkyRequest.Find("Sirius"), reader.read("  take me to Sirius  "))
        val asked = seen.single()
        assertEquals("take me to Sirius", asked.prompt)
        assertEquals(REQUEST_SYSTEM, asked.system)
        assertEquals(REQUEST_GRAMMAR, asked.grammar)
        assertEquals(0f, asked.temperature)
        assertEquals(SkyRequest.Unknown, reader.read("   "))
        assertEquals(1, seen.size)
        assertEquals(
            200,
            reader.let {
                it.read("a".repeat(500))
                seen.last().prompt.length
            },
        )
    }

    @Test
    fun warmingUpReadsTheSystemTurnWithNothingAfterIt() = runTest {
        val seen = mutableListOf<WriteRequest>()
        val writer =
            object : TextWriter {
                override val model = "Test"

                override suspend fun write(request: WriteRequest): String {
                    seen += request
                    return ""
                }
            }
        SkyRequestReader(writer).warmUp()
        val asked = seen.single()
        assertEquals("", asked.prompt)
        assertEquals(REQUEST_SYSTEM, asked.system)
        assertEquals(1, asked.maxTokens)
    }

    @Test
    fun theGrammarHasOneShapePerAction() {
        listOf("find", "describe", "advance", "toggle", "none", "name", "int", "setting").forEach {
            assertTrue(REQUEST_GRAMMAR.contains("$it ::="), it)
        }
    }
}
