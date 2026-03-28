package com.github.irvinglink.inviteRewards.commands.commands.invitationSubCommands

import com.github.irvinglink.inviteRewards.commands.builders.SubCommand
import com.github.irvinglink.inviteRewards.enums.config.MESSAGES
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderContext
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ToggleJoinMessageSubCommand : SubCommand {

    override val name: String get() = "toggle"
    override val description: String get() = "Toggle join notification messages on or off"
    override val syntax: String get() = "/invitation toggle"
    override val allowConsole: Boolean get() = false

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (sender !is Player) {
            sender.sendMessage(chat.format(MESSAGES.NO_PERMISSION_CONSOLE.message))
            return
        }

        plugin.inviteManager.loadOrRegister(sender).thenAccept { data ->
            val newState = !data.joinMessagesEnabled
            val updatedData = data.copy(joinMessagesEnabled = newState)

            plugin.database.players.save(updatedData).thenRun {
                plugin.server.scheduler.runTask(plugin, Runnable {

                    val responseEnum = if (newState) MESSAGES.INVITE_NOTIFICATIONS_ENABLED
                    else MESSAGES.INVITE_NOTIFICATIONS_DISABLED

                    val context = PlaceholderContext(player = sender)
                    sender.sendMessage(chat.format(responseEnum, context).orEmpty())
                })
            }
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return emptyList()
    }
}