package com.github.irvinglink.amethystLibKotlin.features.editor.handlers

import com.github.irvinglink.amethystLibKotlin.features.editor.EditorType
import com.github.irvinglink.amethystLibKotlin.features.editor.IChatEditor
import org.bukkit.entity.Player

class TestHandler : IChatEditor<Any> {

    override fun onType(player: Player, obj: Any, type: EditorType, input: String): Boolean {

        return false
    }
}