package com.github.irvinglink.inviteRewards.sql

import com.github.irvinglink.inviteRewards.storage.StorageConfig
import com.github.irvinglink.inviteRewards.storage.providers.sql.SQLStorage
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