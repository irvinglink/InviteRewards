package com.github.irvinglink.inviteRewards.validators

import org.bukkit.configuration.ConfigurationSection

interface InviteValidator {
    fun isTypeAllowed(typeId: String): Boolean
    fun filterTypes(keys: List<String>): List<String>
    fun filterMilestones(typeId: String, section: ConfigurationSection?): List<String>
}