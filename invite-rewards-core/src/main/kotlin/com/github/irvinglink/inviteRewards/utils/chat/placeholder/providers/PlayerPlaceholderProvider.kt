package com.github.irvinglink.inviteRewards.utils.chat.placeholder.providers

import com.github.irvinglink.inviteRewards.core.PluginContext
import com.github.irvinglink.inviteRewards.database.models.RewardType
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderContext
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderProvider

class PlayerPlaceholderProvider : PlaceholderProvider {

    private val db get() = PluginContext.plugin.database.players
    private val inviteCodes get() = PluginContext.plugin.database.inviteCodes
    private val pendingRewards get() = PluginContext.plugin.database.pendingRewards

    override fun register(registry: MutableMap<String, (PlaceholderContext) -> String?>) {

        registry["player_name"]          = { ctx -> ctx.player?.name ?: "Unknown" }
        registry["player_points"]        = { ctx -> ctx.player?.let { db.load(it.uniqueId).get()?.points?.toString() } ?: "0" }
        registry["player_code"]          = { ctx ->
            ctx.player?.let {
                val codes = inviteCodes.loadByOwner(it.uniqueId).get()
                codes.firstOrNull { code -> code.active }?.code ?: codes.firstOrNull()?.code ?: "N/A"
            } ?: "N/A"
        }
        registry["player_total_invites"] = { ctx -> ctx.player?.let { db.load(it.uniqueId).get()?.totalInvites?.toString() } ?: "0" }
        registry["player_invited_by"]    = { _ -> "N/A" }
        registry["player_has_claimed"]   = { ctx ->
            ctx.player?.let {
                pendingRewards.loadAll(it.uniqueId).get()
                    .none { reward -> reward.rewardType == RewardType.CLAIMER_CLAIM }
                    .toString()
            } ?: "false"
        }

        registry["target_name"]          = { ctx -> ctx.target?.name ?: "Unknown" }
        registry["target_points"]        = { ctx -> ctx.target?.let { db.load(it.uniqueId).get()?.points?.toString() } ?: "0" }
        registry["target_code"]          = { ctx ->
            ctx.target?.let {
                val codes = inviteCodes.loadByOwner(it.uniqueId).get()
                codes.firstOrNull { code -> code.active }?.code ?: codes.firstOrNull()?.code ?: "N/A"
            } ?: "N/A"
        }
        registry["target_total_invites"] = { ctx -> ctx.target?.let { db.load(it.uniqueId).get()?.totalInvites?.toString() } ?: "0" }

    }
}