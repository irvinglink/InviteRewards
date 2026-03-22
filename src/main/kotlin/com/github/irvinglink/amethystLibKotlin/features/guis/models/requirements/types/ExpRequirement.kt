package com.github.irvinglink.amethystLibKotlin.features.guis.models.requirements.types

import com.github.irvinglink.amethystLibKotlin.features.guis.models.requirements.Requirement
import com.github.irvinglink.amethystLibKotlin.models.action.ExecutableAction
import org.bukkit.entity.Player

class ExpRequirement(
    val amount: Int,
    val withdraw: Boolean,
    private val failActions: List<String>
) : Requirement(RequirementType.EXP_REQUIREMENT) {

    private val onFail by lazy { ExecutableAction.fromInstructions(failActions) }

    override fun check(player: Player): Boolean {
        if (player.level < amount) {
            onFail.execute(player)
            return false
        }
        if (withdraw) player.level -= amount
        return true
    }
}