package com.github.irvinglink.amethystLibKotlin.storage.providers.sql

import com.github.irvinglink.amethystLibKotlin.storage.StorageConfig
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class SQLiteStorage(
    plugin: JavaPlugin,
    config: StorageConfig
) : SQLStorage(plugin, config) {

    override fun setup() {
        if (isReady()) return

        Class.forName("org.sqlite.JDBC")

        val file = File(plugin.dataFolder, "database.db")
        if (!file.exists()) file.createNewFile()

        val url = "jdbc:sqlite:${file.absolutePath}"

        connect(url, null, null)
    }
}