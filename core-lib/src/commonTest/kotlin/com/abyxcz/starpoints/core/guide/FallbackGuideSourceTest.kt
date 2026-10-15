package com.abyxcz.starpoints.core.guide

import com.abyxcz.starpoints.core.ports.GuideSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class FallbackGuideSourceTest {
    private val vega = GuideAnchor.Star(91262)
    private val deneb = GuideAnchor.Star(102098)
    private val guide = GuideVoice(GuideVoice.GUIDE_ID, "The Guide", fiction = true)
    private val plain = GuideVoice(GuideVoice.PLAIN_ID, "Plain", fiction = false)

    private fun entry(anchor: GuideAnchor, voice: GuideVoice, origin: GuideOrigin) =
        GuideEntry(anchor.key, anchor, anchor.key, "body", origin, voice.id)

    private val curatedVega = entry(vega, guide, GuideOrigin.Curated("2026.10.1"))
    private val curated =
        PackGuideSource(listOf(GuidePack("2026.10.1", guide, listOf(curatedVega))))

    private val freshDeneb = entry(deneb, plain, GuideOrigin.FreshlyWritten("M"))

    /** A writer stand-in: has an entry for every anchor, in either voice; has written Deneb. */
    private val fresh =
        object : GuideSource {
            override suspend fun entryFor(anchor: GuideAnchor) = entryFor(anchor, plain.id)

            override suspend fun entries() = listOf(freshDeneb)

            override suspend fun voices() = listOf(plain, guide)

            override suspend fun entryFor(anchor: GuideAnchor, voiceId: String) =
                entry(
                    anchor,
                    if (voiceId == guide.id) guide else plain,
                    GuideOrigin.FreshlyWritten("M"),
                )
        }

    private val both = FallbackGuideSource(listOf(curated, fresh))

    @Test
    fun theFirstSourceWithAnEntryAnswers() = runTest {
        assertEquals(curatedVega, both.entryFor(vega, guide.id))
        assertEquals(GuideOrigin.FreshlyWritten("M"), both.entryFor(deneb, guide.id)?.origin)
        assertEquals(GuideOrigin.FreshlyWritten("M"), both.entryFor(vega, plain.id)?.origin)
    }

    @Test
    fun theDefaultVoiceIsTheFirstSourcesDefault() = runTest {
        assertEquals(listOf(guide, plain), both.voices())
        assertEquals(curatedVega, both.entryFor(vega))
        assertEquals(guide.id, both.entryFor(deneb)?.voice)
    }

    @Test
    fun entriesListEverySource() = runTest {
        assertEquals(listOf(curatedVega, freshDeneb), both.entries())
    }

    @Test
    fun aLookupWithoutAVoiceNeverAnswersInAnotherVoice() = runTest {
        var calls = 0
        val writesPlainOnly =
            object : GuideSource {
                override suspend fun entryFor(anchor: GuideAnchor) = entryFor(anchor, plain.id)

                override suspend fun entries() = emptyList<GuideEntry>()

                override suspend fun voices() = listOf(plain, guide)

                override suspend fun entryFor(anchor: GuideAnchor, voiceId: String): GuideEntry? {
                    calls++
                    return if (voiceId == plain.id)
                        entry(anchor, plain, GuideOrigin.FreshlyWritten("M"))
                    else null
                }
            }
        val source = FallbackGuideSource(listOf(curated, writesPlainOnly))
        assertNull(source.entryFor(deneb))
        assertEquals(1, calls)
    }

    @Test
    fun aFirstSourceWithoutVoicesAnswersAlone() = runTest {
        val voiceless =
            object : GuideSource {
                override suspend fun entryFor(anchor: GuideAnchor) = curatedVega.takeIf {
                    anchor == vega
                }

                override suspend fun entries() = listOf(curatedVega)
            }
        val source = FallbackGuideSource(listOf(voiceless, fresh))
        assertEquals(curatedVega, source.entryFor(vega))
        assertNull(source.entryFor(deneb))
    }

    @Test
    fun noSourcesMeansNoEntries() = runTest {
        val none = FallbackGuideSource(emptyList())
        assertNull(none.entryFor(vega))
        assertEquals(emptyList(), none.voices())
    }
}
