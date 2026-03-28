package com.github.irvinglink.inviteRewards.storage

import com.github.irvinglink.inviteRewards.storage.providers.sql.SQLiteStorage
import com.github.irvinglink.inviteRewards.storage.providers.yaml.YamlStorage
import org.bukkit.plugin.java.JavaPlugin

open class StorageManager(
    protected val plugin: JavaPlugin,
    val config: StorageConfig
) {
    lateinit var provider: StorageProvider
        protected set

    open fun setup() {
        provider = when (config.type) {
            StorageType.SQLITE -> SQLiteStorage(plugin, config)
            else -> YamlStorage(plugin, config)
        }
        provider.setup()
    }

    fun shutdown() {
        if (::provider.isInitialized) provider.shutdown()
    }
}