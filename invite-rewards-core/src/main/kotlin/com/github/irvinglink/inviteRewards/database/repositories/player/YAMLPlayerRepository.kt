package com.github.irvinglink.inviteRewards.database.repositories.player

import com.github.irvinglink.inviteRewards.database.models.PlayerData
import com.github.irvinglink.inviteRewards.storage.providers.yaml.YamlStorage
import java.util.UUID
import java.util.concurrent.CompletableFuture

class YamlPlayerRepository(
    private val storage: YamlStorage
) : PlayerRepository {

    private val file = storage.getFile("players")

    private fun <T> resolved(value: T): CompletableFuture<T> =
        CompletableFuture.completedFuture(value)

    override fun save(data: PlayerData): CompletableFuture<Unit> {
        val path = "players.${data.uuid}"
        file.set("$path.name", data.name)
        file.set("$path.points", data.points)
        file.set("$path.total_invites", data.totalInvites)
        file.set("$path.ip_address", data.ipAddress)
        file.set("$path.join_messages_enabled", data.joinMessagesEnabled)
        file.save()
        return resolved(Unit)
    }

    override fun load(uuid: UUID): CompletableFuture<PlayerData?> {
        val path = "players.$uuid"
        if (!file.contains(path)) return resolved(null)

        val data = PlayerData(
            uuid = uuid,
            name = file.getString("$path.name") ?: return resolved(null),
            points = file.getInt("$path.points"),
            totalInvites = file.getInt("$path.total_invites"),
            ipAddress = file.getString("$path.ip_address"),
            joinMessagesEnabled = file.getBoolean("$path.join_messages_enabled", true)
        )
        return resolved(data)
    }

    override fun delete(uuid: UUID): CompletableFuture<Unit> {
        file.set("players.$uuid", null)
        file.save()
        return resolved(Unit)
    }

    override fun exists(uuid: UUID): CompletableFuture<Boolean> =
        resolved(file.contains("players.$uuid"))

    override fun findByIp(ip: String): CompletableFuture<List<PlayerData>> {
        val section = file.getConfigurationSection("players") ?: return resolved(emptyList())
        val result = section.getKeys(false)
            .mapNotNull { load(UUID.fromString(it)).get() }
            .filter { it.ipAddress == ip }
        return resolved(result)
    }

    override fun getTopPlayers(limit: Int): CompletableFuture<List<PlayerData>> {
        val section = file.getConfigurationSection("players") ?: return resolved(emptyList())
        val result = section.getKeys(false)
            .mapNotNull { load(UUID.fromString(it)).get() }
            .sortedByDescending { it.points }
            .take(limit)
        return resolved(result)
    }
}