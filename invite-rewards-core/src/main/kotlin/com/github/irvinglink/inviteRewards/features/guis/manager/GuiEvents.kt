package com.github.irvinglink.inviteRewards.features.guis.manager

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.*

class GuiEvents : Listener {

    private fun InventoryEvent.menu(): IMenu? =
        inventory.holder as? IMenu

    @EventHandler
    fun onOpen(event: InventoryOpenEvent) {
        if (event.isCancelled || event.player.isSleeping) return
        event.menu()?.onOpen(event)
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val menu = event.menu() ?: return
        menu.preClick(event)
    }

    @EventHandler
    fun onDrag(event: InventoryDragEvent) {
        if (event.isCancelled) return
        event.menu()?.onDrag(event)
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        if (event.player !is Player) return
        val menu = event.menu() ?: return
        menu.onClose(event)
        MenuManager.remove(event.player as Player)
    }
}