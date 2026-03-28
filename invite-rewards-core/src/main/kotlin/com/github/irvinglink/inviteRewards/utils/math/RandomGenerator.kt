package com.github.irvinglink.inviteRewards.utils.math

import java.util.concurrent.ThreadLocalRandom

class RandomGenerator {

    fun generateBoolean(chance: Int, bound: Int): Boolean {
        val doubleChance = ThreadLocalRandom.current().nextInt(bound)
        return doubleChance < chance
    }

}