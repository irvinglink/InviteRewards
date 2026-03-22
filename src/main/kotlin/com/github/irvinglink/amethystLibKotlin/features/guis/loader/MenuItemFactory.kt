package com.github.irvinglink.amethystLibKotlin.features.guis.loader

import com.github.irvinglink.amethystLibKotlin.core.PluginContext
import com.github.irvinglink.amethystLibKotlin.features.guis.models.ItemMenu
import com.github.irvinglink.amethystLibKotlin.features.guis.models.MenuClickType
import com.github.irvinglink.amethystLibKotlin.features.guis.models.requirements.RequirementList
import com.github.irvinglink.amethystLibKotlin.models.action.ExecutableAction
import com.github.irvinglink.amethystLibKotlin.utils.items.CustomItems
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag

object MenuItemFactory {

    private val log get() = PluginContext.plugin.logger

    /**
     * @param config   the menu's FileConfiguration
     * @param basePath full path to the item section e.g. "menu.items.close_button"
     * @param itemId   human label used in warnings
     * @param isFill   true when building the fill_item (no slot required)
     */
    fun create(
        config: FileConfiguration,
        basePath: String,
        itemId: String,
        isFill: Boolean = false
    ): ItemMenu? {
        val materialName = config.getString("$basePath.material") ?: run {
            warn(itemId, "missing 'material'"); return null
        }
        val material = runCatching { Material.valueOf(materialName.uppercase()) }.getOrElse {
            warn(itemId, "unknown material '$materialName'"); return null
        }

        val slot = if (isFill) -1 else {
            if (!config.contains("$basePath.slot")) { warn(itemId, "missing 'slot'"); return null }
            config.getInt("$basePath.slot")
        }

        val amount = config.getInt("$basePath.amount", 1).coerceIn(1, 64)

        val itemStack = CustomItems.builder(material, amount).apply {

            config.getString("$basePath.display_name")
                ?.let { name(it) }

            if (config.contains("$basePath.lore"))
                lore(config.getStringList("$basePath.lore"))

            if (config.getBoolean("$basePath.glow", false))
                glow()

            if (config.getBoolean("$basePath.unbreakable", false))
                unbreakable()

            config.getInt("$basePath.custom_model_data", -1)
                .takeIf { it >= 0 }
                ?.let { customModelData(it) }

            if (config.contains("$basePath.flags"))
                config.getStringList("$basePath.flags")
                    .mapNotNull { runCatching { ItemFlag.valueOf(it.uppercase()) }.getOrNull() }
                    .forEach { flags(it) }

            if (config.contains("$basePath.enchantments"))
                config.getStringList("$basePath.enchantments").forEach { entry ->
                    val parts = entry.split(":")
                    if (parts.size < 2) return@forEach
                    val enc = runCatching {
                        @Suppress("DEPRECATION")
                        Enchantment.getByName(parts[0].uppercase())
                    }.getOrNull() ?: return@forEach
                    val level = parts[1].toIntOrNull() ?: return@forEach
                    enchant(enc, level)
                }

        }.build()

        // --- actions per click type ---
        // YAML structure:
        //   actions:
        //     default: ["[message] Hi"]
        //     left:    ["[console] give %player% diamond 1"]
        val actions = mutableMapOf<MenuClickType, ExecutableAction>()

        if (config.contains("$basePath.actions")) {
            for (clickType in MenuClickType.entries) {
                val key  = clickType.name.lowercase()          // "left", "default" …
                val path = "$basePath.actions.$key"
                if (config.contains(path)) {
                    actions[clickType] = ExecutableAction.fromInstructions(
                        config.getStringList(path)
                    )
                }
            }
        }

        // Legacy: flat click_actions list → DEFAULT
        if (actions.isEmpty() && config.contains("$basePath.click_actions")) {
            actions[MenuClickType.DEFAULT] = ExecutableAction.fromInstructions(
                config.getStringList("$basePath.click_actions")
            )
        }

        // --- REQUIREMENTS ---
        val requirements = if (config.contains("$basePath.requirements"))
            RequirementList.fromConfig(config, "$basePath.requirements")
        else null

        return ItemMenu(
            id           = itemId,
            itemStack    = itemStack,
            slot         = slot,
            actions      = actions,
            requirements = requirements
        )
    }

    private fun warn(itemId: String, reason: String) {
        PluginContext.plugin.logger.warning("Skipping item '$itemId': $reason")
    }
}