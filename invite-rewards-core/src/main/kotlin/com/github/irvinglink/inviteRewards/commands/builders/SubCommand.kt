package com.github.irvinglink.inviteRewards.commands.builders

import com.github.irvinglink.inviteRewards.InviteRewardsPlugin
import com.github.irvinglink.inviteRewards.core.PluginContext
import com.github.irvinglink.inviteRewards.utils.chat.Chat
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

interface SubCommand {


    val plugin: InviteRewardsPlugin
        get() = PluginContext.plugin

    val chat: Chat
        get() = plugin.chat

    val name: String
    val description: String
    val syntax: String
    val permission: String?
        get() = null
    val allowConsole: Boolean
        get() = true
    val aliases: List<String>
        get() = emptyList()

    fun execute(sender: CommandSender, args: Array<String>)

    fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return emptyList()
    }

    fun getAvailableInviteTypes(sender: CommandSender): List<String> {
        val section = plugin.configFile.getConfigurationSection("invite-types") ?: return emptyList()

        return section.getKeys(false).filter { typeId ->
            val typeSection = plugin.configFile.getConfigurationSection("invite-types.$typeId") ?: return@filter false

            if (!typeSection.getBoolean("enabled", true)) return@filter false

            if (sender !is Player) return@filter true

            val requiresPermission = typeSection.getBoolean("settings.requires-permission-to-own", false)
            val ownerPermission = typeSection.getString("settings.owner-permission", "")

            if (!requiresPermission) return@filter true
            if (ownerPermission.isNullOrBlank()) return@filter true

            sender.hasPermission(ownerPermission)
        }
    }

}