package com.github.irvinglink.amethystLibKotlin.utils.data.items

import com.github.irvinglink.amethystLibKotlin.utils.data.items.providers.HybridItemDataHandler
import org.bukkit.inventory.ItemStack

class ItemDataStorage private constructor(
    private var item: ItemStack,
    private val handler: ItemDataHandler = HybridItemDataHandler()
) {

    companion object {
        fun of(item: ItemStack): ItemDataStorage = ItemDataStorage(item)
    }

    fun setString(key: String, value: String): ItemDataStorage {
        item = handler.setString(item, key, value)
        return this
    }

    fun setInt(key: String, value: Int): ItemDataStorage {
        item = handler.setInt(item, key, value)
        return this
    }

    fun setBoolean(key: String, value: Boolean): ItemDataStorage {
        item = handler.setBoolean(item, key, value)
        return this
    }

    fun getString(key: String): String? = handler.getString(item, key)
    fun getInt(key: String): Int? = handler.getInt(item, key)
    fun getBoolean(key: String): Boolean? = handler.getBoolean(item, key)
    fun has(key: String): Boolean = handler.has(item, key)

    fun remove(key: String): ItemDataStorage {
        item = handler.remove(item, key)
        return this
    }

    fun item(): ItemStack = item
}