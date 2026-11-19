package com.abyxcz.starpoints.core.guide

import com.abyxcz.starpoints.core.guide.WriterFixtures.TEMPLATE
import com.abyxcz.starpoints.core.guide.WriterFixtures.guide
import com.abyxcz.starpoints.core.guide.WriterFixtures.plain
import com.abyxcz.starpoints.core.guide.WriterFixtures.vegaSheet
import com.abyxcz.starpoints.core.ports.TextWriter
import com.abyxcz.starpoints.core.ports.WriteRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class OnDeviceGuideSourceTest {
    /** Replies by seed (1, 2, 3, …): the last reply repeats. Records every call. */
    private class ScriptedWriter(private val replies: List<String>) : TextWriter {
        override val model = "TestModel-1B"
        val calls = mutableListOf<Triple<String, Int, Int>>()

        override suspend fun write(request: WriteRequest): String {
            calls += Triple(request.prompt, request.maxTokens, request.seed)
            return replies[minOf(request.seed, replies.size) - 1]
        }
    }

    private val vega = GuideAnchor.Star(91262)
    private val good = "Vega shines at magnitude 0.03 for every traveller."

    private fun source(writer: TextWriter, attempts: Int = 3) =
        OnDeviceGuideSource(
            writer = writer,
            template = TEMPLATE,
            voices = listOf(guide, plain),
            factSheetFor = { if (it == vega) vegaSheet else null },
            attempts = attempts,
        )

    @Test
    fun aPassingBodyBecomesAFreshlyWrittenEntryTitledByTheFactSheet() = runTest {
        val writer = ScriptedWriter(listOf("  “$good” \n"))
        val entry = source(writer).entryFor(vega, GuideVoice.PLAIN_ID)
        assertEquals(
            GuideEntry(
                id = "fresh-star:91262",
                anchor = vega,
                title = "Vega",
                body = good,
                origin = GuideOrigin.FreshlyWritten("TestModel-1B"),
                voice = GuideVoice.PLAIN_ID,
            ),
            entry,
        )
        val (prompt, maxTokens, seed) = writer.calls.single()
        assertEquals(renderEntryPrompt(TEMPLATE, plain, vega, vegaSheet), prompt)
        assertEquals(24, maxTokens)
        assertEquals(1, seed)
    }

    @Test
    fun aFailingBodyIsWrittenAgainWithTheNextSeed() = runTest {
        val writer = ScriptedWriter(listOf("Vega is 25 light years from here.", good))
        val entry = source(writer).entryFor(vega, GuideVoice.PLAIN_ID)
        assertEquals(good, entry?.body)
        assertEquals(listOf(1, 2), writer.calls.map { it.third })
    }

    @Test
    fun whenNoAttemptPassesThereIsNoEntryAndTheSourceDoesNotAskAgain() = runTest {
        val writer = ScriptedWriter(listOf("Vega is 25 light years from here."))
        val source = source(writer, attempts = 2)
        assertNull(source.entryFor(vega, GuideVoice.PLAIN_ID))
        assertNull(source.entryFor(vega, GuideVoice.PLAIN_ID))
        assertEquals(2, writer.calls.size)
    }

    @Test
    fun aLongBodyIsCutToWholeSentencesBeforeItIsChecked() = runTest {
        val writer = ScriptedWriter(listOf("$good And it keeps going on far past the limit."))
        assertEquals(good, source(writer).entryFor(vega, GuideVoice.PLAIN_ID)?.body)
        assertEquals(1, writer.calls.size)
    }

    @Test
    fun anEntryIsWrittenOnceAndListed() = runTest {
        val writer = ScriptedWriter(listOf(good))
        val source = source(writer)
        val first = source.entryFor(vega, GuideVoice.PLAIN_ID)
        assertEquals(first, source.entryFor(vega, GuideVoice.PLAIN_ID))
        assertEquals(1, writer.calls.size)
        assertEquals(listOfNotNull(first), source.entries())
    }

    @Test
    fun eachVoiceIsWrittenSeparatelyAndTheFirstIsTheDefault() = runTest {
        val writer = ScriptedWriter(listOf(good))
        val source = source(writer)
        assertEquals(GuideVoice.GUIDE_ID, source.entryFor(vega)?.voice)
        assertEquals(GuideVoice.PLAIN_ID, source.entryFor(vega, GuideVoice.PLAIN_ID)?.voice)
        assertEquals(2, writer.calls.size)
        assertEquals(listOf(guide.voice, plain.voice), source.voices())
    }

    @Test
    fun anUnknownVoiceOrAnObjectWithoutFactsGetsNoEntryAndNoCall() = runTest {
        val writer = ScriptedWriter(listOf(good))
        val source = source(writer)
        assertNull(source.entryFor(vega, "pirate"))
        assertNull(source.entryFor(GuideAnchor.Star(32349), GuideVoice.PLAIN_ID))
        assertNull(source.entryFor(GuideAnchor.Constellation("Lyr"), GuideVoice.PLAIN_ID))
        assertEquals(0, writer.calls.size)
    }

    @Test
    fun aMissingFactSheetIsAskedForAgainLater() = runTest {
        val writer = ScriptedWriter(listOf(good))
        var loaded = false
        val source =
            OnDeviceGuideSource(
                writer,
                TEMPLATE,
                listOf(plain),
                { if (loaded) vegaSheet else null },
            )
        assertNull(source.entryFor(vega))
        loaded = true
        assertEquals(good, source.entryFor(vega)?.body)
    }

    @Test
    fun aWriteInProgressDoesNotHoldUpListingOrAnsweredLookups() = runTest {
        val release = CompletableDeferred<Unit>()
        val slow =
            object : TextWriter {
                override val model = "Slow"

                override suspend fun write(request: WriteRequest): String {
                    release.await()
                    return good
                }
            }
        val source = source(slow)
        val pending = launch { source.entryFor(vega, GuideVoice.PLAIN_ID) }
        runCurrent()
        assertEquals(emptyList(), source.entries())
        assertNull(source.entryFor(vega, "pirate"))
        release.complete(Unit)
        pending.join()
        assertEquals(good, source.entries().single().body)
    }

    @Test
    fun aTopicIsWrittenWithNoFactsAndTitledByItsSlug() = runTest {
        val writer = ScriptedWriter(listOf("A quiet nebula where travellers stop for tea."))
        val topic = GuideAnchor.Topic("the-tea-nebula")
        val entry = source(writer).entryFor(topic, GuideVoice.PLAIN_ID)
        assertEquals("The tea nebula", entry?.title)
        assertEquals(renderEntryPrompt(TEMPLATE, plain, topic, null), writer.calls.single().first)
    }

    @Test
    fun aWritersFailureReachesTheCaller() = runTest {
        val failing =
            object : TextWriter {
                override val model = "Broken"

                override suspend fun write(request: WriteRequest): String =
                    error("no model on this device")
            }
        assertFailsWith<IllegalStateException> { source(failing).entryFor(vega) }
    }

    @Test
    fun aSourceNeedsAVoiceUniqueVoicesAndAnAttempt() {
        val writer = ScriptedWriter(listOf(good))
        assertFailsWith<IllegalArgumentException> {
            OnDeviceGuideSource(writer, TEMPLATE, emptyList(), { null })
        }
        assertFailsWith<IllegalArgumentException> {
            OnDeviceGuideSource(writer, TEMPLATE, listOf(plain, plain), { null })
        }
        assertFailsWith<IllegalArgumentException> {
            OnDeviceGuideSource(writer, TEMPLATE, listOf(plain), { null }, attempts = 0)
        }
    }
}
