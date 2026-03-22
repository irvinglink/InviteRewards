package com.github.irvinglink.amethystLibKotlin.storage.providers.sql

import com.github.irvinglink.amethystLibKotlin.storage.StorageConfig
import org.bukkit.plugin.java.JavaPlugin

class MySQLStorage(
    plugin: JavaPlugin,
    config: StorageConfig
) : SQLStorage(plugin, config) {

    override fun setup() {
        if (isReady()) return

        Class.forName("com.mysql.cj.jdbc.Driver")

        val sql = config.sql

        val url = "jdbc:mysql://${sql.host}:${sql.port}/${sql.database}"

        connect(url, sql.username, sql.password)
    }
}