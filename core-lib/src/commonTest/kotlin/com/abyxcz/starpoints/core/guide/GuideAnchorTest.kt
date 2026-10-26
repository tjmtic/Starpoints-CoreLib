package com.abyxcz.starpoints.core.guide

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class GuideAnchorTest {
    @Test
    fun keysNameTheKindAndTheId() {
        assertEquals("star:91262", GuideAnchor.Star(91262).key)
        assertEquals("constellation:Lyr", GuideAnchor.Constellation("Lyr").key)
        assertEquals("topic:the-tea-nebula", GuideAnchor.Topic("the-tea-nebula").key)
        assertEquals("deepsky:M31", GuideAnchor.DeepSky("M31").key)
    }

    @Test
    fun parsingAKeyGivesBackTheAnchor() {
        val anchors =
            listOf(
                GuideAnchor.Star(91262),
                GuideAnchor.Constellation("Ser"),
                GuideAnchor.Topic("the-tea-nebula"),
                GuideAnchor.DeepSky("M31"),
            )
        for (anchor in anchors) assertEquals(anchor, parseAnchorKey(anchor.key))
    }

    @Test
    fun malformedKeysGiveNull() {
        assertNull(parseAnchorKey("star:vega"))
        assertNull(parseAnchorKey("star:"))
        assertNull(parseAnchorKey("star:0"))
        assertNull(parseAnchorKey("star:-5"))
        assertNull(parseAnchorKey("star:+5"))
        assertNull(parseAnchorKey("star:091262"))
        assertNull(parseAnchorKey("constellation: Lyr"))
        assertNull(parseAnchorKey("constellation:Lyr:x"))
        assertNull(parseAnchorKey("topic:the tea nebula"))
        assertNull(parseAnchorKey("constellation:"))
        assertNull(parseAnchorKey("topic: "))
        assertNull(parseAnchorKey("deepsky:"))
        assertNull(parseAnchorKey("deepsky:M 31"))
        assertNull(parseAnchorKey("deepsky:M31:x"))
        assertNull(parseAnchorKey("planet:mars"))
        assertNull(parseAnchorKey(""))
    }

    @Test
    fun anAnchorThatCouldNotBeParsedBackCannotBeBuilt() {
        assertFailsWith<IllegalArgumentException> { GuideAnchor.Star(0) }
        assertFailsWith<IllegalArgumentException> { GuideAnchor.Star(-5) }
        assertFailsWith<IllegalArgumentException> { GuideAnchor.Constellation(" Lyr") }
        assertFailsWith<IllegalArgumentException> { GuideAnchor.Topic("the tea nebula") }
        assertFailsWith<IllegalArgumentException> { GuideAnchor.Topic("") }
        assertFailsWith<IllegalArgumentException> { GuideAnchor.DeepSky("M 31") }
        assertFailsWith<IllegalArgumentException> { GuideAnchor.DeepSky("") }
    }
}
