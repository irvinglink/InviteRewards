package com.github.irvinglink.amethystLibKotlin.utils.math

import java.util.SplittableRandom

class ProbabilityCollection<E> {

    private val entries = mutableListOf<ProbabilityEntry<E>>()
    private val random = SplittableRandom()
    private var totalProbability: Int = 0

    fun size(): Int = entries.size

    fun isEmpty(): Boolean = entries.isEmpty()

    fun contains(obj: E): Boolean = entries.any { it.value == obj }

    fun add(obj: E, probability: Int) {
        require(probability > 0) { "Probability must be greater than 0" }

        totalProbability += probability
        entries += ProbabilityEntry(
            value = obj,
            probability = probability,
            cumulative = totalProbability
        )
    }

    fun remove(obj: E): Boolean {
        val removed = entries.removeAll { it.value == obj }
        if (removed) rebuildCumulativeProbabilities()
        return removed
    }

    fun clear() {
        entries.clear()
        totalProbability = 0
    }

    fun get(): E {
        check(entries.isNotEmpty()) { "Cannot get an object from an empty collection" }

        val roll = random.nextInt(1, totalProbability + 1)
        return entries.first { roll <= it.cumulative }.value
    }

    fun getTotalProbability(): Int = totalProbability

    fun toList(): List<ProbabilityEntry<E>> = entries.toList()

    private fun rebuildCumulativeProbabilities() {
        var cumulative = 0

        for (i in entries.indices) {
            val entry = entries[i]
            cumulative += entry.probability
            entries[i] = entry.copy(cumulative = cumulative)
        }

        totalProbability = cumulative
    }

    data class ProbabilityEntry<T>(
        val value: T,
        val probability: Int,
        val cumulative: Int
    )
}