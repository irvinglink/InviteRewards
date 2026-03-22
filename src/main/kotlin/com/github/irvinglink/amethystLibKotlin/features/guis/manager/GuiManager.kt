package com.github.irvinglink.amethystLibKotlin.features.guis.manager

import com.github.irvinglink.amethystLibKotlin.core.PluginContext
import org.bukkit.entity.Player
import org.bukkit.event.inventory.*

class GuiManager {

    private val plugin get() = PluginContext.plugin

    fun onOpen(event: InventoryOpenEvent) {
        val menu = event.inventory.holder as? IMenu ?: return
        menu.onOpen(event)
    }

    fun onClick(event: InventoryClickEvent) {
        val menu = event.inventory.holder as? IMenu ?: return
        menu.preClick(event)
    }

    fun onDrag(event: InventoryDragEvent) {
        val menu = event.inventory.holder as? IMenu ?: return
        menu.onDrag(event)
    }

    fun onClose(event: InventoryCloseEvent) {
        val menu = event.inventory.holder as? IMenu ?: return
        menu.onClose(event)
        MenuManager.remove(event.player as? Player ?: return)
    }
}