package com.github.irvinglink.amethystLibKotlin

import com.github.irvinglink.amethystLibKotlin.core.PluginContext
import com.github.irvinglink.amethystLibKotlin.database.Database
import com.github.irvinglink.amethystLibKotlin.features.guis.loader.DefaultMenusLoader
import com.github.irvinglink.amethystLibKotlin.features.guis.loader.MenuLoader
import com.github.irvinglink.amethystLibKotlin.features.guis.manager.GuiEvents
import com.github.irvinglink.amethystLibKotlin.files.files.ConfigFile
import com.github.irvinglink.amethystLibKotlin.files.files.LangFile
import com.github.irvinglink.amethystLibKotlin.hooks.VaultHook
import com.github.irvinglink.amethystLibKotlin.managers.CommandManager
import com.github.irvinglink.amethystLibKotlin.managers.FileManager
import com.github.irvinglink.amethystLibKotlin.storage.StorageConfig
import com.github.irvinglink.amethystLibKotlin.storage.StorageManager
import com.github.irvinglink.amethystLibKotlin.storage.StorageType
import com.github.irvinglink.amethystLibKotlin.storage.SQLConfig
import com.github.irvinglink.amethystLibKotlin.storage.YamlConfig
import com.github.irvinglink.amethystLibKotlin.utils.chat.Chat
import com.github.irvinglink.amethystLibKotlin.utils.chat.placeholder.PlaceholderEngine
import com.github.irvinglink.amethystLibKotlin.utils.chat.placeholder.PlaceholderRegistry
import com.github.irvinglink.amethystLibKotlin.utils.items.defaults.DefaultItems
import com.github.irvinglink.amethystLibKotlin.utils.version.VersionUtils
import org.bukkit.plugin.java.JavaPlugin

class AmethystLibKotlin : JavaPlugin() {

    lateinit var fileManager: FileManager
        private set

    lateinit var commandManager: CommandManager
        private set

    lateinit var storageManager: StorageManager
        private set

    lateinit var database: Database
        private set

    val configFile: ConfigFile
        get() = fileManager.configFile

    val langFile: LangFile
        get() = fileManager.langFile

    lateinit var placeholderRegistry: PlaceholderRegistry
        private set

    lateinit var placeholderEngine: PlaceholderEngine
        private set

    lateinit var chat: Chat
        private set

    val serverVersion: String = VersionUtils.minecraftVersion

    override fun onLoad() {
        PluginContext.plugin = this
    }

    override fun onEnable() {
        setupManagers()
        setupPlaceholderSystem()
        setupChat()
        setupStorage()
        setupDatabase()
        setupHooks()         // ← before menus so MoneyRequirement works at load time
        setupItems()
        setupMenus()

        logger.info("$name v${description.version} enabled.")
    }

    override fun onDisable() {
        server.onlinePlayers.forEach { it.closeInventory() }

        if (::storageManager.isInitialized) storageManager.shutdown()
        if (::fileManager.isInitialized) fileManager.saveAll()

        logger.info("$name disabled.")
    }

    // ── setup ─────────────────────────────────────────────────────

    private fun setupManagers() {
        fileManager = FileManager(this)
        fileManager.loadAll()
        commandManager = CommandManager()
    }

    private fun setupPlaceholderSystem() {
        placeholderRegistry = PlaceholderRegistry()
        placeholderEngine = PlaceholderEngine(placeholderRegistry)
    }

    private fun setupChat() {
        chat = Chat(this, placeholderEngine)
    }

    private fun setupStorage() {
        storageManager = StorageManager(this, loadStorageConfig())
        storageManager.setup()
    }

    private fun setupDatabase() {
        database = Database(this, storageManager.provider)
    }

    private fun setupHooks() {
        VaultHook.setup()
    }

    private fun setupItems() {
        DefaultItems.register()
    }

    private fun setupMenus() {
        server.pluginManager.registerEvents(GuiEvents(), this)
        DefaultMenusLoader.register()
        MenuLoader.loadAll()  // YAML menus built eagerly after
    }

    private fun loadStorageConfig(): StorageConfig {
        val type = runCatching {
            StorageType.valueOf(configFile.getString("storage.type", "YAML").uppercase())
        }.getOrElse {
            logger.warning("Invalid storage.type in config, defaulting to YAML.")
            StorageType.YAML
        }

        return StorageConfig(
            type = type,
            sql = SQLConfig(
                host     = configFile.getString("storage.sql.host", "localhost"),
                port     = configFile.getInt("storage.sql.port", 3306),
                database = configFile.getString("storage.sql.database", "amethyst"),
                username = configFile.getString("storage.sql.username", "root"),
                password = configFile.getString("storage.sql.password", "")
            ),
            yaml = YamlConfig(
                folder = configFile.getString("storage.yaml.folder", "data")
            )
        )
    }
}