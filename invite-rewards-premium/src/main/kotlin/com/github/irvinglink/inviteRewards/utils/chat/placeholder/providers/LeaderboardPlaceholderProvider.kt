package com.github.irvinglink.inviteRewards.utils.chat.placeholder.providers

import com.github.irvinglink.inviteRewards.core.PluginContext
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderContext
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderProvider

class LeaderboardPlaceholderProvider : PlaceholderProvider {

    private val plugin get() = PluginContext.plugin
    private val leaderboardManager get() = plugin.leaderboardManager

    override fun register(registry: MutableMap<String, (PlaceholderContext) -> String?>) {

        registry["player_leaderboard_rank"] = { ctx ->
            val uuid = ctx.player?.uniqueId ?: ctx.target?.uniqueId
            uuid?.let {
                val rank = leaderboardManager?.getPlayerRank(it) ?: -1
                if (rank == -1) "100+" else rank.toString()
            } ?: "N/A"
        }

        registry["target_leaderboard_rank"] = { ctx -> ctx.value ?: "N/A" }

        for (i in 1..10) {
            registry["leaderboard_name_$i"] = { _ -> leaderboardManager?.getPlayerAtRank(i)?.name ?: "---" }
            registry["leaderboard_points_$i"] = { _ -> leaderboardManager?.getPlayerAtRank(i)?.points?.toString() ?: "0" }
            registry["leaderboard_total_invites_$i"] = { _ -> leaderboardManager?.getPlayerAtRank(i)?.totalInvites?.toString() ?: "0" }
        }
    }
}