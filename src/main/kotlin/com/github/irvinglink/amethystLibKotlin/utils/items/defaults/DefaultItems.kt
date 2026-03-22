package com.github.irvinglink.amethystLibKotlin.utils.items.defaults

import com.github.irvinglink.amethystLibKotlin.utils.items.CustomItem
import com.github.irvinglink.amethystLibKotlin.utils.items.CustomItems
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

enum class DefaultItems(
    override val id: String,
    override val builder: () -> ItemStack,
) : CustomItem {

    MAIN_MENU(
        id = "main_menu",
        builder = {
            CustomItems.builder(Material.OAK_DOOR)
                .name("&aMain Menu")
                .lore("&7Click to go back")
                .build()
        }
    ),

    CLOSE_MENU(
        id = "close_menu",
        builder = {
            CustomItems.builder(Material.BARRIER)
                .name("&cClose")
                .lore("&7Click to close this menu")
                .build()
        }
    ),

    FILLER(
        id = "filler",
        builder = {
            CustomItems.builder(Material.GRAY_STAINED_GLASS_PANE)
                .name("&r")
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build()
        }
    );

    companion object {
        fun register() = CustomItems.register(*entries.toTypedArray())
    }
}