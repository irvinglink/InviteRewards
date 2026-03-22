package com.github.irvinglink.amethystLibKotlin.database.repositories.player

import com.github.irvinglink.amethystLibKotlin.database.models.PlayerData
import com.github.irvinglink.amethystLibKotlin.storage.providers.yaml.YamlStorage
import java.util.UUID

class YamlPlayerRepository(
    private val storage: YamlStorage
) : PlayerRepository {

    private val file = storage.getFile("players")

    override fun save(data: PlayerData) {
        file.set("players.${data.uuid}.name", data.name)
        file.save()
    }

    override fun load(uuid: UUID): PlayerData? {
        val path = "players.$uuid"
        if (!file.contains(path)) return null

        val name = file.getString("$path.name") ?: return null
        return PlayerData(uuid, name)
    }

    override fun delete(uuid: UUID) {
        file.set("players.$uuid", null)
        file.save()
    }

    override fun exists(uuid: UUID): Boolean {
        return file.contains("players.$uuid")
    }
}