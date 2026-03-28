package com.github.irvinglink.inviteRewards.features.guis.models

import com.github.irvinglink.inviteRewards.features.guis.manager.MenuAction
import com.github.irvinglink.inviteRewards.features.guis.models.requirements.RequirementList
import com.github.irvinglink.inviteRewards.models.action.ExecutableAction
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class ItemMenu(
    val id: String,
    val itemStack: ItemStack,
    val slot: Int,
    val actions: Map<MenuClickType, ExecutableAction> = emptyMap(),
    var overrideAction: MenuAction? = null,
    val requirements: RequirementList? = null,
    val onClick: ((Player, MenuClickType) -> Unit)? = null
) {
    fun perform(player: Player, clickType: MenuClickType) {
        if (requirements?.checkAll(player) == false) return

        (actions[clickType] ?: actions[MenuClickType.DEFAULT])
            ?.execute(player)

        overrideAction?.execute(player, clickType)

        onClick?.invoke(player, clickType)
    }
}