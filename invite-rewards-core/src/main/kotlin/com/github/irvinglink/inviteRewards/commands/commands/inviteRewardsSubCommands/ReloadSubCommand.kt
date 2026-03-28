package com.github.irvinglink.inviteRewards.commands.commands.inviteRewardsSubCommands

import com.github.irvinglink.inviteRewards.commands.builders.SubCommand
import com.github.irvinglink.inviteRewards.enums.config.MESSAGES
import com.github.irvinglink.inviteRewards.features.guis.loader.MenuLoader
import com.github.irvinglink.inviteRewards.files.files.ConfigFile
import com.github.irvinglink.inviteRewards.files.files.LangFile
import com.github.irvinglink.inviteRewards.files.files.PendingRewardsMenuFile
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderContext
import org.bukkit.command.CommandSender

class ReloadSubCommand : SubCommand {

    override val name: String = "reload"
    override val description: String = "Reload plugin files and menus"
    override val syntax: String = "/amethyst reload <config|lang|menus|menu <id>|all>"
    override val permission: String = "inviterewards.command.admin"
    override val allowConsole: Boolean = true

    override fun execute(sender: CommandSender, args: Array<String>) {

        if (args.isEmpty()) {
            reloadAll(sender)
            return
        }

        when (args[0].lowercase()) {

            "config" -> {
                val configFile: ConfigFile = plugin.fileManager.configFile
                configFile.reload()
                sender.sendMessage(chat.format(null, null, configFile.fileName, MESSAGES.RELOAD_FILE))
            }

            "lang" -> {
                val langFile: LangFile = plugin.fileManager.langFile
                langFile.reload()
                sender.sendMessage(chat.format(null, null, langFile.fileName, MESSAGES.RELOAD_FILE))
            }

            "menu" -> {
                val pendingRewardsMenuFile: PendingRewardsMenuFile = plugin.fileManager.pendingRewardsMenuFile
                pendingRewardsMenuFile.reload()
                sender.sendMessage(chat.format(MESSAGES.RELOAD_MENUS))
            }

            "all" -> reloadAll(sender)

            else -> {
                sender.sendMessage(
                    chat.format(MESSAGES.WRONG_USAGE, PlaceholderContext(null, null, syntax))
                )
            }
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            1 -> listOf("config", "lang", "menu", "all")
                .filter { it.startsWith(args[0].lowercase()) }
            2 if args[0].lowercase() == "menu" -> MenuLoader.getLoadedIds()
                .filter { it.startsWith(args[1].lowercase()) }
            else -> emptyList()
        }
    }

    private fun reloadAll(sender: CommandSender) {
        plugin.fileManager.reloadAllAndUpdate()
        sender.sendMessage(chat.format(MESSAGES.RELOAD_FILES))
    }
}