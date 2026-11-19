package com.abyxcz.starpoints.core.quiz

import com.abyxcz.starpoints.core.catalog.StarRecord
import com.abyxcz.starpoints.core.presenter.Observer
import com.abyxcz.starpoints.core.presenter.starFacts
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Greenwich, 2026-09-27 21:00 UTC: Vega 58° up at azimuth 263° (west); Sirius below the horizon.
 */
class QuizQuestionTest {
    private val vega = StarRecord(91262, 279.23473f, 38.78369f, 0.03f, 0.0f)
    private val sirius = StarRecord(32349, 101.287201f, -16.716101f, -1.44f, 0.009f)
    private val altair = StarRecord(97649, 297.695801f, 8.868300f, 0.76f, 0.221f)
    private val names = mapOf(91262 to "Vega", 32349 to "Sirius", 97649 to "Altair")
    private val sky =
        QuizSky(listOf(vega, sirius, altair), names, Observer(51.4769, 0.0), 1_790_542_800_000)

    private fun ask(kind: QuizKind, star: StarRecord, seed: Int = 1, inSky: QuizSky = sky) =
        questionOf(kind, star, inSky, Random(seed))

    @Test
    fun theSceneIsAsTheTestsAssume() {
        val siriusAltitude = starFacts(sirius, names, sky.observer, sky.epochMillis).altitudeDegrees
        assertTrue(siriusAltitude < -2, "Sirius at $siriusAltitude°")
    }

    @Test
    fun brighterAsksAboutTwoStarsAndTheSmallerMagnitudeWins() {
        repeat(10) { seed ->
            val q = assertNotNull(ask(QuizKind.BRIGHTER, vega, seed))
            assertEquals(2, q.choices.size)
            assertTrue("Vega" in q.choices)
            val other = q.choices.single { it != "Vega" }
            val expected = if (other == "Sirius") "Sirius" else "Vega"
            assertEquals(expected, q.choices[q.answer])
            assertTrue(q.text.contains(q.choices[0]) && q.text.contains(q.choices[1]))
        }
    }

    @Test
    fun lookAlikesAreNeverCompared() {
        val twin = StarRecord(1, 10f, 10f, 0.2f, 0f)
        val lookAlikes = sky.copy(stars = listOf(vega, twin), names = names + (1 to "Twin"))
        assertNull(ask(QuizKind.BRIGHTER, vega, inSky = lookAlikes))
    }

    @Test
    fun colorOffersFourColoursWithTheRightOne() {
        val q = assertNotNull(ask(QuizKind.COLOR, vega))
        assertEquals(4, q.choices.size)
        assertEquals("white", q.choices[q.answer])
        assertTrue(q.fact.startsWith("Vega is white"))
    }

    @Test
    fun aboveTheHorizonIsYesForVegaAndNoForSirius() {
        val up = assertNotNull(ask(QuizKind.ABOVE_HORIZON, vega))
        assertEquals("Yes", up.choices[up.answer])
        assertEquals("Vega is 58° above the horizon right now.", up.fact)
        val down = assertNotNull(ask(QuizKind.ABOVE_HORIZON, sirius))
        assertEquals("No", down.choices[down.answer])
    }

    @Test
    fun aStarAtTheHorizonGetsNoHorizonOrDirectionQuestion() {
        val nearHorizon =
            (-180..180)
                .map { StarRecord(5, 279.23473f, it / 2f, 2.0f, 0f) }
                .first {
                    val altitude =
                        starFacts(it, names, sky.observer, sky.epochMillis).altitudeDegrees
                    altitude > 0 && altitude < 1.5
                }
        assertNull(ask(QuizKind.ABOVE_HORIZON, nearHorizon))
        assertNull(ask(QuizKind.DIRECTION, nearHorizon))
    }

    @Test
    fun directionIsWestForVegaAndEveryWrongChoiceIsARightAngleAway() {
        repeat(10) { seed ->
            val q = assertNotNull(ask(QuizKind.DIRECTION, vega, seed))
            assertEquals(4, q.choices.size)
            assertEquals("west", q.choices[q.answer])
            assertFalse("northwest" in q.choices || "southwest" in q.choices, q.choices.toString())
        }
        assertNull(ask(QuizKind.DIRECTION, sirius))
    }

    @Test
    fun compassIndexRoundsToTheNearestOfEightPoints() {
        assertEquals(0, compassIndex(0.0))
        assertEquals(0, compassIndex(22.4))
        assertEquals(1, compassIndex(22.5))
        assertEquals(0, compassIndex(359.0))
        assertEquals(6, compassIndex(263.49))
        assertEquals(4, compassIndex(-180.0))
    }

    @Test
    fun quizForPicksOnlyKindsWithAClearAnswer() {
        val vegaKinds = (0 until 40).mapNotNull { quizFor(vega, sky, Random(it))?.kind }.toSet()
        assertEquals(QuizKind.entries.toSet(), vegaKinds)
        val siriusKinds = (0 until 40).mapNotNull { quizFor(sirius, sky, Random(it))?.kind }.toSet()
        assertFalse(QuizKind.DIRECTION in siriusKinds)
        assertEquals(quizFor(vega, sky, Random(7)), quizFor(vega, sky, Random(7)))
    }
}
