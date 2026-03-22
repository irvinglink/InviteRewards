package com.github.irvinglink.amethystLibKotlin.features.guis.models.requirements.types

import com.github.irvinglink.amethystLibKotlin.features.guis.models.requirements.Requirement
import com.github.irvinglink.amethystLibKotlin.models.action.ExecutableAction
import org.bukkit.entity.Player

class PermissionRequirement(
    val permission: String,
    private val failActions: List<String>
) : Requirement(Requirement.RequirementType.PERMISSION_REQUIREMENT) {

    private val onFail by lazy { ExecutableAction.fromInstructions(failActions) }

    override fun check(player: Player): Boolean {
        if (!player.hasPermission(permission) && !player.isOp) {
            onFail.execute(player)
            return false
        }
        return true
    }
}