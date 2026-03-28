package com.github.irvinglink.inviteRewards.features.guis.manager

import com.github.irvinglink.inviteRewards.features.guis.models.MenuClickType
import org.bukkit.entity.Player

fun interface MenuAction {
    fun execute(player: Player, clickType: MenuClickType)
}