package com.github.irvinglink.amethystLibKotlin.database

import com.github.irvinglink.amethystLibKotlin.database.repositories.player.PlayerRepository
import com.github.irvinglink.amethystLibKotlin.database.repositories.player.SQLPlayerRepository
import com.github.irvinglink.amethystLibKotlin.database.repositories.player.YamlPlayerRepository
import com.github.irvinglink.amethystLibKotlin.storage.StorageProvider
import com.github.irvinglink.amethystLibKotlin.storage.providers.sql.SQLStorage
import com.github.irvinglink.amethystLibKotlin.storage.providers.yaml.YamlStorage
import org.bukkit.plugin.java.JavaPlugin

class Database(
    plugin: JavaPlugin,
    provider: StorageProvider
) {

    val players: PlayerRepository

    init {
        when (provider) {
            is SQLStorage -> {
                players = SQLPlayerRepository(provider)
            }

            is YamlStorage -> {
                players = YamlPlayerRepository(provider)
            }

            else -> error("Unsupported storage type")
        }
    }
}