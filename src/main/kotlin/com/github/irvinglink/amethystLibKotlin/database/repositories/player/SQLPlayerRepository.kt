package com.github.irvinglink.amethystLibKotlin.database.repositories.player

import com.github.irvinglink.amethystLibKotlin.database.models.PlayerData
import com.github.irvinglink.amethystLibKotlin.storage.providers.sql.SQLStorage
import java.util.UUID

class SQLPlayerRepository(
    private val storage: SQLStorage
) : PlayerRepository {

    init {
        createTable()
    }

    private fun createTable() {
        storage.connection.createStatement().use {
            it.executeUpdate("""
                CREATE TABLE IF NOT EXISTS players (
                    uuid VARCHAR(36) PRIMARY KEY,
                    name TEXT
                )
            """.trimIndent())
        }
    }

    override fun save(data: PlayerData) {
        storage.connection.prepareStatement(
            "REPLACE INTO players (uuid, name) VALUES (?, ?)"
        ).use {
            it.setString(1, data.uuid.toString())
            it.setString(2, data.name)
            it.executeUpdate()
        }
    }

    override fun load(uuid: UUID): PlayerData? {
        storage.connection.prepareStatement(
            "SELECT name FROM players WHERE uuid = ?"
        ).use {
            it.setString(1, uuid.toString())

            val rs = it.executeQuery()
            if (!rs.next()) return null

            return PlayerData(uuid, rs.getString("name"))
        }
    }

    override fun delete(uuid: UUID) {
        storage.connection.prepareStatement(
            "DELETE FROM players WHERE uuid = ?"
        ).use {
            it.setString(1, uuid.toString())
            it.executeUpdate()
        }
    }

    override fun exists(uuid: UUID): Boolean {
        return load(uuid) != null
    }
}