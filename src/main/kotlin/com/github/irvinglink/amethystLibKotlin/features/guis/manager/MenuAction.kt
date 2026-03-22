package com.github.irvinglink.amethystLibKotlin.features.guis.manager

import com.github.irvinglink.amethystLibKotlin.features.guis.models.MenuClickType
import org.bukkit.entity.Player

fun interface MenuAction {
    fun execute(player: Player, clickType: MenuClickType)
}