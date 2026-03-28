package com.github.irvinglink.inviteRewards.commands.commands

import com.github.irvinglink.inviteRewards.commands.builders.BaseCommand
import com.github.irvinglink.inviteRewards.commands.commands.invitationSubCommands.ClaimSubCommand
import com.github.irvinglink.inviteRewards.commands.commands.invitationSubCommands.CodeSubCommand
import com.github.irvinglink.inviteRewards.commands.commands.invitationSubCommands.PendingSubCommand
import com.github.irvinglink.inviteRewards.enums.config.MESSAGES
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class InvitationRewardsCommand : BaseCommand("invitation", "inviterewards.command.invitation", false) {

    init {
        registerSubCommands(
            CodeSubCommand(),
            ClaimSubCommand(),
            PendingSubCommand()
        )
    }

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (sender !is Player) {
            sender.sendMessage(plugin.chat.format(MESSAGES.NO_PERMISSION_CONSOLE.message))
            return
        }
        sendHelp(sender)
    }

    private fun sendHelp(sender: CommandSender) {
        val prefix = MESSAGES.PREFIX.message
        sender.sendMessage(plugin.chat.format("$prefix &7--- &eInvitation Commands &7---"))
        getSubCommands().forEach { sub ->
            sender.sendMessage(plugin.chat.format(" &e${sub.syntax} &8— &7${sub.description}"))
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        if (args.size == 1) return listOf("code", "claim")
        return emptyList()
    }
}