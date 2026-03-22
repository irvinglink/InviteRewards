package com.github.irvinglink.amethystLibKotlin.utils.data.items

import org.bukkit.inventory.ItemStack

interface ItemDataHandler {

    fun setString(item: ItemStack, key: String, value: String): ItemStack
    fun setInt(item: ItemStack, key: String, value: Int): ItemStack
    fun setBoolean(item: ItemStack, key: String, value: Boolean): ItemStack

    fun getString(item: ItemStack, key: String): String?
    fun getInt(item: ItemStack, key: String): Int?
    fun getBoolean(item: ItemStack, key: String): Boolean?

    fun has(item: ItemStack, key: String): Boolean
    fun remove(item: ItemStack, key: String): ItemStack

}