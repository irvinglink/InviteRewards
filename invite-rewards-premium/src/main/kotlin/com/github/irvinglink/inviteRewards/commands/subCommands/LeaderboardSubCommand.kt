package com.github.irvinglink.inviteRewards.commands.subCommands

import com.github.irvinglink.inviteRewards.commands.builders.SubCommand
import com.github.irvinglink.inviteRewards.enums.config.MESSAGES
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderContext
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LeaderboardSubCommand : SubCommand {

    override val name: String get() = "leaderboard"
    override val description: String get() = "Displays the top 10 inviters in chat"
    override val syntax: String get() = "leaderboard"

    override fun execute(sender: CommandSender, args: Array<String>) {
        val topPlayers = plugin.leaderboardManager?.getTopPlayers(10) ?: emptyList()

        if (topPlayers.isEmpty()) {
            sender.sendMessage(chat.format(MESSAGES.LEADERBOARD_EMPTY, sender))
            return
        }

        sender.sendMessage(chat.format(MESSAGES.LEADERBOARD_HEADER, sender))

        topPlayers.forEachIndexed { index, data ->
            val rank = (index + 1).toString()
            val offlinePlayer = Bukkit.getOfflinePlayer(data.uuid)

            val context = PlaceholderContext(player = sender as? Player, target = offlinePlayer, value = rank)
            val entryMessage = chat.format(MESSAGES.LEADERBOARD_ENTRY, context)

            sender.sendMessage(entryMessage)
        }

        if (sender is Player) sender.sendMessage(chat.format(MESSAGES.LEADERBOARD_PLAYER_RANK, sender))

    }
}