package com.github.irvinglink.inviteRewards.storage

import com.github.irvinglink.inviteRewards.sql.MySQLStorage
import com.github.irvinglink.inviteRewards.storage.providers.sql.SQLiteStorage
import com.github.irvinglink.inviteRewards.storage.providers.yaml.YamlStorage
import org.bukkit.plugin.java.JavaPlugin

class PremiumStorageManager(
    plugin: JavaPlugin,
    config: StorageConfig,
) : StorageManager(plugin, config) {

    override fun setup() {
        provider = when (config.type) {
            StorageType.MYSQL, StorageType.MARIADB -> MySQLStorage(plugin, config)
            StorageType.SQLITE -> SQLiteStorage(plugin, config)
            StorageType.YAML -> YamlStorage(plugin, config)
        }
        provider.setup()
    }
}