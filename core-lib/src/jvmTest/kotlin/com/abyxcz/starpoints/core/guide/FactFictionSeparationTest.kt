package com.abyxcz.starpoints.core.guide

import com.abyxcz.starpoints.core.presenter.StarFacts
import java.io.File
import java.lang.reflect.GenericArrayType
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val OUR_PACKAGE = "com.abyxcz.starpoints"
private const val CLASS_SUFFIX = ".class"

/**
 * Computed facts and Guide fiction never share a type. From each fact type, every type its fields
 * can hold is walked — through arrays, type arguments, type-variable bounds, inherited fields, and
 * every subclass in its package (sealed subclasses must live there) — and none may be, extend, or
 * be extended by a fiction type; the same the other way round. A fact type may not hold `Any`,
 * which could carry anything. So fiction can never reach the fact card, and no fact sheet can carry
 * fiction. Uses Java reflection, so it runs on the JVM.
 */
class FactFictionSeparationTest {
    private val factTypes: List<Class<*>> =
        listOf(FactSheet::class.java, Fact::class.java, StarFacts::class.java)

    private val fictionTypes: List<Class<*>> =
        listOf(
            GuideEntry::class.java,
            GuideOrigin::class.java,
            GuideVoice::class.java,
            GuidePack::class.java,
            PackGuideSource::class.java,
        )

    /** Every class named by [type]: array elements, type arguments and bounds, recursively. */
    private fun classesIn(type: Type): List<Class<*>> =
        when (type) {
            is Class<*> -> if (type.isArray) classesIn(type.componentType) else listOf(type)
            is ParameterizedType ->
                classesIn(type.rawType) + type.actualTypeArguments.flatMap { classesIn(it) }
            is WildcardType -> (type.upperBounds + type.lowerBounds).flatMap { classesIn(it) }
            is GenericArrayType -> classesIn(type.genericComponentType)
            is TypeVariable<*> -> type.bounds.flatMap { classesIn(it) }
            else -> emptyList()
        }

    /** [type]'s instance fields, its superclasses' included. */
    private fun instanceFields(type: Class<*>) =
        generateSequence(type) { it.superclass }
            .flatMap { it.declaredFields.asSequence() }
            .filter { !Modifier.isStatic(it.modifiers) }
            .toList()

    /** Every class compiled into [type]'s package (read from the test classpath). */
    private fun packageClasses(type: Class<*>): List<Class<*>> {
        val loader = type.classLoader ?: return emptyList()
        val folders =
            loader.getResources(type.packageName.replace('.', '/')).toList().filter {
                it.protocol == "file"
            }
        val names = folders.flatMap { url ->
            File(url.toURI())
                .listFiles()
                .orEmpty()
                .map { it.name }
                .filter { it.endsWith(CLASS_SUFFIX) }
        }
        return names.mapNotNull {
            runCatching {
                Class.forName(type.packageName + "." + it.removeSuffix(CLASS_SUFFIX), false, loader)
            }
                .getOrNull()
        }
    }

    /** Every class of ours reachable from [start]: field types, nested classes and subclasses. */
    private fun reachable(start: Class<*>): Set<Class<*>> {
        val seen = mutableSetOf<Class<*>>()
        val queue = ArrayDeque(listOf(start))
        while (queue.isNotEmpty()) {
            val type = queue.removeFirst()
            if (!seen.add(type)) continue
            val held = instanceFields(type).flatMap { classesIn(it.genericType) }
            val subclasses = packageClasses(type).filter { it != type && type.isAssignableFrom(it) }
            val next = held + type.declaredClasses + subclasses
            queue.addAll(next.filter { it.name.startsWith(OUR_PACKAGE) })
        }
        return seen
    }

    /** Whether [a] and [b] are the same type or one extends the other (not counting `Any`). */
    private fun related(a: Class<*>, b: Class<*>): Boolean =
        a != Any::class.java &&
            b != Any::class.java &&
            (a.isAssignableFrom(b) || b.isAssignableFrom(a))

    @Test
    fun noFactTypeReachesFiction() {
        for (fact in factTypes) {
            val reached = reachable(fact)
            for (fiction in fictionTypes) {
                val found = reached.filter { related(it, fiction) }
                assertTrue(
                    found.isEmpty(),
                    "${fact.simpleName} can hold ${fiction.simpleName} via $found",
                )
            }
        }
    }

    @Test
    fun noFictionTypeReachesFacts() {
        for (fiction in fictionTypes) {
            val reached = reachable(fiction)
            for (fact in factTypes) {
                val found = reached.filter { related(it, fact) }
                assertTrue(
                    found.isEmpty(),
                    "${fiction.simpleName} can hold ${fact.simpleName} via $found",
                )
            }
        }
    }

    @Test
    fun noFactTypeHoldsAny() {
        for (fact in factTypes) {
            for (type in reachable(fact)) {
                for (field in instanceFields(type)) {
                    assertFalse(
                        classesIn(field.genericType).any { it == Any::class.java },
                        "${type.simpleName}.${field.name} can hold anything",
                    )
                }
            }
        }
    }

    @Test
    fun theWalkSeesEveryKindOfAnchor() {
        val reached = reachable(FactSheet::class.java)
        val anchors =
            listOf(
                GuideAnchor.Star::class.java,
                GuideAnchor.Constellation::class.java,
                GuideAnchor.Topic::class.java,
            )
        assertTrue(reached.containsAll(anchors), "the walk missed an anchor kind: $reached")
    }

    @Test
    fun neitherIsASubtypeOfTheOther() {
        for (fact in factTypes) {
            for (fiction in fictionTypes) {
                assertFalse(
                    related(fact, fiction),
                    "${fact.simpleName} and ${fiction.simpleName} are related",
                )
            }
        }
    }
}
