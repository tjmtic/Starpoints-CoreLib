package com.abyxcz.starpoints.core.guide

import com.abyxcz.starpoints.core.ports.GuideSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class GuideVoiceTest {
    private val pack = GuideOrigin.Curated("1")
    private val vega = GuideAnchor.Star(91262)

    /** A source with one entry that implements only what the port requires. */
    private class OneEntrySource(private val entry: GuideEntry) : GuideSource {
        override suspend fun entryFor(anchor: GuideAnchor): GuideEntry? = entry.takeIf {
            it.anchor == anchor
        }

        override suspend fun entries(): List<GuideEntry> = listOf(entry)
    }

    @Test
    fun anEntryIsInTheGuideVoiceUnlessToldOtherwise() {
        assertEquals("guide", GuideEntry("vega", vega, "Vega", "Text.", pack).voice)
        assertEquals(
            "plain",
            GuideEntry("vega", vega, "Vega", "Text.", pack, GuideVoice.PLAIN_ID).voice,
        )
    }

    @Test
    fun aVoiceIdIsAnIdAndAVoiceHasAName() {
        assertEquals("guide", GuideVoice("guide", "The Guide", fiction = true).id)
        assertFailsWith<IllegalArgumentException> { GuideVoice("", "Empty", fiction = false) }
        assertFailsWith<IllegalArgumentException> { GuideVoice("the guide", "G", fiction = true) }
        assertFailsWith<IllegalArgumentException> { GuideVoice("guide:2", "G", fiction = true) }
        assertFailsWith<IllegalArgumentException> { GuideVoice("plain", " ", fiction = false) }
        assertFailsWith<IllegalArgumentException> {
            GuideEntry("vega", vega, "Vega", "Text.", pack, voice = "the guide")
        }
    }

    @Test
    fun aOneVoiceSourceAnswersOnlyForItsOwnVoice() = runTest {
        val entry = GuideEntry("vega", vega, "Vega", "Text.", pack, GuideVoice.PLAIN_ID)
        val source = OneEntrySource(entry)
        assertEquals(entry, source.entryFor(vega, "plain"))
        assertNull(source.entryFor(vega, "guide"))
        assertNull(source.entryFor(GuideAnchor.Star(1), "plain"))
    }

    @Test
    fun aSourceListsNoVoicesUnlessItSaysSo() = runTest {
        val source = OneEntrySource(GuideEntry("vega", vega, "Vega", "Text.", pack))
        assertTrue(source.voices().isEmpty())
    }
}
