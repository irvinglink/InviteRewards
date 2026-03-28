package com.github.irvinglink.inviteRewards.utils.data.items.providers

import com.github.irvinglink.inviteRewards.utils.data.items.ItemDataHandler
import com.github.irvinglink.inviteRewards.utils.version.VersionUtils
import org.bukkit.inventory.ItemStack

class HybridItemDataHandler(
    private val pdcHandler: PdcItemDataHandler = PdcItemDataHandler(),
    private val nbtHandler: NbtItemDataHandler = NbtItemDataHandler()
) : ItemDataHandler {

    private val writer: ItemDataHandler
        get() = if (VersionUtils.isPdcSupported) pdcHandler else nbtHandler

    override fun setString(item: ItemStack, key: String, value: String): ItemStack {
        return writer.setString(item, key, value)
    }

    override fun setInt(item: ItemStack, key: String, value: Int): ItemStack {
        return writer.setInt(item, key, value)
    }

    override fun setBoolean(item: ItemStack, key: String, value: Boolean): ItemStack {
        return writer.setBoolean(item, key, value)
    }

    override fun getString(item: ItemStack, key: String): String? {
        if (VersionUtils.isPdcSupported) {
            return pdcHandler.getString(item, key) ?: nbtHandler.getString(item, key)
        }
        return nbtHandler.getString(item, key)
    }

    override fun getInt(item: ItemStack, key: String): Int? {
        if (VersionUtils.isPdcSupported) {
            return pdcHandler.getInt(item, key) ?: nbtHandler.getInt(item, key)
        }
        return nbtHandler.getInt(item, key)
    }

    override fun getBoolean(item: ItemStack, key: String): Boolean? {
        if (VersionUtils.isPdcSupported) {
            return pdcHandler.getBoolean(item, key) ?: nbtHandler.getBoolean(item, key)
        }
        return nbtHandler.getBoolean(item, key)
    }

    override fun has(item: ItemStack, key: String): Boolean {
        if (VersionUtils.isPdcSupported) {
            return pdcHandler.has(item, key) || nbtHandler.has(item, key)
        }
        return nbtHandler.has(item, key)
    }

    override fun remove(item: ItemStack, key: String): ItemStack {
        var result = item
        if (VersionUtils.isPdcSupported) {
            result = pdcHandler.remove(result, key)
        }
        return nbtHandler.remove(result, key)
    }
}