package com.github.irvinglink.amethystLibKotlin.utils.data.items.providers

import com.github.irvinglink.amethystLibKotlin.utils.data.items.ItemDataHandler
import com.github.irvinglink.amethystLibKotlin.utils.nbt.NBTEditor
import org.bukkit.inventory.ItemStack

class NbtItemDataHandler : ItemDataHandler {

    override fun setString(item: ItemStack, key: String, value: String): ItemStack {
        return NBTEditor.set(item, value, key)
    }

    override fun setInt(item: ItemStack, key: String, value: Int): ItemStack {
        return NBTEditor.set(item, value, key)
    }

    override fun setBoolean(item: ItemStack, key: String, value: Boolean): ItemStack {
        return NBTEditor.set(item, value, key)
    }

    override fun getString(item: ItemStack, key: String): String? {
        return runCatching { NBTEditor.getString(item, key) }.getOrNull()
    }

    override fun getInt(item: ItemStack, key: String): Int? {
        return runCatching { NBTEditor.getInt(item, key) }.getOrNull()
    }

    override fun getBoolean(item: ItemStack, key: String): Boolean? {
        return runCatching { NBTEditor.getBoolean(item, key) }.getOrNull()
    }

    override fun has(item: ItemStack, key: String): Boolean {
        return runCatching { NBTEditor.getValue(item, key) }.getOrNull() != null
    }

    override fun remove(item: ItemStack, key: String): ItemStack {
        return NBTEditor.set(item, null, key)
    }
}