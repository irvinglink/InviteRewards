package com.github.irvinglink.inviteRewards.validators

import org.bukkit.configuration.ConfigurationSection

class LiteInviteValidator(private val config: ConfigurationSection?) : InviteValidator {

    override fun isTypeAllowed(typeId: String): Boolean {
        return typeId == config?.getKeys(false)?.firstOrNull()
    }

    override fun filterTypes(keys: List<String>): List<String> = keys.take(1)

    override fun filterMilestones(typeId: String, section: ConfigurationSection?): List<String> {
        return section?.getKeys(false)?.toList()?.take(3) ?: emptyList()
    }

}