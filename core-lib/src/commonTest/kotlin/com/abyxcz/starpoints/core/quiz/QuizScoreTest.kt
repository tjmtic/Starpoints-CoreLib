package com.abyxcz.starpoints.core.quiz

import kotlin.test.Test
import kotlin.test.assertEquals

class QuizScoreTest {
    @Test
    fun aStreakEarnsMoreUpToItsCap() {
        assertEquals(listOf(10, 12, 14, 16, 18, 18, 18), (1..7).map { pointsFor(it) })
    }

    @Test
    fun rightAnswersAddAndAWrongOneEndsTheStreak() {
        val score =
            listOf(true, true, true, false, true).fold(QuizScore()) { s, right -> s.after(right) }
        assertEquals(
            QuizScore(
                points = 10 + 12 + 14 + 10,
                streak = 1,
                bestStreak = 3,
                answered = 5,
                correct = 4,
            ),
            score,
        )
    }

    @Test
    fun aScoreRoundTripsAndGarbageIsAFreshScore() {
        val score = QuizScore(points = 52, streak = 2, bestStreak = 4, answered = 9, correct = 6)
        assertEquals(score, decodeQuizScore(encodeQuizScore(score)))
        assertEquals(QuizScore(), decodeQuizScore("not json"))
        assertEquals(QuizScore(points = 5), decodeQuizScore("""{"points": 5}"""))
    }
}
