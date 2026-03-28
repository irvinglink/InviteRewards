package com.github.irvinglink.inviteRewards.listeners

import com.github.irvinglink.inviteRewards.core.PluginContext
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.CompletableFuture

class PlayerQuitListener : Listener {

    private val plugin = PluginContext.plugin

    @EventHandler(priority = EventPriority.NORMAL)
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        plugin.database.players.load(player.uniqueId)
            .thenCompose { data ->
                if (data == null) return@thenCompose CompletableFuture.completedFuture(Unit)
                plugin.database.players.save(data.copy(name = player.name))
            }
    }
}