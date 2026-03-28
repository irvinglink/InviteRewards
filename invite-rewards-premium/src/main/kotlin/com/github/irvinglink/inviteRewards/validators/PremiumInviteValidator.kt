package com.github.irvinglink.inviteRewards.validators

import org.bukkit.configuration.ConfigurationSection

class PremiumInviteValidator : InviteValidator {

    override fun isTypeAllowed(typeId: String): Boolean = true

    override fun filterTypes(keys: List<String>): List<String> = keys

    override fun filterMilestones(typeId: String, section: ConfigurationSection?): List<String> {
        return section?.getKeys(false)?.toList() ?: emptyList()
    }
}