package com.github.irvinglink.inviteRewards.listeners

import com.github.irvinglink.inviteRewards.core.PluginContext
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import java.util.concurrent.CompletableFuture

class PlayerLoginListener : Listener {

    private val plugin = PluginContext.plugin

    @EventHandler(priority = EventPriority.NORMAL)
    fun onLogin(event: PlayerLoginEvent) {
        val ip = event.address.hostAddress
        plugin.database.players.load(event.player.uniqueId)
            .thenCompose { data ->
                if (data == null) return@thenCompose CompletableFuture.completedFuture(Unit)
                if (data.ipAddress == ip) return@thenCompose CompletableFuture.completedFuture(Unit)
                plugin.database.players.save(data.copy(ipAddress = ip))
            }
    }
}