package com.github.irvinglink.inviteRewards.features.editor

import com.github.irvinglink.inviteRewards.InviteRewardsPlugin
import com.github.irvinglink.inviteRewards.core.PluginContext
import org.bukkit.entity.Player

interface IChatEditor<T : Any> {

    val plugin: InviteRewardsPlugin get() = PluginContext.plugin

    fun onType(player: Player, obj: T, type: EditorType, input: String): Boolean
}