package com.github.irvinglink.inviteRewards

import com.github.irvinglink.inviteRewards.api.InviteRewardsAPI
import com.github.irvinglink.inviteRewards.api.implementation.InviteRewardsAPIProvider
import com.github.irvinglink.inviteRewards.core.PluginContext
import com.github.irvinglink.inviteRewards.database.Database
import com.github.irvinglink.inviteRewards.features.guis.loader.DefaultMenusLoader
import com.github.irvinglink.inviteRewards.features.guis.manager.GuiEvents
import com.github.irvinglink.inviteRewards.files.files.ConfigFile
import com.github.irvinglink.inviteRewards.files.files.LangFile
import com.github.irvinglink.inviteRewards.files.files.PendingRewardsMenuFile
import com.github.irvinglink.inviteRewards.hooks.VaultHook
import com.github.irvinglink.inviteRewards.listeners.PlayerJoinListener
import com.github.irvinglink.inviteRewards.listeners.PlayerLoginListener
import com.github.irvinglink.inviteRewards.listeners.PlayerQuitListener
import com.github.irvinglink.inviteRewards.managers.CommandManager
import com.github.irvinglink.inviteRewards.managers.FileManager
import com.github.irvinglink.inviteRewards.managers.invitation.InviteManager
import com.github.irvinglink.inviteRewards.managers.invitation.RewardManager
import com.github.irvinglink.inviteRewards.managers.leaderboard.ILeaderboardManager
import com.github.irvinglink.inviteRewards.storage.*
import com.github.irvinglink.inviteRewards.utils.chat.Chat
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderEngine
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderRegistry
import com.github.irvinglink.inviteRewards.utils.items.defaults.DefaultItems
import com.github.irvinglink.inviteRewards.utils.version.VersionUtils
import org.bukkit.plugin.java.JavaPlugin

abstract class InviteRewardsPlugin : JavaPlugin() {

    abstract val isPremium: Boolean

    lateinit var fileManager: FileManager
        protected set

    lateinit var commandManager: CommandManager
        protected set

    private var _storageManager: StorageManager? = null
    var storageManager: StorageManager
        get() = _storageManager ?: error("StorageManager not initialized")
        set(value) { _storageManager = value }

    lateinit var inviteManager: InviteManager
        protected set

    open var leaderboardManager: ILeaderboardManager? = null

    lateinit var rewardManager: RewardManager
        protected set

    private var _database: Database? = null
    var database: Database
        get() = _database ?: error("Database not initialized")
        set(value) { _database = value }

    val configFile: ConfigFile
        get() = fileManager.configFile

    val langFile: LangFile
        get() = fileManager.langFile

    val pendingRewardsMenuFile: PendingRewardsMenuFile
        get() = fileManager.pendingRewardsMenuFile

    lateinit var placeholderRegistry: PlaceholderRegistry
        protected set

    lateinit var placeholderEngine: PlaceholderEngine
        protected set

    lateinit var chat: Chat
        protected set

    val serverVersion: String = VersionUtils.minecraftVersion

    override fun onLoad() {
        PluginContext.plugin = this
    }

    override fun onEnable() {
        setupManagers()
        setupPlaceholderSystem()
        setupChat()

        setupInviteManager()
        setupRewardManager()

        setupStorage()
        setupDatabase()

        setupPremiumFeatures()

        setupHooks()
        setupItems()
        setupMenus()
        setupListeners()

        InviteRewardsAPI.register(InviteRewardsAPIProvider(this))

        showEnableMessage()
    }

    private fun showEnableMessage() {
        val line = "─".repeat(39)
        val type = if (isPremium) "PREMIUM" else "FREE"
        val v = "v${description.version}"
        val storage = _storageManager?.config?.type?.name ?: "NONE"

        leaderboardManager?.stopUpdateTask()

        logger.info("┌$line┐")
        logger.info("│  InviteRewards ${v.padEnd(12)} [$type] │")
        logger.info("│  Storage : ${storage.padEnd(27)}│")
        logger.info("│  Status  : Enabled ✔                  │")
        logger.info("└$line┘")
    }

    override fun onDisable() {
        server.onlinePlayers.forEach { it.closeInventory() }

        _storageManager?.shutdown()
        if (::fileManager.isInitialized) fileManager.saveAll()

        logger.info("┌─────────────────────────────────────┐")
        logger.info("│  InviteRewards — Disabled ✘         │")
        logger.info("└─────────────────────────────────────┘")
    }

    private fun setupListeners() {
        val pm = server.pluginManager
        pm.registerEvents(PlayerJoinListener(), this)
        pm.registerEvents(PlayerQuitListener(), this)
        pm.registerEvents(PlayerLoginListener(), this)
    }

    private fun setupManagers() {
        fileManager = FileManager(this)
        fileManager.loadAll()
        commandManager = CommandManager()
    }

    private fun setupPlaceholderSystem() {
        this.placeholderRegistry = PlaceholderRegistry()
        this.placeholderEngine = PlaceholderEngine(placeholderRegistry)
    }

    private fun setupChat() {
        this.chat = Chat(this, placeholderEngine)
    }

    protected fun setupStorage() {
        val storageConfig = loadStorageConfig()

        val finalConfig = if (!isPremium && (storageConfig.type == StorageType.MYSQL || storageConfig.type == StorageType.MARIADB)) {
            logger.severe("[!] MySQL/MariaDB is a PREMIUM feature.")
            logger.warning("[!] Defaulting to SQLite/YAML.")
            storageConfig.copy(type = StorageType.SQLITE)
        } else {
            storageConfig
        }

        storageManager = StorageManager(this, finalConfig)
        storageManager.setup()
    }

    protected fun setupDatabase() {
        database = Database(storageManager.provider)
    }

    private fun setupInviteManager() {
        inviteManager = InviteManager()
    }

    private fun setupRewardManager() {
        rewardManager = RewardManager()
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
    }

    protected fun loadStorageConfig(): StorageConfig {
        val typeStr = configFile.getString("storage.type", "YAML").uppercase()
        val type = try { StorageType.valueOf(typeStr) } catch (e: Exception) { StorageType.YAML }

        return StorageConfig(
            type = type,
            sql = SQLConfig(
                host     = configFile.getString("storage.sql.host", "localhost"),
                port     = configFile.getInt("storage.sql.port", 3306),
                database = configFile.getString("storage.sql.database", "invite_rewards"),
                username = configFile.getString("storage.sql.username", "root"),
                password = configFile.getString("storage.sql.password", "")
            ),
            yaml = YamlConfig(
                folder = configFile.getString("storage.yaml.folder", "data")
            )
        )
    }

    open fun setupPremiumFeatures() {}

    open fun onDisablePremiumFeatures() {}
}