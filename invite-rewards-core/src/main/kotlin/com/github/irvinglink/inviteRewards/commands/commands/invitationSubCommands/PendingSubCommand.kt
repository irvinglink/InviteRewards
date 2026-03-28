package com.github.irvinglink.inviteRewards.commands.commands.invitationSubCommands

import com.github.irvinglink.inviteRewards.commands.builders.SubCommand
import com.github.irvinglink.inviteRewards.enums.config.MESSAGES
import com.github.irvinglink.inviteRewards.features.guis.manager.MenuManager
import com.github.irvinglink.inviteRewards.features.guis.menus.PendingRewardsMenu
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PendingSubCommand : SubCommand {

    override val name: String        get() = "pending"
    override val description: String get() = "View your pending rewards"
    override val syntax: String      get() = "/invitation pending"

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (sender !is Player) {
            plugin.chat.format(MESSAGES.NO_PERMISSION_CONSOLE, color = true)
                .let { sender.sendMessage(it) }
            return
        }

        plugin.database.pendingRewards.loadAll(sender.uniqueId)
            .thenAccept { rewards ->
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    if (rewards.isEmpty()) {
                        plugin.chat.format(sender, MESSAGES.INVITE_NO_PENDING_REWARDS, true)
                            .let { sender.sendMessage(it) }
                        return@Runnable
                    }

                    MenuManager.openMenu(sender, PendingRewardsMenu(sender, rewards, page = 1))
                })
            }
            .exceptionally {
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    sender.sendMessage(plugin.chat.format("&cFailed to load pending rewards&7."))
                })
                it.printStackTrace()
                null
            }
    }
}