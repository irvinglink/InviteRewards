package com.github.irvinglink.inviteRewards.managers.invitation

import com.github.irvinglink.inviteRewards.core.PluginContext
import com.github.irvinglink.inviteRewards.database.models.PendingReward
import com.github.irvinglink.inviteRewards.database.models.PlayerData
import com.github.irvinglink.inviteRewards.database.models.RewardType
import com.github.irvinglink.inviteRewards.models.action.ExecutableAction
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

class RewardManager {

    private val plugin = PluginContext.plugin
    private val config get() = plugin.configFile
    private val db get() = plugin.database.pendingRewards
    private val inviteManager get() = plugin.inviteManager

    fun executeClaimRewards(inviteTypeId: String, claimer: PlayerData, inviter: PlayerData) {
        if (!inviteManager.validator.isTypeAllowed(inviteTypeId)) return

        val claimerOnline = Bukkit.getPlayer(claimer.uuid)
        val inviterOnline = Bukkit.getPlayer(inviter.uuid)

        val claimerOffline = Bukkit.getOfflinePlayer(claimer.uuid)
        val inviterOffline = Bukkit.getOfflinePlayer(inviter.uuid)

        if (claimerOnline != null) {
            runActions("invite-types.$inviteTypeId.claim.claimer-actions", claimerOnline, inviterOffline)
        }

        if (inviterOnline != null) {
            runActions("invite-types.$inviteTypeId.claim.inviter-actions", inviterOnline, claimerOffline)
            checkMilestones(inviteTypeId, inviter, inviterOnline)
        } else {
            savePending(inviter.uuid, RewardType.INVITER_CLAIM, inviteTypeId, claimer.uuid)
            checkMilestones(inviteTypeId, inviter, null)
        }
    }

    fun checkMilestones(inviteTypeId: String, inviter: PlayerData, inviterOnline: Player?) {
        val section = config.getConfigurationSection("invite-types.$inviteTypeId.milestones.tiers") ?: return

        val keysToProcess = inviteManager.validator.filterMilestones(inviteTypeId, section)

        keysToProcess.forEach { key ->
            val tier = key.toIntOrNull() ?: return@forEach
            if (inviter.totalInvites == tier) {
                if (inviterOnline != null) {
                    runActions("invite-types.$inviteTypeId.milestones.tiers.$key.actions", inviterOnline, Bukkit.getOfflinePlayer(inviter.uuid))
                } else {
                    savePending(inviter.uuid, RewardType.INVITER_MILESTONE, inviteTypeId, inviter.uuid, tier)
                }
            }
        }
    }

    fun deliverPendingRewards(player: Player) {
        db.loadAll(player.uniqueId).thenAccept { rewards ->
            if (rewards.isEmpty()) return@thenAccept

            Bukkit.getScheduler().runTask(plugin, Runnable {
                rewards.forEach { reward ->
                    if (!inviteManager.validator.isTypeAllowed(reward.inviteTypeId)) return@forEach

                    val related = reward.claimerUuid?.let(Bukkit::getOfflinePlayer) ?: player

                    when (reward.rewardType) {
                        RewardType.INVITER_CLAIM -> runActions("invite-types.${reward.inviteTypeId}.claim.inviter-actions", player, related)
                        RewardType.CLAIMER_CLAIM -> runActions("invite-types.${reward.inviteTypeId}.claim.claimer-actions", player, related)
                        RewardType.INVITER_MILESTONE -> runActions("invite-types.${reward.inviteTypeId}.milestones.tiers.${reward.tier}.actions", player, related)
                    }
                }
                db.deleteAll(player.uniqueId)
            })
        }
    }

    fun deliverPendingReward(player: Player, reward: PendingReward): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()

        if (!inviteManager.validator.isTypeAllowed(reward.inviteTypeId)) {
            future.complete(null)
            return future
        }

        Bukkit.getScheduler().runTask(plugin, Runnable {
            try {
                val related = reward.claimerUuid?.let(Bukkit::getOfflinePlayer) ?: player

                when (reward.rewardType) {
                    RewardType.INVITER_CLAIM -> runActions("invite-types.${reward.inviteTypeId}.claim.inviter-actions", player, related)
                    RewardType.CLAIMER_CLAIM -> runActions("invite-types.${reward.inviteTypeId}.claim.claimer-actions", player, related)
                    RewardType.INVITER_MILESTONE -> runActions("invite-types.${reward.inviteTypeId}.milestones.tiers.${reward.tier}.actions", player, related)
                }

                db.delete(reward).thenAccept { future.complete(null) }
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        })

        return future
    }

    private fun runActions(path: String, actor: Player, target: OfflinePlayer) {
        val instructions = config.getStringList(path)
        if (instructions.isNotEmpty()) {
            ExecutableAction.fromInstructions(instructions).execute(actor, target)
        }
    }

    private fun savePending(receiver: UUID, type: RewardType, typeId: String, related: UUID, tier: Int? = null) {
        db.save(PendingReward(
            receiverUuid = receiver, rewardType = type, inviteTypeId = typeId,
            claimerUuid = related, inviterUuid = related, tier = tier
        ))
    }
}