package com.github.irvinglink.amethystLibKotlin.features.editor

import com.github.irvinglink.amethystLibKotlin.features.editor.handlers.TestHandler
import org.bukkit.entity.Player

class EditorHandler : ChatEditor() {

    // Registry: map handler id → IChatEditor
    private val handlers: Map<String, IChatEditor<*>> = mapOf(
        "test" to TestHandler()
    )

    override fun handle(player: Player, session: EditorSession, input: String): Boolean {
        return when (session.type) {
            EditorType.EDITING_AN_OBJECT -> {
                @Suppress("UNCHECKED_CAST")
                (handlers["test"] as? IChatEditor<Any>)
                    ?.onType(player, session.obj, session.type, input)
                    ?: false
            }
        }
    }
}