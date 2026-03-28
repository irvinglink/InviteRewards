package com.github.irvinglink.inviteRewards.features.editor

import com.github.irvinglink.inviteRewards.core.PluginContext
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

abstract class ChatEditor : Listener {

    private val plugin get() = PluginContext.plugin
    private val chat get() = plugin.chat

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val session = EditorManager.getSession(player) ?: return

        event.recipients.clear()
        event.isCancelled = true

        if (chat.stripColor(event.message).equals("exit", ignoreCase = true)) {
            EditorManager.stopEditing(player)
            return
        }

        plugin.server.scheduler.runTask(plugin, Runnable {
            if (handle(player, session, event.message)) {
                EditorManager.stopEditing(player)
            }
        })
    }

    protected abstract fun handle(player: Player, session: EditorSession, input: String): Boolean
}