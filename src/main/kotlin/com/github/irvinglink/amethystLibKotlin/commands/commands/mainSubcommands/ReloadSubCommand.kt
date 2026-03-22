package com.github.irvinglink.amethystLibKotlin.commands.commands.mainSubcommands

import com.github.irvinglink.amethystLibKotlin.commands.builders.SubCommand
import com.github.irvinglink.amethystLibKotlin.enums.config.MESSAGES
import com.github.irvinglink.amethystLibKotlin.features.guis.loader.MenuLoader
import com.github.irvinglink.amethystLibKotlin.files.files.ConfigFile
import com.github.irvinglink.amethystLibKotlin.files.files.LangFile
import com.github.irvinglink.amethystLibKotlin.utils.chat.placeholder.PlaceholderContext
import org.bukkit.command.CommandSender

class ReloadSubCommand : SubCommand {

    override val name: String = "reload"
    override val description: String = "Reload plugin files and menus"
    override val syntax: String = "/amethyst reload <config|lang|menus|menu <id>|all>"
    override val permission: String = "amethystlib.example.reload"
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

            "menus" -> {
                MenuLoader.reloadAll()
                sender.sendMessage(chat.format(MESSAGES.RELOAD_MENUS))
            }

            "menu" -> {
                val menuId = args.getOrNull(1)
                if (menuId.isNullOrBlank()) {
                    sender.sendMessage(
                        chat.format(MESSAGES.WRONG_USAGE, PlaceholderContext(null, null, syntax))
                    )
                    return
                }

                if (!MenuLoader.contains(menuId)) {
                    sender.sendMessage(chat.format(MESSAGES.MENU_NO_EXISTS))
                    return
                }

                MenuLoader.reload(menuId)
                sender.sendMessage(chat.format(null, null, menuId, MESSAGES.RELOAD_MENU))
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
        return when {
            args.size == 1 -> listOf("config", "lang", "menus", "menu", "all")
                .filter { it.startsWith(args[0].lowercase()) }

            args.size == 2 && args[0].lowercase() == "menu" ->
                MenuLoader.getLoadedIds()
                    .filter { it.startsWith(args[1].lowercase()) }

            else -> emptyList()
        }
    }

    private fun reloadAll(sender: CommandSender) {
        plugin.fileManager.reloadAllAndUpdate()
        MenuLoader.reloadAll()
        sender.sendMessage(chat.format(MESSAGES.RELOAD_FILES))
    }
}