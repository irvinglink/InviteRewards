package com.github.irvinglink.amethystLibKotlin.utils.items

import com.github.irvinglink.amethystLibKotlin.core.PluginContext
import com.github.irvinglink.amethystLibKotlin.utils.chat.placeholder.PlaceholderContext
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.lang.reflect.Field
import java.util.Base64
import java.util.UUID

object CustomHead : ItemCreator() {

    private val plugin
        get() = PluginContext.plugin

    private val chat
        get() = plugin.chat

    private var profileField: Field? = null

    fun builder(itemStack: ItemStack): Builder {
        return Builder(itemStack)
    }

    fun builder(material: Material = Material.PLAYER_HEAD, amount: Int = 1): Builder {
        return Builder(material, amount)
    }

    fun getPlayerHead(
        playerName: String,
        displayName: String? = "&e$playerName",
        context: PlaceholderContext = PlaceholderContext()
    ): ItemStack {
        val skull = ItemStack(Material.PLAYER_HEAD)
        val meta = skull.itemMeta as? SkullMeta
            ?: error("PLAYER_HEAD does not provide SkullMeta")

        meta.owningPlayer = Bukkit.getOfflinePlayer(playerName)
        meta.setDisplayName(chat.format(displayName, context, true))

        skull.itemMeta = meta
        return skull
    }

    fun getCustomHead(
        name: String?,
        urlOrTexture: String,
        amount: Int = 1,
        context: PlaceholderContext = PlaceholderContext()
    ): ItemStack {
        val skull = ItemStack(Material.PLAYER_HEAD, amount)
        val meta = skull.itemMeta as? SkullMeta
            ?: error("PLAYER_HEAD does not provide SkullMeta")

        if (urlOrTexture.length < 16) {
            meta.owningPlayer = Bukkit.getOfflinePlayer(urlOrTexture)
            meta.setDisplayName(chat.format(name, context, true))
            skull.itemMeta = meta
            return skull
        }

        applyTexture(meta, urlOrTexture)
        meta.setDisplayName(chat.format(name, context, true))
        skull.itemMeta = meta

        return skull
    }

    private fun applyTexture(meta: SkullMeta, textureIdOrUrl: String) {
        val fullUrl = if (textureIdOrUrl.startsWith("http", ignoreCase = true)) {
            textureIdOrUrl
        } else {
            "https://textures.minecraft.net/texture/$textureIdOrUrl"
        }

        val profile = GameProfile(UUID.randomUUID(), null)
        val json = """{"textures":{"SKIN":{"url":"$fullUrl"}}}"""
        val encoded = Base64.getEncoder().encodeToString(json.toByteArray())

        profile.properties.put("textures", Property("textures", encoded))

        try {
            val field = profileField ?: meta.javaClass.getDeclaredField("profile").also {
                it.isAccessible = true
                profileField = it
            }

            field.set(meta, profile)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    enum class Default(
        val id: String,
        val displayName: String,
        val texture: String
    ) {
        EXAMPLE_ITEM(
            id = "example_item",
            displayName = "&6Example Item",
            texture = "c74df1c52d48b81f46ad07dfd54f7cba13412abf6df5a8ebccbdf746a26a75ab"
        );

        fun toItem(amount: Int = 1, context: PlaceholderContext = PlaceholderContext()): ItemStack {
            return getCustomHead(displayName, texture, amount, context)
        }
    }

    class Builder : ItemCreator.Builder<Builder> {

        constructor(material: Material = Material.PLAYER_HEAD, amount: Int = 1) : super(material, amount)
        constructor(itemStack: ItemStack) : super(itemStack)

        fun setOwner(playerName: String): Builder {
            val meta = itemStack.itemMeta as? SkullMeta
                ?: error("Item ${itemStack.type} is not a skull")

            meta.owningPlayer = Bukkit.getOfflinePlayer(playerName)
            itemStack.itemMeta = meta
            return this
        }

        fun setTexture(textureIdOrUrl: String): Builder {
            val meta = itemStack.itemMeta as? SkullMeta
                ?: error("Item ${itemStack.type} is not a skull")

            applyTexture(meta, textureIdOrUrl)
            itemStack.itemMeta = meta
            return this
        }

        fun setHeadName(
            displayName: String?,
            context: PlaceholderContext = PlaceholderContext()
        ): Builder {
            val meta = itemStack.itemMeta as? SkullMeta
                ?: error("Item ${itemStack.type} is not a skull")

            meta.setDisplayName(chat.format(displayName, context, true))
            itemStack.itemMeta = meta
            return this
        }
    }
}