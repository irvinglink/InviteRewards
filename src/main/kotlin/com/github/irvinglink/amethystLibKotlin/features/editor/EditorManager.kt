package com.github.irvinglink.amethystLibKotlin.features.editor

import com.github.irvinglink.amethystLibKotlin.core.PluginContext
import com.github.irvinglink.amethystLibKotlin.enums.config.MESSAGES
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import java.util.WeakHashMap

data class EditorSession(val type: EditorType, val obj: Any)

object EditorManager : Listener {

    private val plugin get() = PluginContext.plugin
    private val chat get() = plugin.chat

    private val sessions = WeakHashMap<Player, EditorSession>()

    fun isEditing(player: Player): Boolean = sessions.containsKey(player)

    fun getSession(player: Player): EditorSession? = sessions[player]

    fun startEditing(player: Player, type: EditorType, obj: Any) {
        sessions[player] = EditorSession(type, obj)
        player.closeInventory()

        player.sendTitle(
            chat.format(MESSAGES.EDITOR_EDITING_TITLE),
            chat.format(MESSAGES.EDITOR_EDITING_SUBTITLE),
            10, 40, 10
        )

        player.spigot().sendMessage(
            chat.suggestCommand(
                text       = MESSAGES.EDITOR_SUGGEST_MESSAGE.message,
                suggestion = "exit"
            )
        )
    }

    fun stopEditing(player: Player) {
        sessions.remove(player)
        player.sendTitle(
            chat.format(MESSAGES.EDITOR_DONE_TITLE),
            "",
            10, 40, 10
        )
    }
}