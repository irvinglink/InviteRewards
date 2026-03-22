package com.github.irvinglink.amethystLibKotlin.features.guis.manager

import org.bukkit.Material
import org.bukkit.event.inventory.*
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import java.io.File

interface IMenu : InventoryHolder {

    val id: String
    val menuConfigFile: File?
    val enableSlots: Boolean

    fun onOpen(event: InventoryOpenEvent) {}
    fun onDrag(event: InventoryDragEvent) {}
    fun onClose(event: InventoryCloseEvent) {}
    fun onClick(event: InventoryClickEvent)

    fun preClick(event: InventoryClickEvent) {
        if (!enableSlots) {
            val action = event.action
            val clickedInv = event.clickedInventory ?: return

            val shouldCancel = action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                    || (action.name.contains("PLACE") && !clickedInv.isPlayerInventory())
                    || action == InventoryAction.HOTBAR_SWAP

            if (shouldCancel) {
                event.isCancelled = true
                return
            }

            val item = event.currentItem
            if (item == null || item.type == Material.AIR) return

            if (!clickedInv.isPlayerInventory()) event.isCancelled = true
        }

        onClick(event)
    }

    fun Inventory.isPlayerInventory() = type == InventoryType.PLAYER
}