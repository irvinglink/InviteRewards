package com.github.irvinglink.inviteRewards.features.guis.models.requirements

import org.bukkit.entity.Player

abstract class Requirement(val type: RequirementType) {

    enum class RequirementType {
        EXP_REQUIREMENT,
        PERMISSION_REQUIREMENT,
        MONEY_REQUIREMENT
    }

    abstract fun check(player: Player): Boolean
}