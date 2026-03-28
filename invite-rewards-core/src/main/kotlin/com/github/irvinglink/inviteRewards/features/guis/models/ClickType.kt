package com.github.irvinglink.inviteRewards.features.guis.models

import org.bukkit.event.inventory.InventoryClickEvent

enum class MenuClickType {
    LEFT, RIGHT, SHIFT_LEFT, SHIFT_RIGHT, DROP, DEFAULT;

    companion object {
        fun from(event: InventoryClickEvent): MenuClickType = when {
            event.isLeftClick  && event.isShiftClick  -> SHIFT_LEFT
            event.isRightClick && event.isShiftClick  -> SHIFT_RIGHT
            event.isLeftClick                         -> LEFT
            event.isRightClick                        -> RIGHT
            event.action.name.contains("DROP")        -> DROP
            else                                      -> DEFAULT
        }
    }
}