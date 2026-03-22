package com.github.irvinglink.amethystLibKotlin.features.guis.models.requirements.types

import com.github.irvinglink.amethystLibKotlin.features.guis.models.requirements.Requirement
import com.github.irvinglink.amethystLibKotlin.hooks.VaultHook
import com.github.irvinglink.amethystLibKotlin.models.action.ExecutableAction
import org.bukkit.entity.Player

class MoneyRequirement(
    val amount: Int,
    val withdraw: Boolean,
    private val failActions: List<String>
) : Requirement(RequirementType.MONEY_REQUIREMENT) {

    private val onFail by lazy { ExecutableAction.fromInstructions(failActions) }

    override fun check(player: Player): Boolean {
        if (!VaultHook.isEnabled) {
            player.sendMessage("§cEconomy is not available.")
            return false
        }

        if (!VaultHook.has(player, amount.toDouble())) {
            onFail.execute(player)
            return false
        }

        if (withdraw) VaultHook.withdraw(player, amount.toDouble())

        return true
    }
}