package com.github.irvinglink.amethystLibKotlin.features.guis.manager

import com.github.irvinglink.amethystLibKotlin.core.PluginContext
import com.github.irvinglink.amethystLibKotlin.features.guis.models.ItemMenu
import com.github.irvinglink.amethystLibKotlin.features.guis.models.MenuClickType
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.concurrent.ConcurrentHashMap

open class Menu(
    override val id: String,
    private val title: String,
    private val rows: Int,
    override val menuConfigFile: File? = null,
    override val enableSlots: Boolean = false
) : IMenu {

    private val chat get() = PluginContext.plugin.chat
    private val _inventory: Inventory = Bukkit.createInventory(this, rows * 9, chat.colorize(title))
    private val items = ConcurrentHashMap<Int, ItemMenu>()

    override fun getInventory(): Inventory = _inventory

    fun addItems(vararg itemMenus: ItemMenu) {
        for (item in itemMenus) {
            if (item.slot < 0 || item.slot >= rows * 9) continue
            items[item.slot] = item
            _inventory.setItem(item.slot, item.itemStack)
        }
    }

    /**
     * Inject a programmatic override action on an already-loaded item.
     * This preserves the YAML defaultAction while adding custom behavior.
     */
    fun onSlot(slot: Int, action: (player: Player, clickType: MenuClickType) -> Unit) {
        items[slot]?.overrideAction = MenuAction { p, c -> action(p, c) }
    }

    fun fillBorder(fillItem: ItemStack) {
        val totalRows = _inventory.size / 9
        for (slot in 0 until _inventory.size) {
            val row = slot / 9
            if (row == 0 || row == totalRows - 1 || slot % 9 == 0 || slot % 9 == 8) {
                _inventory.setItem(slot, fillItem)
            }
        }
    }

    fun fillAll(fillItem: ItemStack) {
        for (slot in 0 until _inventory.size) _inventory.setItem(slot, fillItem)
    }

    fun fillPattern(fillItem: ItemStack, pattern: String) {
        val clean = pattern.replace("\\s+".toRegex(), "")
        for (slot in 0 until _inventory.size) {
            if (matchesPattern(slot, clean)) _inventory.setItem(slot, fillItem)
        }
    }

    private fun matchesPattern(slot: Int, pattern: String): Boolean {
        var result = false
        for (part in pattern.split(",")) {
            if (part.startsWith("!")) {
                val sub = part.substring(1)
                if (sub.contains("-")) {
                    val (start, end) = sub.split("-").map(String::toInt)
                    if (slot in start..end) { result = false; break }
                } else {
                    if (slot == sub.toInt()) { result = false; break }
                }
            } else if (part.contains("-")) {
                val (start, end) = part.split("-").map(String::toInt)
                if (slot in start..end) { result = true; break }
            } else {
                if (slot == part.toInt()) { result = true; break }
            }
        }
        return result
    }

    override fun onClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val clickType = MenuClickType.from(event)
        items[event.rawSlot]?.perform(player, clickType)
    }
}