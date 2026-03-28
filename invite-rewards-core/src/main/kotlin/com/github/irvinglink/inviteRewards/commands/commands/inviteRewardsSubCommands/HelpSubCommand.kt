package com.github.irvinglink.inviteRewards.commands.commands.inviteRewardsSubCommands

import com.github.irvinglink.inviteRewards.commands.builders.SubCommand
import org.bukkit.command.CommandSender

class HelpSubCommand : SubCommand {

    override val name: String = "help"
    override val description: String = "Show plugin help"
    override val syntax: String = "/inviterewards help"
    override val permission: String = "inviterewards.command.admin"
    override val allowConsole: Boolean = true

    override fun execute(sender: CommandSender, args: Array<String>) {
        sender.sendMessage(chat.format("&8&m----------------------------------------"))
        sender.sendMessage(chat.format("&e&lInvite&d&lRewards &7- &fAdmin Help &8| &7by &dirvinglink"))
        sender.sendMessage(chat.format("&e/inviterewards help &7- Show help"))
        sender.sendMessage(chat.format("&e/inviterewards reload <config|lang|menu|all> &7- Reload plugin"))
        sender.sendMessage(chat.format("&e/inviterewards data info <player> &7- View player data"))
        sender.sendMessage(chat.format("&e/inviterewards data reset rewards <player> &7- Clear pending rewards (Player must rejoin)"))
        sender.sendMessage(chat.format("&e/inviterewards data reset player <player> &7- Reset all data (Player must rejoin)"))
        if (plugin.isPremium) {
            sender.sendMessage(chat.format("&e/inviterewards code create <type> <player> <code> &7- Create a manual invite code"))
            sender.sendMessage(chat.format("&e/inviterewards code delete <type> <code> &7- Delete an invite code"))
            sender.sendMessage(chat.format("&e/inviterewards code list <player> &7- List all invite codes of a player"))
        }
        sender.sendMessage(chat.format("&8&m----------------------------------------"))
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return emptyList()
    }
}