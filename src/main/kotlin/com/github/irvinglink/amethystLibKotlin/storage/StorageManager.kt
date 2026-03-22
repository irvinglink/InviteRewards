package com.github.irvinglink.amethystLibKotlin.storage

import com.github.irvinglink.amethystLibKotlin.storage.providers.sql.MySQLStorage
import com.github.irvinglink.amethystLibKotlin.storage.providers.sql.SQLiteStorage
import com.github.irvinglink.amethystLibKotlin.storage.providers.yaml.YamlStorage
import org.bukkit.plugin.java.JavaPlugin

class StorageManager(
    private val plugin: JavaPlugin,
    private val config: StorageConfig
) {

    lateinit var provider: StorageProvider
        private set

    fun setup() {
        provider = when (config.type) {
            StorageType.MYSQL,
            StorageType.MARIADB -> MySQLStorage(plugin, config)

            StorageType.SQLITE -> SQLiteStorage(plugin, config)

            StorageType.YAML -> YamlStorage(plugin, config)
        }

        provider.setup()
    }

    fun shutdown() {
        if (::provider.isInitialized) {
            provider.shutdown()
        }
    }
}