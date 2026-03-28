package com.github.irvinglink.inviteRewards.utils.items

import com.github.irvinglink.inviteRewards.InviteRewardsPlugin
import com.github.irvinglink.inviteRewards.core.PluginContext
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderContext
import com.github.irvinglink.inviteRewards.utils.nbt.NBTEditor
import com.github.irvinglink.inviteRewards.utils.version.VersionUtils
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

abstract class ItemCreator {

    abstract class Builder<B : Builder<B>> {

        protected val plugin: InviteRewardsPlugin
            get() = PluginContext.plugin

        protected val chat
            get() = plugin.chat

        protected var itemStack: ItemStack

        constructor(itemStack: ItemStack) {
            this.itemStack = itemStack.clone()
        }

        constructor(material: Material, amount: Int = 1) {
            require(amount > 0) { "amount must be greater than 0" }
            this.itemStack = ItemStack(material, amount)
        }

        @Suppress("UNCHECKED_CAST")
        protected fun self(): B = this as B

        protected inline fun editMeta(action: (ItemMeta) -> Unit): B {
            val meta = itemStack.itemMeta ?: error("Item ${itemStack.type} does not support item meta")
            action(meta)
            itemStack.itemMeta = meta
            return self()
        }

        fun type(material: Material): B {
            itemStack.type = material
            return self()
        }

        fun amount(amount: Int): B {
            require(amount > 0) { "amount must be greater than 0" }
            itemStack.amount = amount
            return self()
        }

        fun name(
            displayName: String?,
            context: PlaceholderContext = PlaceholderContext(),
            color: Boolean = true
        ): B {
            return editMeta { meta ->
                meta.setDisplayName(chat.format(displayName, context, color))
            }
        }

        fun lore(
            lore: List<String>?,
            context: PlaceholderContext = PlaceholderContext(),
            color: Boolean = true
        ): B {
            return editMeta { meta ->
                meta.lore = lore
                    ?.mapNotNull { chat.format(it, context, color) }
                    ?.takeIf { it.isNotEmpty() }
            }
        }

        fun lore(
            vararg lines: String,
            context: PlaceholderContext = PlaceholderContext(),
            color: Boolean = true
        ): B {
            return lore(lines.toList(), context, color)
        }

        fun addLore(
            line: String,
            context: PlaceholderContext = PlaceholderContext(),
            color: Boolean = true
        ): B {
            return editMeta { meta ->
                val currentLore = meta.lore?.toMutableList() ?: mutableListOf()
                chat.format(line, context, color)?.let(currentLore::add)
                meta.lore = currentLore
            }
        }

        fun clearLore(): B {
            return editMeta { it.lore = null }
        }

        fun customModelData(data: Int): B {
            if (!VersionUtils.isAtLeast(14)) return self()

            return editMeta { meta ->
                meta.setCustomModelData(data)
            }
        }

        fun unbreakable(value: Boolean = true): B {
            return editMeta { meta ->
                meta.isUnbreakable = value
            }
        }

        fun damage(value: Int): B {
            return editMeta { meta ->
                if (meta is Damageable) {
                    meta.damage = value
                }
            }
        }

        fun enchant(
            enchantment: Enchantment,
            level: Int,
            ignoreLevelRestriction: Boolean = true
        ): B {
            itemStack.addUnsafeEnchantment(enchantment, level.takeIf { ignoreLevelRestriction } ?: level)
            return self()
        }

        fun removeEnchant(enchantment: Enchantment): B {
            itemStack.removeEnchantment(enchantment)
            return self()
        }

        fun glow(): B {
            itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1)
            return flags(ItemFlag.HIDE_ENCHANTS)
        }

        fun flags(vararg flags: ItemFlag): B {
            return editMeta { meta ->
                meta.addItemFlags(*flags)
            }
        }

        fun removeFlags(vararg flags: ItemFlag): B {
            return editMeta { meta ->
                meta.removeItemFlags(*flags)
            }
        }

        fun data(key: String, value: String): B {
            return editMeta { meta ->
                meta.persistentDataContainer.set(
                    NamespacedKey(plugin, key),
                    PersistentDataType.STRING,
                    value
                )
            }
        }

        fun data(key: String, value: Int): B {
            return editMeta { meta ->
                meta.persistentDataContainer.set(
                    NamespacedKey(plugin, key),
                    PersistentDataType.INTEGER,
                    value
                )
            }
        }

        fun data(key: String, value: Boolean): B {
            return editMeta { meta ->
                meta.persistentDataContainer.set(
                    NamespacedKey(plugin, key),
                    PersistentDataType.BYTE,
                    if (value) 1 else 0
                )
            }
        }

        fun removeData(key: String): B {
            return editMeta { meta ->
                meta.persistentDataContainer.remove(NamespacedKey(plugin, key))
            }
        }

        fun hasData(key: String): Boolean {
            val meta = itemStack.itemMeta ?: return false
            val namespacedKey = NamespacedKey(plugin, key)

            val container = meta.persistentDataContainer

            return container.has(namespacedKey, PersistentDataType.STRING) ||
                    container.has(namespacedKey, PersistentDataType.INTEGER) ||
                    container.has(namespacedKey, PersistentDataType.BYTE)
        }

        fun hasStringData(key: String): Boolean {
            val meta = itemStack.itemMeta ?: return false
            return meta.persistentDataContainer.has(
                NamespacedKey(plugin, key),
                PersistentDataType.STRING
            )
        }

        fun hasIntData(key: String): Boolean {
            val meta = itemStack.itemMeta ?: return false
            return meta.persistentDataContainer.has(
                NamespacedKey(plugin, key),
                PersistentDataType.INTEGER
            )
        }

        fun hasBooleanData(key: String): Boolean {
            val meta = itemStack.itemMeta ?: return false
            return meta.persistentDataContainer.has(
                NamespacedKey(plugin, key),
                PersistentDataType.BYTE
            )
        }

        fun legacyNbt(key: String, value: String?): B {
            itemStack = NBTEditor.set(itemStack, value, key)
            return self()
        }

        fun legacyNbt(key: String, value: Int): B {
            itemStack = NBTEditor.set(itemStack, value, key)
            return self()
        }

        fun legacyNbt(key: String, value: Boolean): B {
            itemStack = NBTEditor.set(itemStack, value, key)
            return self()
        }

        fun recolorName(color: ChatColor): B {
            val currentName = itemStack.itemMeta?.displayName ?: return self()
            val stripped = ChatColor.stripColor(currentName) ?: currentName
            return name("&${color.char}$stripped")
        }

        fun build(): ItemStack {
            return itemStack.clone()
        }
    }
}