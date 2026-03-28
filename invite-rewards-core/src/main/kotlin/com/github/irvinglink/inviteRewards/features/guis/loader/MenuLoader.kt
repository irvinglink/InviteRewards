package com.github.irvinglink.inviteRewards.features.guis.loader

import com.github.irvinglink.inviteRewards.core.PluginContext
import com.github.irvinglink.inviteRewards.features.guis.manager.IMenu
import com.github.irvinglink.inviteRewards.features.guis.manager.Menu
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object MenuLoader {

    private val plugin get() = PluginContext.plugin
    private val log    get() = plugin.logger
    private const val ROOT   = "menu."

    private sealed interface MenuEntry {
        // Built fresh per player on every open
        data class Programmatic(val builder: (Player) -> IMenu) : MenuEntry
        // Built once at loadAll(), player-independent
        data class Yaml(val instance: IMenu, val file: File) : MenuEntry
    }

    private val registry   = ConcurrentHashMap<String, MenuEntry>()
    private val menuFolder get() = File(plugin.dataFolder, "menus")

    fun register(id: String, builder: (Player) -> IMenu) {
        registry[id] = MenuEntry.Programmatic(builder)
    }

    fun build(menuId: String, player: Player): IMenu? {
        return when (val entry = registry[menuId]) {
            is MenuEntry.Programmatic -> runCatching { entry.builder(player) }
                .getOrElse { log.warning("Failed to build '$menuId': ${it.message}"); null }
            is MenuEntry.Yaml -> entry.instance
            null -> { log.warning("Menu '$menuId' not found."); null }
        }
    }

    fun loadAll() {
        menuFolder.mkdirs()
        val files = menuFolder.listFiles { f -> f.extension == "yml" } ?: return
        files.forEach { file ->
            runCatching { loadFile(file) }
                .onFailure { log.warning("Failed to load '${file.name}': ${it.message}") }
        }
        log.info("Loaded ${registry.size} menu(s).")
    }

    fun reload(menuId: String) {
        val entry = registry[menuId]

        if (entry !is MenuEntry.Yaml) {
            log.warning("'$menuId' is programmatic — cannot reload from file.")
            return
        }

        Bukkit.getOnlinePlayers()
            .filter { it.openInventory.topInventory.holder.let { h -> h is IMenu && h.id == menuId } }
            .forEach { it.closeInventory() }

        registry.remove(menuId)

        runCatching { loadFile(entry.file) }
            .onFailure { log.warning("Failed to reload '$menuId': ${it.message}") }
    }

    fun reloadAll() {
        Bukkit.getOnlinePlayers().forEach { it.closeInventory() }
        // Only remove YAML entries — programmatic survive reload
        registry.entries.removeIf { it.value is MenuEntry.Yaml }
        loadAll()
    }

    fun contains(menuId: String): Boolean = registry.containsKey(menuId)

    fun getLoadedIds(): List<String> = registry.keys.sorted()

    fun isYaml(menuId: String): Boolean = registry[menuId] is MenuEntry.Yaml

    // ── internal ──────────────────────────────────────────────────

    private fun loadFile(file: File) {
        val config = YamlConfiguration.loadConfiguration(file)
        loadMenu(file, config)
    }

    private fun loadMenu(file: File, config: FileConfiguration) {
        if (!config.isConfigurationSection(ROOT.trimEnd('.')))
            error("Missing 'menu' section in '${file.name}'")

        val menuId = config.getString("${ROOT}menu_id")
            ?.takeIf { it.isNotBlank() }
            ?: error("Missing or blank 'menu.menu_id' in '${file.name}'")

        if (registry.containsKey(menuId))
            error("Duplicate menu id '$menuId' from '${file.name}'")

        val title       = config.getString("${ROOT}title") ?: error("Missing 'menu.title'")
        val rows        = config.getInt("${ROOT}rows").coerceIn(1, 6)
        val enableSlots = config.getBoolean("${ROOT}enable_slots", false)

        // Built eagerly — errors surface at startup
        val instance = buildYamlMenu(config, file, menuId, title, rows, enableSlots)
        registry[menuId] = MenuEntry.Yaml(instance = instance, file = file)

        log.info("Loaded YAML menu '$menuId'.")
    }

    private fun buildYamlMenu(
        config: FileConfiguration,
        file: File,
        menuId: String,
        title: String,
        rows: Int,
        enableSlots: Boolean
    ): Menu {
        val menu = Menu(
            id             = menuId,
            title          = title,
            rows           = rows,
            menuConfigFile = file,
            enableSlots    = enableSlots
        )

        if (config.getBoolean("${ROOT}fill_item.enabled", false)) {
            val fillItem = MenuItemFactory
                .create(config, "${ROOT}fill_item.item", "fill_item", isFill = true)
                ?.itemStack ?: run { log.warning("[$menuId] Invalid fill_item, skipping."); null }

            if (fillItem != null) {
                val fillTypeStr = config.getString("${ROOT}fill_item.type", "FULL")!!.uppercase()
                val fillType = runCatching { FillType.valueOf(fillTypeStr) }.getOrElse {
                    log.warning("[$menuId] Unknown fill type '$fillTypeStr', defaulting to FULL")
                    FillType.FULL
                }
                when (fillType) {
                    FillType.FULL    -> menu.fillAll(fillItem)
                    FillType.BORDER  -> menu.fillBorder(fillItem)
                    FillType.PATTERN -> {
                        val pattern = config.getString("${ROOT}fill_item.pattern") ?: run {
                            log.warning("[$menuId] FillType PATTERN requires 'fill_item.pattern'")
                            return menu
                        }
                        menu.fillPattern(fillItem, pattern)
                    }
                }
            }
        }

        config.getConfigurationSection("${ROOT}items")
            ?.getKeys(false)
            ?.forEach { itemId ->
                val item = MenuItemFactory.create(config, "${ROOT}items.$itemId", itemId)
                if (item != null) menu.addItems(item)
                else log.warning("[$menuId] Skipped item '$itemId'.")
            }

        return menu
    }
}