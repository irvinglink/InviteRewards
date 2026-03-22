package com.github.irvinglink.amethystLibKotlin.commands.commands.mainSubcommands

import com.github.irvinglink.amethystLibKotlin.commands.builders.SubCommand
import com.github.irvinglink.amethystLibKotlin.enums.config.MESSAGES
import com.github.irvinglink.amethystLibKotlin.features.guis.loader.MenuLoader
import com.github.irvinglink.amethystLibKotlin.features.guis.manager.MenuManager
import com.github.irvinglink.amethystLibKotlin.features.guis.menus.ExampleProgrammaticMenu
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MenuSubCommand : SubCommand {

    override val name: String = "menu"
    override val description: String = "Opens a menu by id"
    override val syntax: String = "/amethyst menu [id]"

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (sender !is Player) {
            sender.sendMessage(chat.format(MESSAGES.NO_PERMISSION_CONSOLE))
            return
        }

        if (args.isEmpty()) {
            MenuManager.openMenu(sender, ExampleProgrammaticMenu(sender))
            return
        }

        if (!MenuLoader.contains(args[0])) {
            sender.sendMessage(chat.format(MESSAGES.MENU_NO_EXISTS))
            return
        }

        MenuManager.openMenu(sender, args[0])
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        if (sender !is Player || args.size != 1) return emptyList()
        return MenuLoader.getLoadedIds().filter { it.startsWith(args[0].lowercase()) }
    }
}