package com.github.irvinglink.inviteRewards.features.guis.manager

import com.github.irvinglink.inviteRewards.core.PluginContext
import com.github.irvinglink.inviteRewards.features.guis.loader.MenuLoader
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object MenuManager {

    private val plugin get() = PluginContext.plugin
    private val playerMenus = ConcurrentHashMap<UUID, IMenu>()

    fun openMenu(player: Player, menuId: String) {
        val menu = MenuLoader.build(menuId, player) ?: run {
            plugin.logger.warning("Menu '$menuId' not found.")
            return
        }
        openMenu(player, menu)
    }

    fun openMenu(player: Player, menu: IMenu) {
        player.closeInventory()
        playerMenus[player.uniqueId] = menu
        player.openInventory(menu.inventory)
    }

    fun closeMenu(player: Player) {
        player.closeInventory()
        playerMenus.remove(player.uniqueId)
    }

    fun getMenu(player: Player): IMenu? = playerMenus[player.uniqueId]

    fun isViewing(player: Player): Boolean = playerMenus.containsKey(player.uniqueId)

    internal fun set(player: Player, menu: IMenu) {
        playerMenus[player.uniqueId] = menu
    }

    internal fun remove(player: Player) {
        playerMenus.remove(player.uniqueId)
    }
}