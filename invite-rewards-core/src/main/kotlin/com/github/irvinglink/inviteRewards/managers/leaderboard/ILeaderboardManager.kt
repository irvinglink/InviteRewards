package com.github.irvinglink.inviteRewards.managers.leaderboard

import com.github.irvinglink.inviteRewards.database.models.PlayerData
import java.util.*
import java.util.concurrent.CompletableFuture

interface ILeaderboardManager {

    fun startUpdateTask()

    fun stopUpdateTask()

    fun refreshCache(): CompletableFuture<Void?>

    fun getTopPlayers(limit: Int): List<PlayerData>

    fun getPlayerRank(uuid: UUID): Int

    fun getPlayerAtRank(rank: Int): PlayerData?
}