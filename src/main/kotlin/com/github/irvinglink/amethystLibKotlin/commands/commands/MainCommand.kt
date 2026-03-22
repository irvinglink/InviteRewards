package com.github.irvinglink.amethystLibKotlin.commands.commands

import com.github.irvinglink.amethystLibKotlin.commands.builders.BaseCommand
import com.github.irvinglink.amethystLibKotlin.commands.commands.mainSubcommands.ItemSubCommand
import com.github.irvinglink.amethystLibKotlin.commands.commands.mainSubcommands.MenuSubCommand
import com.github.irvinglink.amethystLibKotlin.commands.commands.mainSubcommands.ReloadSubCommand
import org.bukkit.command.CommandSender

class MainCommand : BaseCommand(commandName = "amethyst", permission = "", true) {

    init {
        registerSubCommands(
            ReloadSubCommand(),
            ItemSubCommand(),
            MenuSubCommand()
        )
    }

    override fun execute(sender: CommandSender, args: Array<String>) {
        sender.sendMessage(chat.format("&eExample Message"))
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        if (args.size == 1) {
            return listOf("help")
        }
        return emptyList()
    }


}