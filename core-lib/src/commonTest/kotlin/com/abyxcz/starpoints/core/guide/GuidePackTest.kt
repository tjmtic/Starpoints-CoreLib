package com.abyxcz.starpoints.core.guide

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class GuidePackTest {
    private fun pack(
        voice: String = "{\"id\":\"guide\",\"name\":\"The Guide\",\"fiction\":true}",
        entries: String =
            "{\"id\":\"vega\",\"anchor\":\"star:91262\",\"title\":\"Vega\",\"body\":\"Weekend comets.\"}," +
                "{\"id\":\"lyra\",\"anchor\":\"constellation:Lyr\",\"title\":\"Lyra\",\"body\":\"In A.\"}",
        format: Int = 1,
    ) = "{\"format\":$format,\"version\":\"2026.10.1\",\"voice\":$voice,\"entries\":[${entries}]}"

    private val plain = "{\"id\":\"plain\",\"name\":\"Plain\",\"fiction\":false}"

    @Test
    fun aPackGivesCuratedEntriesInItsVoice() {
        val parsed = parseGuidePack(pack())
        assertEquals("2026.10.1", parsed.version)
        assertEquals(GuideVoice("guide", "The Guide", fiction = true), parsed.voice)
        assertEquals(
            GuideEntry(
                "vega",
                GuideAnchor.Star(91262),
                "Vega",
                "Weekend comets.",
                GuideOrigin.Curated("2026.10.1"),
                "guide",
            ),
            parsed.entries[0],
        )
        assertEquals(GuideAnchor.Constellation("Lyr"), parsed.entries[1].anchor)
    }

    @Test
    fun unknownFieldsAreIgnored() {
        val withExtra = pack().replace("\"format\":1,", "\"format\":1,\"reviewedBy\":\"tm\",")
        assertEquals(2, parseGuidePack(withExtra).entries.size)
    }

    @Test
    fun aBadPackIsRefused() {
        val vega = "{\"id\":\"vega\",\"anchor\":\"star:91262\",\"title\":\"Vega\",\"body\":\"B.\"}"
        val bad =
            listOf(
                "not json",
                pack(format = 2),
                pack(entries = vega.replace("star:91262", "star:vega")),
                pack(entries = vega.replace("\"Vega\"", "\" \"")),
                pack(entries = vega.replace("\"B.\"", "\"\"")),
                pack(entries = "$vega,$vega"),
                pack(entries = "$vega," + vega.replace("\"id\":\"vega\"", "\"id\":\"vega2\"")),
                pack(voice = plain.replace("\"plain\"", "\"plain text\"")),
                pack().replace("\"version\":\"2026.10.1\"", "\"version\":\"\""),
            )
        for (json in bad) {
            assertFailsWith<IllegalArgumentException>(json) { parseGuidePack(json) }
        }
    }

    @Test
    fun aSourceAnswersInTheVoiceAskedForAndDefaultsToTheFirstPack() = runTest {
        val guide = parseGuidePack(pack())
        val plainPack =
            parseGuidePack(
                pack(
                    voice = plain,
                    entries =
                        "{\"id\":\"vega\",\"anchor\":\"star:91262\",\"title\":\"Vega\",\"body\":\"An A0 star.\"}",
                )
            )
        val source = PackGuideSource(listOf(guide, plainPack))
        val vega = GuideAnchor.Star(91262)
        val lyra = GuideAnchor.Constellation("Lyr")
        assertEquals(listOf(guide.voice, plainPack.voice), source.voices())
        assertEquals("Weekend comets.", source.entryFor(vega)?.body)
        assertEquals("An A0 star.", source.entryFor(vega, "plain")?.body)
        assertEquals("In A.", source.entryFor(lyra, "guide")?.body)
        assertNull(source.entryFor(lyra, "plain"))
        assertNull(source.entryFor(vega, "pirate"))
        assertEquals(guide.entries + plainPack.entries, source.entries())
    }

    @Test
    fun noPacksMeansNoEntries() = runTest {
        val source = PackGuideSource(emptyList())
        assertNull(source.entryFor(GuideAnchor.Star(91262)))
        assertEquals(emptyList(), source.entries())
        assertEquals(emptyList(), source.voices())
    }

    @Test
    fun twoPacksInOneVoiceAreRefused() {
        val guide = parseGuidePack(pack())
        assertFailsWith<IllegalArgumentException> { PackGuideSource(listOf(guide, guide)) }
    }

    @Test
    fun aPackHoldsOnlyItsOwnEntries() {
        val voice = GuideVoice("guide", "The Guide", fiction = true)
        val stray =
            GuideEntry("vega", GuideAnchor.Star(91262), "Vega", "B.", GuideOrigin.Curated("1"))
        assertFailsWith<IllegalArgumentException> { GuidePack("2", voice, listOf(stray)) }
        assertFailsWith<IllegalArgumentException> {
            GuidePack("1", voice.copy(id = "plain"), listOf(stray))
        }
        assertEquals(1, GuidePack("1", voice, listOf(stray)).entries.size)
    }
}
