package com.github.irvinglink.inviteRewards.commands.commands.invitationSubCommands

import com.github.irvinglink.inviteRewards.commands.builders.SubCommand
import com.github.irvinglink.inviteRewards.enums.config.MESSAGES
import com.github.irvinglink.inviteRewards.models.InvitationResult
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ClaimSubCommand : SubCommand {

    override val name: String get() = "claim"
    override val description: String get() = "Claim an invitation reward"
    override val syntax: String get() = "/invitation claim <type> <code>"

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (sender !is Player) {
            plugin.chat.format(MESSAGES.NO_PERMISSION_CONSOLE, color = true)
                .let { sender.sendMessage(it) }
            return
        }

        if (args.size < 2) {
            plugin.chat.format(
                sender,
                null,
                syntax,
                MESSAGES.NOT_ENOUGH_ARGS.message,
                true
            ).let { sender.sendMessage(it) }
            return
        }

        val inviteTypeId = args[0]
        val code = args[1]

        plugin.inviteManager.claim(sender, inviteTypeId, code)
            .thenAccept { result ->
                plugin.server.scheduler.runTask(plugin, Runnable {
                    when (result) {

                        is InvitationResult.Success -> {
                            val inviterOffline = Bukkit.getOfflinePlayer(result.inviter.uuid)
                            val inviterOnline = Bukkit.getPlayer(result.inviter.uuid)

                            plugin.chat.format(
                                sender,
                                inviterOffline,
                                null,
                                MESSAGES.INVITE_CLAIM_SUCCESS_CLAIMER.message,
                                true
                            ).let { sender.sendMessage(it) }

                            inviterOnline?.let { inviter ->
                                plugin.chat.format(
                                    sender,
                                    inviter,
                                    null,
                                    MESSAGES.INVITE_CLAIM_SUCCESS_INVITER.message,
                                    true
                                ).let { inviter.sendMessage(it) }
                            }

                            plugin.rewardManager.executeClaimRewards(
                                inviteTypeId,
                                result.claimer,
                                result.inviter
                            )
                        }

                        is InvitationResult.AlreadyClaimed -> {
                            plugin.chat.format(sender, MESSAGES.INVITE_CLAIM_ALREADY_CLAIMED, true)
                                .let { sender.sendMessage(it) }
                        }

                        InvitationResult.InvalidCode -> {
                            plugin.chat.format(sender, MESSAGES.INVITE_CLAIM_INVALID_CODE, true)
                                .let { sender.sendMessage(it) }
                        }

                        InvitationResult.SelfInvite -> {
                            plugin.chat.format(sender, MESSAGES.INVITE_CLAIM_SELF_INVITE, true)
                                .let { sender.sendMessage(it) }
                        }

                        InvitationResult.NotRegistered -> {
                            plugin.chat.format(sender, MESSAGES.INVITE_CLAIM_NOT_REGISTERED, true)
                                .let { sender.sendMessage(it) }
                        }

                        InvitationResult.SuspiciousActivity -> {
                            plugin.chat.format(sender, MESSAGES.INVITE_CLAIM_SUSPICIOUS, true)
                                .let { sender.sendMessage(it) }
                        }
                    }
                })
            }
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            1 -> getAvailableInviteTypes(sender)
                .filter { it.startsWith(args[0], ignoreCase = true) }
            else -> emptyList()
        }
    }

}