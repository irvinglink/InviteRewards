package com.github.irvinglink.inviteRewards.commands.commands.inviteRewardsSubCommands

import com.github.irvinglink.inviteRewards.commands.builders.SubCommand
import com.github.irvinglink.inviteRewards.database.models.PendingReward
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.CompletableFuture

class DataSubCommand : SubCommand {

    override val name: String = "data"
    override val description: String = "Manage stored player data"
    override val syntax: String = "/inviterewards data <info|reset> ..."
    override val permission: String = "inviterewards.command.admin"
    override val allowConsole: Boolean = true

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            sendUsage(sender)
            return
        }

        when (args[0].lowercase()) {
            "info" -> handleInfo(sender, args)
            "reset" -> handleReset(sender, args)
            else -> sendUsage(sender)
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            1 -> listOf("info", "reset").filter { it.startsWith(args[0], ignoreCase = true) }
            2 -> when (args[0].lowercase()) {
                "reset" -> listOf("rewards", "player", "claims").filter { it.startsWith(args[1], ignoreCase = true) }
                "info" -> playerNames(args[1])
                else -> emptyList()
            }
            3 -> when (args[0].lowercase()) {
                "reset" -> playerNames(args[2])
                else -> emptyList()
            }
            else -> emptyList()
        }
    }

    private fun handleReset(sender: CommandSender, args: Array<String>) {
        if (args.size < 3) {
            sender.sendMessage(chat.format("%inviterewards_prefix% &cUsage: &f/inviterewards data reset <rewards|player|claims> <player>"))
            return
        }

        val resetType = args[1].lowercase()
        val target = findPlayer(args[2])

        if (target == null) {
            sender.sendMessage(chat.format(text = "%inviterewards_prefix% &cPlayer not found: &f%inviterewards_value%", value = args[2]))
            return
        }

        when (resetType) {
            "rewards" -> {
                plugin.database.pendingRewards.deleteAll(target.uniqueId).thenAccept {
                    notifySuccess(sender, "Deleted all pending rewards", target)
                }
            }

            "claims" -> {
                plugin.database.claims.deleteAll(target.uniqueId).thenAccept {
                    notifySuccess(sender, "Deleted all claimed codes history", target)
                }
            }

            "player" -> {
                // Perform a full wipe across all repositories
                CompletableFuture.allOf(
                    plugin.database.players.delete(target.uniqueId),
                    plugin.database.pendingRewards.deleteAll(target.uniqueId),
                    plugin.database.claims.deleteAll(target.uniqueId),
                    deletePlayerInviteCodes(target.uniqueId)
                ).thenAccept {
                    notifySuccess(sender, "Reset ALL plugin data (stats, rewards, claims, codes)", target)
                }
            }

            else -> sendUsage(sender)
        }
    }

    /**
     * Helper to delete all invite codes owned by a player.
     */
    private fun deletePlayerInviteCodes(uuid: UUID): CompletableFuture<Void> {
        return plugin.database.inviteCodes.loadByOwner(uuid).thenCompose { codes ->
            val deletions = codes.map { plugin.database.inviteCodes.delete(it.code, it.inviteTypeId) }
            CompletableFuture.allOf(*deletions.toTypedArray())
        }
    }

    private fun notifySuccess(sender: CommandSender, message: String, target: OfflinePlayer) {
        sync {
            sender.sendMessage(
                chat.format(
                    text = "%inviterewards_prefix% &a$message for &f%inviterewards_value%&a.",
                    value = target.name ?: target.uniqueId.toString()
                )
            )
        }
    }

    private fun handleInfo(sender: CommandSender, args: Array<String>) {
        if (args.size < 2) {
            sender.sendMessage(chat.format(text = "%inviterewards_prefix% &cUsage: &f%inviterewards_value%", value = "/inviterewards data info <player>"))
            return
        }

        val target = findPlayer(args[1])
        if (target == null) {
            sender.sendMessage(chat.format(text = "%inviterewards_prefix% &cPlayer not found: &f%inviterewards_value%", value = args[1]))
            return
        }

        plugin.database.players.load(target.uniqueId)
            .thenCombine(plugin.database.pendingRewards.loadAll(target.uniqueId)) { data, rewards -> data to rewards }
            .thenAccept { (data, rewards) ->
                sync {
                    sender.sendMessage(chat.format("&8&m----------------------------------------"))
                    sender.sendMessage(chat.format("&e&lInvite&d&lRewards &7- &fPlayer Data"))
                    sender.sendMessage(chat.format(text = "&7Player: &f%inviterewards_value%", value = target.name ?: target.uniqueId.toString()))
                    sender.sendMessage(chat.format(if (data != null) "&7Data exists: &atrue" else "&7Data exists: &cfalse"))
                    sender.sendMessage(chat.format(text = "&7Pending rewards: &e%inviterewards_value%", value = rewards.size.toString()))

                    if (data != null) {
                        sender.sendMessage(chat.format(text = "&7Points: &f%inviterewards_value%", value = data.points.toString()))
                        sender.sendMessage(chat.format(text = "&7Total invites: &f%inviterewards_value%", value = data.totalInvites.toString()))
                    }

                    if (rewards.isNotEmpty()) {
                        sender.sendMessage(chat.format("&7Pending reward entries:"))
                        rewards.take(10).forEachIndexed { index, reward ->
                            sender.sendMessage(formatPendingRewardLine(index + 1, reward))
                        }
                        if (rewards.size > 10) {
                            sender.sendMessage(chat.format(text = "&7And &f%inviterewards_value% &7more...", value = (rewards.size - 10).toString()))
                        }
                    }
                    sender.sendMessage(chat.format("&8&m----------------------------------------"))
                }
            }
    }

    private fun formatPendingRewardLine(index: Int, reward: PendingReward): String {
        val inviterName = reward.inviterUuid?.let { Bukkit.getOfflinePlayer(it).name } ?: "none"
        val claimerName = reward.claimerUuid?.let { Bukkit.getOfflinePlayer(it).name } ?: "none"
        val tierText = reward.tier?.toString() ?: "none"
        val dateText = formatDate(reward.createdAt)

        return chat.format("&7#$index &8| &7type: &f${reward.rewardType.name} &8| &7inviteType: &f${reward.inviteTypeId} &8| &7claimer: &f$claimerName &8| &7inviter: &f$inviterName &8| &7tier: &f$tierText &8| &7created: &f$dateText")
    }

    private fun formatDate(timestamp: Long): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        format.timeZone = TimeZone.getDefault()
        return format.format(Date(timestamp))
    }

    private fun sendUsage(sender: CommandSender) {
        sender.sendMessage(chat.format("%inviterewards_prefix% &cUsage:"))
        sender.sendMessage(chat.format("&7/inviterewards data info <player>"))
        sender.sendMessage(chat.format("&7/inviterewards data reset rewards <player>"))
        sender.sendMessage(chat.format("&7/inviterewards data reset claims <player>"))
        sender.sendMessage(chat.format("&7/inviterewards data reset player <player>"))
    }

    private fun playerNames(input: String): List<String> = Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(input, ignoreCase = true) }

    private fun findPlayer(nameOrUuid: String): OfflinePlayer? {
        Bukkit.getPlayerExact(nameOrUuid)?.let { return it }
        runCatching { UUID.fromString(nameOrUuid) }.getOrNull()?.let { return Bukkit.getOfflinePlayer(it) }
        val offline = Bukkit.getOfflinePlayer(nameOrUuid)
        return if (offline.name != null || offline.hasPlayedBefore()) offline else null
    }

    private fun sync(action: () -> Unit) {
        if (Bukkit.isPrimaryThread()) action() else plugin.server.scheduler.runTask(plugin, Runnable { action() })
    }
}