package com.github.irvinglink.inviteRewards.utils.items

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

interface CustomItem {
    val id: String
    val builder: () -> ItemStack
}

object CustomItems {

    private val registry = mutableMapOf<String, () -> ItemStack>()
    private val cache = mutableMapOf<String, ItemStack>()

    fun register(item: CustomItem) {
        registry[item.id] = item.builder
        cache.remove(item.id)
    }

    fun register(vararg items: CustomItem) = items.forEach { register(it) }

    fun get(item: CustomItem): ItemStack {
        return cache.getOrPut(item.id) {
            registry[item.id]?.invoke()
                ?: error("CustomItem '${item.id}' is not registered.")
        }.clone()
    }

    fun reload() = cache.clear()

    fun builder(material: Material, amount: Int = 1): ItemCreator.Builder<*> = SimpleBuilder(material, amount)
    fun builder(itemStack: ItemStack): ItemCreator.Builder<*> = SimpleBuilder(itemStack)

    private class SimpleBuilder : ItemCreator.Builder<SimpleBuilder> {
        constructor(material: Material, amount: Int = 1) : super(material, amount)
        constructor(itemStack: ItemStack) : super(itemStack)
    }
}