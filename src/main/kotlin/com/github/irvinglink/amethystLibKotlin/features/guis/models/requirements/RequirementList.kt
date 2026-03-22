package com.github.irvinglink.amethystLibKotlin.features.guis.models.requirements

import com.github.irvinglink.amethystLibKotlin.core.PluginContext
import com.github.irvinglink.amethystLibKotlin.features.guis.models.requirements.types.ExpRequirement
import com.github.irvinglink.amethystLibKotlin.features.guis.models.requirements.types.MoneyRequirement
import com.github.irvinglink.amethystLibKotlin.features.guis.models.requirements.types.PermissionRequirement
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

class RequirementList(private val requirements: List<Requirement>) {

    companion object {
        private val log get() = PluginContext.plugin.logger

        fun fromConfig(config: FileConfiguration, path: String): RequirementList {
            val section = config.getConfigurationSection(path)
                ?: return RequirementList(emptyList())

            val requirements = section.getKeys(false).mapNotNull { key ->
                val entryPath = "$path.$key"
                runCatching {
                    when (Requirement.RequirementType.valueOf(key.uppercase())) {

                        Requirement.RequirementType.EXP_REQUIREMENT ->
                            ExpRequirement(
                                amount = config.getInt("$entryPath.level"),
                                withdraw = config.getBoolean("$entryPath.withdraw"),
                                failActions = config.getStringList("$entryPath.deny_actions")
                            )

                        Requirement.RequirementType.MONEY_REQUIREMENT ->
                            MoneyRequirement(
                                amount = config.getInt("$entryPath.money"),
                                withdraw = config.getBoolean("$entryPath.withdraw"),
                                failActions = config.getStringList("$entryPath.deny_actions")
                            )

                        Requirement.RequirementType.PERMISSION_REQUIREMENT ->
                            PermissionRequirement(
                                permission = config.getString("$entryPath.permission") ?: return@mapNotNull null,
                                failActions = config.getStringList("$entryPath.deny_actions")
                            )
                    }
                }.getOrElse {
                    log.warning("Invalid requirement type '$key' at path '$entryPath': ${it.message}")
                    null
                }
            }

            return RequirementList(requirements)
        }
    }

    fun checkAll(player: Player): Boolean =
        requirements.all { it.check(player) }

    fun isEmpty(): Boolean = requirements.isEmpty()
}