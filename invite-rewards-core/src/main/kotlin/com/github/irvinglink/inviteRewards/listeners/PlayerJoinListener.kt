package com.github.irvinglink.inviteRewards.listeners

import com.github.irvinglink.inviteRewards.core.PluginContext
import com.github.irvinglink.inviteRewards.models.action.ExecutableAction
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderContext
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener : Listener {

    private val plugin = PluginContext.plugin

    @EventHandler(priority = EventPriority.NORMAL)
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        plugin.inviteManager.loadOrRegister(player).thenAccept { data ->
            if (!data.joinMessagesEnabled) return@thenAccept

            plugin.server.scheduler.runTask(plugin, Runnable {
                val config = plugin.configFile
                val defaultTypeId = "default"

                // 1. Share Code Reminder
                if (config.getBoolean("joining-messages.share-code.enabled", true)) {
                    plugin.inviteManager.getCode(player.uniqueId, defaultTypeId).thenAccept { code ->
                        if (code == null) return@thenAccept
                        plugin.server.scheduler.runTask(plugin, Runnable {
                            val path = "joining-messages.share-code"

                            // Fetching the display name for the specific invite type
                            val typeName = config.getString("invite-types.$defaultTypeId.display-name") ?: defaultTypeId

                            // Manual replacement for context-specific placeholders
                            val rawMessage = config.getString("$path.message")
                                ?.replace("%inviterewards_invite_type_name%", typeName)
                                ?.replace("%inviterewards_player_code%", code) ?: ""

                            val rawHover = config.getString("$path.hover")
                                ?.replace("%inviterewards_invite_type_name%", typeName)
                                ?.replace("%inviterewards_player_code%", code) ?: ""

                            val component = plugin.chat.suggestCommand(
                                text = rawMessage,
                                suggestion = code,
                                hoverText = rawHover,
                                context = PlaceholderContext(player = player)
                            )
                            player.spigot().sendMessage(component)
                        })
                    }
                }

                // 2. No Code Nudge
                if (config.getBoolean("joining-messages.no-code.enabled", true)) {
                    plugin.database.claims.loadClaimsOf(player.uniqueId).thenAccept { claims ->
                        if (claims.isNotEmpty()) return@thenAccept
                        plugin.server.scheduler.runTask(plugin, Runnable {
                            val path = "joining-messages.no-code"
                            val component = plugin.chat.suggestCommand(
                                text = config.getString("$path.message"),
                                suggestion = "/invitation claim ",
                                hoverText = config.getString("$path.hover"),
                                context = PlaceholderContext(player = player)
                            )
                            player.spigot().sendMessage(component)
                        })
                    }
                }

                // 3. Pending Rewards
                plugin.database.pendingRewards.loadAll(player.uniqueId).thenAccept { rewards ->
                    if (rewards.isEmpty() || !config.getBoolean("joining-messages.pending-rewards.enabled", true)) return@thenAccept

                    plugin.server.scheduler.runTask(plugin, Runnable {
                        val path = "joining-messages.pending-rewards"

                        val component = plugin.chat.runCommand(
                            text = config.getString("$path.message"),
                            command = "/invitation pending",
                            hoverText = config.getString("$path.hover"),
                            context = PlaceholderContext(player = player, value = rewards.size.toString())
                        )
                        player.spigot().sendMessage(component)

                        config.getString("$path.sound")?.let { rawSound ->
                            val fullInstruction = if (rawSound.startsWith("[")) rawSound else "[sound] $rawSound"
                            ExecutableAction.fromInstruction(fullInstruction).execute(player)
                        }
                    })
                }
            })
        }
    }
}