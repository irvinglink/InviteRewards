package com.github.irvinglink.inviteRewards.api.implementation

import com.github.irvinglink.inviteRewards.InviteRewardsPlugin
import com.github.irvinglink.inviteRewards.api.InviteRewardsAPI
import com.github.irvinglink.inviteRewards.database.models.InviteCodeData
import com.github.irvinglink.inviteRewards.database.models.PlayerData
import com.github.irvinglink.inviteRewards.models.InvitationResult
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

class InviteRewardsAPIProvider(private val plugin: InviteRewardsPlugin) : InviteRewardsAPI {

    override fun getPlayerData(uuid: UUID): CompletableFuture<PlayerData?> {
        return plugin.database.players.load(uuid)
    }

    override fun updatePoints(uuid: UUID, points: Int): CompletableFuture<Void?> {
        return getPlayerData(uuid).thenCompose { data ->
            if (data == null) return@thenCompose CompletableFuture.completedFuture<Void?>(null)
            val updatedData = data.copy(points = data.points + points)
            plugin.database.players.save(updatedData).thenApply { null as Void? }
        }
    }

    override fun claimInvite(player: Player, inviteTypeId: String, code: String): CompletableFuture<InvitationResult> {
        return plugin.inviteManager.claim(player, inviteTypeId, code)
    }

    override fun getPlayerCodes(uuid: UUID): CompletableFuture<List<InviteCodeData>> {
        return plugin.inviteManager.getCodes(uuid)
    }

    override fun getActiveCode(uuid: UUID, inviteTypeId: String): CompletableFuture<String?> {
        return plugin.inviteManager.getCode(uuid, inviteTypeId)
    }

    override fun getLeaderboard(limit: Int): List<PlayerData> {
        return plugin.leaderboardManager?.getTopPlayers(limit) ?: emptyList()
    }

    override fun getPlayerRank(uuid: UUID): Int {
        return plugin.leaderboardManager?.getPlayerRank(uuid) ?: -1
    }

    override fun refreshLeaderboard(): CompletableFuture<Void?> {
        return plugin.leaderboardManager?.refreshCache() ?: CompletableFuture.completedFuture(null)
    }

    override fun processPendingRewards(player: Player) {
        plugin.rewardManager.deliverPendingRewards(player)
    }
}