package com.github.irvinglink.inviteRewards.models.action

import com.github.irvinglink.inviteRewards.InviteRewardsPlugin
import com.github.irvinglink.inviteRewards.core.PluginContext
import com.github.irvinglink.inviteRewards.utils.chat.Chat
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

interface ActionHandler {

    val plugin : InviteRewardsPlugin
        get() = PluginContext.plugin

    val chat : Chat
        get() = plugin.chat

    fun handle(player: Player, target: OfflinePlayer?, instruction: ActionInstruction): Boolean
}

class LiteActionHandler : ActionHandler {
    override fun handle(player: Player, target: OfflinePlayer?, instruction: ActionInstruction): Boolean {
        return false
    }
}