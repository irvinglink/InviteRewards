package com.github.irvinglink.inviteRewards.utils.data.items.providers

import com.github.irvinglink.inviteRewards.InviteRewardsPlugin
import com.github.irvinglink.inviteRewards.core.PluginContext
import com.github.irvinglink.inviteRewards.utils.data.items.ItemDataHandler
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class PdcItemDataHandler : ItemDataHandler {

    val plugin: InviteRewardsPlugin = PluginContext.plugin

    private fun namespacedKey(key: String): NamespacedKey {
        return NamespacedKey(plugin, key.lowercase())
    }

    override fun setString(item: ItemStack, key: String, value: String): ItemStack {
        val meta = item.itemMeta ?: return item
        meta.persistentDataContainer.set(namespacedKey(key), PersistentDataType.STRING, value)
        item.itemMeta = meta
        return item
    }

    override fun setInt(item: ItemStack, key: String, value: Int): ItemStack {
        val meta = item.itemMeta ?: return item
        meta.persistentDataContainer.set(namespacedKey(key), PersistentDataType.INTEGER, value)
        item.itemMeta = meta
        return item
    }

    override fun setBoolean(item: ItemStack, key: String, value: Boolean): ItemStack {
        val meta = item.itemMeta ?: return item
        meta.persistentDataContainer.set(
            namespacedKey(key),
            PersistentDataType.BYTE,
            if (value) 1 else 0
        )
        item.itemMeta = meta
        return item
    }

    override fun getString(item: ItemStack, key: String): String? {
        val meta = item.itemMeta ?: return null
        return meta.persistentDataContainer.get(namespacedKey(key), PersistentDataType.STRING)
    }

    override fun getInt(item: ItemStack, key: String): Int? {
        val meta = item.itemMeta ?: return null
        return meta.persistentDataContainer.get(namespacedKey(key), PersistentDataType.INTEGER)
    }

    override fun getBoolean(item: ItemStack, key: String): Boolean? {
        val meta = item.itemMeta ?: return null
        val raw = meta.persistentDataContainer.get(namespacedKey(key), PersistentDataType.BYTE) ?: return null
        return raw.toInt() == 1
    }

    override fun has(item: ItemStack, key: String): Boolean {
        val container = item.itemMeta?.persistentDataContainer ?: return false

        return container.has(namespacedKey(key), PersistentDataType.STRING) ||
                container.has(namespacedKey(key), PersistentDataType.INTEGER) ||
                container.has(namespacedKey(key), PersistentDataType.BYTE)
    }

    override fun remove(item: ItemStack, key: String): ItemStack {
        val meta = item.itemMeta ?: return item
        meta.persistentDataContainer.remove(namespacedKey(key))
        item.itemMeta = meta
        return item
    }
}