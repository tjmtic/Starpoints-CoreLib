package com.abyxcz.starpoints.core.quiz

import com.abyxcz.starpoints.core.catalog.StarRecord
import com.abyxcz.starpoints.core.guide.twoDecimals
import com.abyxcz.starpoints.core.math.normalizeDegrees
import com.abyxcz.starpoints.core.presenter.Observer
import com.abyxcz.starpoints.core.presenter.StarFacts
import com.abyxcz.starpoints.core.presenter.starFacts
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

/** Two stars this close in magnitude look alike: no brighter-or-fainter question between them. */
private const val MIN_MAGNITUDE_GAP = 0.3

/** A star this near the horizon is too close to call (refraction, hills, the observer's height). */
private const val HORIZON_MARGIN_DEGREES = 2.0

/** Near the zenith every direction is "up": no which-way question above this altitude. */
private const val MAX_DIRECTION_ALTITUDE = 75.0

private const val COMPASS_SECTOR = 45.0
private const val HALF_SECTOR = 22.5
private const val DIRECTION_CHOICES = 4
private const val COLOR_CHOICES = 4

/** Two steps of 45°: a wrong direction is at least a right angle from the right one. */
private const val MIN_WRONG_STEPS = 2

private val COMPASS =
    listOf("north", "northeast", "east", "southeast", "south", "southwest", "west", "northwest")

/** Every colour colorName gives, in its order. */
private val COLORS = listOf("blue-white", "white", "yellow-white", "yellow", "orange", "red")

private const val YES = "Yes"
private const val NO = "No"

/** What a question asks about a star. */
enum class QuizKind {
    /** Which of two stars looks brighter. */
    BRIGHTER,

    /** The star's colour. */
    COLOR,

    /** Whether the star is above the horizon now. */
    ABOVE_HORIZON,

    /** Which way to face to see it now. */
    DIRECTION,
}

/**
 * A quiz question about one star, worked out by the core from the catalog and the sky at the
 * question's moment: [answer] indexes [choices] and is right by construction. A language model may
 * react to an answer ([QuizReactor]); it never sets the question or decides what is right.
 *
 * @property fact a computed sentence explaining the answer, shown once the reader has answered.
 */
data class QuizQuestion(
    val starId: Int,
    val starName: String,
    val kind: QuizKind,
    val text: String,
    val choices: List<String>,
    val answer: Int,
    val fact: String,
) {
    init {
        require(answer in choices.indices) {
            "answer $answer is not one of ${choices.size} choices"
        }
        require(choices.toSet().size == choices.size) { "a choice repeats: $choices" }
    }
}

/** The sky a question is asked about: the catalog, where the reader is, and when. */
data class QuizSky(
    val stars: List<StarRecord>,
    val names: Map<Int, String>,
    val observer: Observer,
    val epochMillis: Long,
)

/** The star a question is about, with its facts at the sky's moment. */
private class Subject(val star: StarRecord, val facts: StarFacts, val name: String)

/**
 * A question about [star] in [sky], of a kind [random] picks among those that have a clear answer
 * now: no brighter-or-fainter between look-alikes, no above-the-horizon for a star within 2° of it,
 * no which-way for a star below the horizon or near the zenith. Null only when no kind applies.
 */
fun quizFor(star: StarRecord, sky: QuizSky, random: Random): QuizQuestion? =
    QuizKind.entries.shuffled(random).firstNotNullOfOrNull { questionOf(it, star, sky, random) }

/** A question of [kind] about [star], or null when that kind has no clear answer now. */
internal fun questionOf(
    kind: QuizKind,
    star: StarRecord,
    sky: QuizSky,
    random: Random,
): QuizQuestion? {
    val facts = starFacts(star, sky.names, sky.observer, sky.epochMillis)
    val subject = Subject(star, facts, facts.name ?: "HIP ${star.id}")
    return when (kind) {
        QuizKind.BRIGHTER -> brighter(subject, sky, random)
        QuizKind.COLOR -> color(subject, random)
        QuizKind.ABOVE_HORIZON -> aboveHorizon(subject)
        QuizKind.DIRECTION -> direction(subject, random)
    }
}

private fun brighter(s: Subject, sky: QuizSky, random: Random): QuizQuestion? {
    val other =
        sky.stars
            .filter { it.id != s.star.id && it.id in sky.names }
            .filter { abs(it.magnitude - s.star.magnitude) >= MIN_MAGNITUDE_GAP }
            .randomOrNull(random) ?: return null
    val otherName = sky.names.getValue(other.id)
    val choices = listOf(s.name, otherName).shuffled(random)
    val brighter = if (s.star.magnitude < other.magnitude) s.name else otherName
    return QuizQuestion(
        s.star.id,
        s.name,
        QuizKind.BRIGHTER,
        "Which looks brighter in the sky: ${choices[0]} or ${choices[1]}?",
        choices,
        choices.indexOf(brighter),
        "${s.name} shines at magnitude ${twoDecimals(s.star.magnitude.toDouble())} and $otherName " +
            "at ${twoDecimals(other.magnitude.toDouble())}: the smaller the number, the brighter.",
    )
}

private fun color(s: Subject, random: Random): QuizQuestion {
    val right = s.facts.colorName
    val choices =
        (listOf(right) + (COLORS - right).shuffled(random).take(COLOR_CHOICES - 1)).shuffled(random)
    return QuizQuestion(
        s.star.id,
        s.name,
        QuizKind.COLOR,
        "What colour does ${s.name} shine?",
        choices,
        choices.indexOf(right),
        "${s.name} is $right: its colour index is ${twoDecimals(s.star.colorIndex.toDouble())}.",
    )
}

private fun aboveHorizon(s: Subject): QuizQuestion? {
    val altitude = s.facts.altitudeDegrees
    if (abs(altitude) < HORIZON_MARGIN_DEGREES) return null
    val above = altitude > 0
    return QuizQuestion(
        s.star.id,
        s.name,
        QuizKind.ABOVE_HORIZON,
        "Is ${s.name} above the horizon right now?",
        listOf(YES, NO),
        if (above) 0 else 1,
        "${s.name} is ${abs(altitude).roundToInt()}° ${if (above) "above" else "below"} the horizon " +
            "right now.",
    )
}

private fun direction(s: Subject, random: Random): QuizQuestion? {
    val altitude = s.facts.altitudeDegrees
    if (altitude < HORIZON_MARGIN_DEGREES || altitude > MAX_DIRECTION_ALTITUDE) return null
    val index = compassIndex(s.facts.azimuthDegrees)
    val wrong =
        (MIN_WRONG_STEPS..COMPASS.size - MIN_WRONG_STEPS)
            .map { COMPASS[(index + it) % COMPASS.size] }
            .shuffled(random)
            .take(DIRECTION_CHOICES - 1)
    val choices = (wrong + COMPASS[index]).shuffled(random)
    return QuizQuestion(
        s.star.id,
        s.name,
        QuizKind.DIRECTION,
        "Which way do you face to see ${s.name} right now?",
        choices,
        choices.indexOf(COMPASS[index]),
        "Face ${COMPASS[index]}: ${s.name} is at azimuth ${s.facts.azimuthDegrees.roundToInt()}°, " +
            "${altitude.roundToInt()}° up.",
    )
}

/** The eight-point compass direction an azimuth falls in (north = 0, eastward). */
internal fun compassIndex(azimuthDegrees: Double): Int =
    ((azimuthDegrees.normalizeDegrees() + HALF_SECTOR) / COMPASS_SECTOR).toInt() % COMPASS.size
