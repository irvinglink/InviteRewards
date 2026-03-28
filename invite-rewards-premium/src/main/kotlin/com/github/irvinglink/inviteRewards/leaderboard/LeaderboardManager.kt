package com.github.irvinglink.inviteRewards.leaderboard

import com.github.irvinglink.inviteRewards.core.PluginContext
import com.github.irvinglink.inviteRewards.database.models.PlayerData
import com.github.irvinglink.inviteRewards.managers.leaderboard.ILeaderboardManager
import org.bukkit.scheduler.BukkitTask
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

class LeaderboardManager : ILeaderboardManager {

    private val plugin = PluginContext.plugin
    private val leaderboardCache = AtomicReference<List<PlayerData>>(emptyList())
    private var refreshTask: BukkitTask? = null

    override fun startUpdateTask() {
        val path = "leaderboard.refresh-interval"
        val intervalSeconds = if (plugin.configFile.contains(path)) plugin.configFile.getLong(path) else 600L
        val ticks = intervalSeconds * 20

        refreshTask = plugin.server.scheduler.runTaskTimerAsynchronously(plugin, Runnable {
            refreshCache().join()
        }, 20L, ticks)
    }

    override fun stopUpdateTask() {
        refreshTask?.cancel()
        refreshTask = null
    }

    override fun refreshCache(): CompletableFuture<Void?> {
        val fetchSize = plugin.configFile.getInt("leaderboard.size", 10).coerceAtLeast(100)

        return plugin.database.players.getTopPlayers(fetchSize).thenAccept { players ->
            leaderboardCache.set(players)
        }
    }

    override fun getTopPlayers(limit: Int): List<PlayerData> {
        return leaderboardCache.get().take(limit)
    }

    override fun getPlayerRank(uuid: UUID): Int {
        val players = leaderboardCache.get()
        val index = players.indexOfFirst { it.uuid == uuid }
        return if (index == -1) -1 else index + 1
    }

    override fun getPlayerAtRank(rank: Int): PlayerData? {
        val players = leaderboardCache.get()
        val index = rank - 1
        return if (index in players.indices) players[index] else null
    }
}