package com.github.irvinglink.inviteRewards.features.editor.handlers

import com.github.irvinglink.inviteRewards.features.editor.EditorType
import com.github.irvinglink.inviteRewards.features.editor.IChatEditor
import org.bukkit.entity.Player

class TestHandler : IChatEditor<Any> {

    override fun onType(player: Player, obj: Any, type: EditorType, input: String): Boolean {

        return false
    }
}