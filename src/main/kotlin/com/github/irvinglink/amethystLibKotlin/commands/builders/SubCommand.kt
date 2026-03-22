package com.github.irvinglink.amethystLibKotlin.commands.builders

import com.github.irvinglink.amethystLibKotlin.AmethystLibKotlin
import com.github.irvinglink.amethystLibKotlin.core.PluginContext
import com.github.irvinglink.amethystLibKotlin.utils.chat.Chat
import org.bukkit.command.CommandSender

interface SubCommand {


    val plugin: AmethystLibKotlin
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

}