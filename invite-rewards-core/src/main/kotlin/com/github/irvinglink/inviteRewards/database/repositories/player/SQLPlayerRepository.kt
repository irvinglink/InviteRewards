package com.github.irvinglink.inviteRewards.database.repositories.player

import com.github.irvinglink.inviteRewards.database.models.PlayerData
import com.github.irvinglink.inviteRewards.storage.providers.sql.SQLStorage
import java.sql.ResultSet
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class SQLPlayerRepository(
    private val storage: SQLStorage
) : PlayerRepository {

    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "InviteRewards-SQL-Thread").also { it.isDaemon = true }
    }

    private fun <T> async(block: () -> T): CompletableFuture<T> =
        CompletableFuture.supplyAsync({ block() }, executor)

    init { createTable() }

    private fun createTable() {
        storage.connection.createStatement().use {
            it.executeUpdate("""
                CREATE TABLE IF NOT EXISTS players (
                    uuid                   VARCHAR(36) PRIMARY KEY,
                    name                   TEXT        NOT NULL,
                    points                 INT         NOT NULL DEFAULT 0,
                    total_invites          INT         NOT NULL DEFAULT 0,
                    ip_address             VARCHAR(45) DEFAULT NULL,
                    join_messages_enabled  BOOLEAN     NOT NULL DEFAULT TRUE
                )
            """.trimIndent())
        }
    }

    override fun save(data: PlayerData): CompletableFuture<Unit> = async {
        storage.connection.prepareStatement("""
            REPLACE INTO players
                (uuid, name, points, total_invites, ip_address, join_messages_enabled)
            VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()).use {
            it.setString(1, data.uuid.toString())
            it.setString(2, data.name)
            it.setInt(3, data.points)
            it.setInt(4, data.totalInvites)
            it.setString(5, data.ipAddress)
            it.setBoolean(6, data.joinMessagesEnabled)
            it.executeUpdate()
        }
    }

    override fun load(uuid: UUID): CompletableFuture<PlayerData?> = async {
        storage.connection.prepareStatement(
            "SELECT * FROM players WHERE uuid = ?"
        ).use {
            it.setString(1, uuid.toString())
            val rs = it.executeQuery()
            if (!rs.next()) return@async null
            rs.toPlayerData()
        }
    }

    override fun delete(uuid: UUID): CompletableFuture<Unit> = async {
        storage.connection.prepareStatement(
            "DELETE FROM players WHERE uuid = ?"
        ).use {
            it.setString(1, uuid.toString())
            it.executeUpdate()
        }
    }

    override fun exists(uuid: UUID): CompletableFuture<Boolean> = async {
        storage.connection.prepareStatement(
            "SELECT 1 FROM players WHERE uuid = ?"
        ).use {
            it.setString(1, uuid.toString())
            it.executeQuery().next()
        }
    }

    override fun findByIp(ip: String): CompletableFuture<List<PlayerData>> = async {
        storage.connection.prepareStatement(
            "SELECT * FROM players WHERE ip_address = ?"
        ).use {
            it.setString(1, ip)
            val rs = it.executeQuery()
            val list = mutableListOf<PlayerData>()
            while (rs.next()) list.add(rs.toPlayerData())
            list
        }
    }

    override fun getTopPlayers(limit: Int): CompletableFuture<List<PlayerData>> = async {
        storage.connection.prepareStatement(
            "SELECT * FROM players ORDER BY points DESC LIMIT ?"
        ).use {
            it.setInt(1, limit)
            val rs = it.executeQuery()
            val list = mutableListOf<PlayerData>()
            while (rs.next()) list.add(rs.toPlayerData())
            list
        }
    }

    fun shutdown() = executor.shutdown()

    private fun ResultSet.toPlayerData() = PlayerData(
        uuid = UUID.fromString(getString("uuid")),
        name = getString("name"),
        points = getInt("points"),
        totalInvites = getInt("total_invites"),
        ipAddress = getString("ip_address"),
        joinMessagesEnabled = getBoolean("join_messages_enabled")
    )
}