package com.github.irvinglink.inviteRewards.utils.nbt

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class ItemDataManager private constructor(
    private val item: ItemStack,
    private val basePath: Array<out Any> = emptyArray()
) {

    companion object {
        private lateinit var plugin: JavaPlugin

        fun init(plugin: JavaPlugin) {
            this.plugin = plugin
        }

        fun of(item: ItemStack): ItemDataManager = ItemDataManager(item)
    }

    private fun path(vararg extra: Any): Array<out Any> {
        return arrayOf(*basePath, *extra)
    }

    fun has(vararg key: Any): Boolean {
        return getAny(*key) != null
    }

    fun getAny(vararg key: Any): Any? {
        return getPdcString(key.firstOrNull()?.toString() ?: return null)
            ?: runCatching { NBTEditor.getValue(item, *path(*key)) }.getOrNull()
    }

    fun getString(vararg key: Any): String? {
        if (key.size == 1) {
            val pdc = getPdcString(key[0].toString())
            if (pdc != null) return pdc
        }

        return runCatching { NBTEditor.getString(item, *path(*key)) }.getOrNull()
    }

    fun getInt(vararg key: Any): Int? {
        if (key.size == 1) {
            val pdc = getPdcInt(key[0].toString())
            if (pdc != null) return pdc
        }

        return runCatching { NBTEditor.getInt(item, *path(*key)) }.getOrNull()
    }

    fun getDouble(vararg key: Any): Double? {
        return runCatching { NBTEditor.getDouble(item, *path(*key)) }.getOrNull()
    }

    fun getBoolean(vararg key: Any): Boolean? {
        if (key.size == 1) {
            val pdc = getPdcBoolean(key[0].toString())
            if (pdc != null) return pdc
        }

        return runCatching { NBTEditor.getBoolean(item, *path(*key)) }.getOrNull()
    }

    fun isCompound(vararg key: Any): Boolean {
        val value = runCatching { NBTEditor.getValue(item, *path(*key)) }.getOrNull() ?: return false
        return value == NBTEditor.COMPOUND || value::class.java.simpleName.contains("Compound", ignoreCase = true)
    }

    fun compound(vararg key: Any): ItemDataManager? {
        return if (runCatching { NBTEditor.getValue(item, *path(*key)) }.getOrNull() != null) {
            ItemDataManager(item, path(*key))
        } else null
    }

    fun requireCompound(vararg key: Any): ItemDataManager {
        return ItemDataManager(item, path(*key))
    }

    fun keysPreview(vararg key: Any): Any? {
        return runCatching { NBTEditor.getValue(item, *path(*key)) }.getOrNull()
    }

    private fun getPdcString(key: String): String? {
        val meta = item.itemMeta ?: return null
        return meta.persistentDataContainer.get(
            NamespacedKey(plugin, key),
            PersistentDataType.STRING
        )
    }

    private fun getPdcInt(key: String): Int? {
        val meta = item.itemMeta ?: return null
        return meta.persistentDataContainer.get(
            NamespacedKey(plugin, key),
            PersistentDataType.INTEGER
        )
    }

    private fun getPdcBoolean(key: String): Boolean? {
        val meta = item.itemMeta ?: return null
        val raw = meta.persistentDataContainer.get(
            NamespacedKey(plugin, key),
            PersistentDataType.BYTE
        ) ?: return null

        return raw.toInt() == 1
    }
}