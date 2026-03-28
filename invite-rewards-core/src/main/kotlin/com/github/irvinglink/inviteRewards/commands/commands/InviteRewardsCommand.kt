package com.github.irvinglink.inviteRewards.commands.commands

import com.github.irvinglink.inviteRewards.commands.builders.BaseCommand
import com.github.irvinglink.inviteRewards.commands.commands.inviteRewardsSubCommands.DataSubCommand
import com.github.irvinglink.inviteRewards.commands.commands.inviteRewardsSubCommands.HelpSubCommand
import com.github.irvinglink.inviteRewards.commands.commands.inviteRewardsSubCommands.ReloadSubCommand
import org.bukkit.command.CommandSender

class InviteRewardsCommand : BaseCommand(commandName = "inviterewards", permission = "inviterewards.command.admin", allowConsole = true) {

    init {
        registerSubCommands(
            HelpSubCommand(),
            ReloadSubCommand(),
            DataSubCommand()
        )
    }

    override fun execute(sender: CommandSender, args: Array<String>) {
        HelpSubCommand().execute(sender, emptyArray())
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            1 -> listOf("help", "reload", "data")
                .filter { it.startsWith(args[0].lowercase()) }
            else -> emptyList()
        }
    }
}