package com.abyxcz.starpoints.core.quiz

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val BASE_POINTS = 10
private const val STREAK_BONUS = 2

/** The streak bonus stops growing here: 10, 12, 14, 16, 18, then 18 a question. */
private const val MAX_BONUS_STREAK = 5

/** The reader's quiz record, kept between launches. */
@Serializable
data class QuizScore(
    val points: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val answered: Int = 0,
    val correct: Int = 0,
)

/** Points for a right answer that makes the streak [streak] long (1 for the first in a row). */
fun pointsFor(streak: Int): Int =
    BASE_POINTS + STREAK_BONUS * (streak.coerceIn(1, MAX_BONUS_STREAK) - 1)

/**
 * The score after one more answer: a right one adds points and grows the streak, a wrong one ends
 * it.
 */
fun QuizScore.after(right: Boolean): QuizScore =
    if (right) {
        val run = streak + 1
        copy(
            points = points + pointsFor(run),
            streak = run,
            bestStreak = maxOf(bestStreak, run),
            answered = answered + 1,
            correct = correct + 1,
        )
    } else {
        copy(streak = 0, answered = answered + 1)
    }

private val SCORE_JSON = Json { ignoreUnknownKeys = true }

fun encodeQuizScore(score: QuizScore): String = SCORE_JSON.encodeToString(score)

/** The saved score, or a fresh one when [json] is not a score. */
fun decodeQuizScore(json: String): QuizScore = runCatching {
    SCORE_JSON.decodeFromString<QuizScore>(json)
}
    .getOrDefault(QuizScore())
