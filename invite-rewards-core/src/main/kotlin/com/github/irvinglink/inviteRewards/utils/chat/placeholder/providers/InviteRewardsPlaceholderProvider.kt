package com.github.irvinglink.inviteRewards.utils.chat.placeholder.providers

import com.github.irvinglink.inviteRewards.core.PluginContext
import com.github.irvinglink.inviteRewards.database.models.RewardType
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderContext
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderProvider

class InviteRewardsPlaceholderProvider : PlaceholderProvider {

    private val playerDatabase get() = PluginContext.plugin.database.players
    private val inviteCodeDatabase get() = PluginContext.plugin.database.inviteCodes
    private val pendingRewardDatabase get() = PluginContext.plugin.database.pendingRewards

    override fun register(registry: MutableMap<String, (PlaceholderContext) -> String?>) {

        registry["claimer_name"] = { ctx ->
            ctx.player?.name ?: "Unknown"
        }

        registry["claimer_points"] = { ctx ->
            ctx.player?.let {
                playerDatabase.load(it.uniqueId).get()?.points?.toString()
            } ?: "0"
        }

        registry["claimer_code"] = { ctx ->
            ctx.player?.let {
                val codes = inviteCodeDatabase.loadByOwner(it.uniqueId).get()
                codes.firstOrNull { code -> code.active }?.code ?: codes.firstOrNull()?.code ?: "N/A"
            } ?: "N/A"
        }

        registry["claimer_invited_by"] = { _ ->
            "N/A"
        }

        registry["claimer_has_claimed"] = { ctx ->
            ctx.player?.let {
                pendingRewardDatabase.loadAll(it.uniqueId).get()
                    .none { reward -> reward.rewardType == RewardType.CLAIMER_CLAIM }.toString()
            } ?: "false"
        }

        registry["inviter_name"] = { ctx ->
            ctx.target?.name ?: "Unknown"
        }

        registry["inviter_points"] = { ctx ->
            ctx.target?.let {
                playerDatabase.load(it.uniqueId).get()?.points?.toString()
            } ?: "0"
        }

        registry["inviter_code"] = { ctx ->
            ctx.target?.let {
                val codes = inviteCodeDatabase.loadByOwner(it.uniqueId).get()
                codes.firstOrNull { code -> code.active }?.code ?: codes.firstOrNull()?.code ?: "N/A"
            } ?: "N/A"
        }

        registry["inviter_total_invites"] = { ctx ->
            ctx.target?.let {
                playerDatabase.load(it.uniqueId).get()?.totalInvites?.toString()
            } ?: "0"
        }
    }
}