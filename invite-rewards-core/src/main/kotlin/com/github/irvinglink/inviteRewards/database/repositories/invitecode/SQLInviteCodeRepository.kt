package com.github.irvinglink.inviteRewards.database.repositories.invitecode

import com.github.irvinglink.inviteRewards.database.models.InviteCodeData
import com.github.irvinglink.inviteRewards.storage.providers.sql.SQLStorage
import java.sql.ResultSet
import java.sql.Types
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class SQLInviteCodeRepository(
    private val storage: SQLStorage
) : InviteCodeRepository {

    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "InviteRewards-SQL-Thread").also { it.isDaemon = true }
    }

    private fun <T> async(block: () -> T): CompletableFuture<T> =
        CompletableFuture.supplyAsync({ block() }, executor)

    init { createTable() }

    private fun createTable() {
        storage.connection.createStatement().use {
            it.executeUpdate("""
                CREATE TABLE IF NOT EXISTS invite_codes (
                    code            VARCHAR(64)  NOT NULL,
                    owner_uuid      VARCHAR(36)  NOT NULL,
                    invite_type_id  VARCHAR(64)  NOT NULL,
                    active          BOOLEAN      NOT NULL DEFAULT TRUE,
                    manual          BOOLEAN      NOT NULL DEFAULT FALSE,
                    created_at      BIGINT       NOT NULL,
                    created_by      VARCHAR(36)  DEFAULT NULL,
                    PRIMARY KEY (code, invite_type_id)
                )
            """.trimIndent())
        }
    }

    override fun save(data: InviteCodeData): CompletableFuture<Unit> = async {
        storage.connection.prepareStatement("""
            REPLACE INTO invite_codes
                (code, owner_uuid, invite_type_id, active, manual, created_at, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()).use {
            it.setString(1, data.code)
            it.setString(2, data.ownerUuid.toString())
            it.setString(3, data.inviteTypeId)
            it.setBoolean(4, data.active)
            it.setBoolean(5, data.manual)
            it.setLong(6, data.createdAt)

            if (data.createdBy != null) it.setString(7, data.createdBy.toString())
            else it.setNull(7, Types.VARCHAR)

            it.executeUpdate()
        }
    }

    override fun load(code: String, inviteTypeId: String): CompletableFuture<InviteCodeData?> = async {
        storage.connection.prepareStatement("""
            SELECT * FROM invite_codes
            WHERE code = ? AND invite_type_id = ?
        """.trimIndent()).use {
            it.setString(1, code)
            it.setString(2, inviteTypeId)

            val rs = it.executeQuery()
            if (!rs.next()) return@async null
            rs.toInviteCodeData()
        }
    }

    override fun loadByOwner(ownerUuid: UUID): CompletableFuture<List<InviteCodeData>> = async {
        storage.connection.prepareStatement("""
            SELECT * FROM invite_codes
            WHERE owner_uuid = ?
            ORDER BY created_at ASC
        """.trimIndent()).use {
            it.setString(1, ownerUuid.toString())

            val rs = it.executeQuery()
            val list = mutableListOf<InviteCodeData>()
            while (rs.next()) {
                list.add(rs.toInviteCodeData())
            }
            list
        }
    }

    override fun loadByOwnerAndType(ownerUuid: UUID, inviteTypeId: String): CompletableFuture<List<InviteCodeData>> = async {
        storage.connection.prepareStatement("""
            SELECT * FROM invite_codes
            WHERE owner_uuid = ? AND invite_type_id = ?
            ORDER BY created_at ASC
        """.trimIndent()).use {
            it.setString(1, ownerUuid.toString())
            it.setString(2, inviteTypeId)

            val rs = it.executeQuery()
            val list = mutableListOf<InviteCodeData>()
            while (rs.next()) {
                list.add(rs.toInviteCodeData())
            }
            list
        }
    }

    override fun delete(code: String, inviteTypeId: String): CompletableFuture<Unit> = async {
        storage.connection.prepareStatement("""
            DELETE FROM invite_codes
            WHERE code = ? AND invite_type_id = ?
        """.trimIndent()).use {
            it.setString(1, code)
            it.setString(2, inviteTypeId)
            it.executeUpdate()
        }
    }

    override fun exists(code: String, inviteTypeId: String): CompletableFuture<Boolean> = async {
        storage.connection.prepareStatement("""
            SELECT 1 FROM invite_codes
            WHERE code = ? AND invite_type_id = ?
        """.trimIndent()).use {
            it.setString(1, code)
            it.setString(2, inviteTypeId)
            it.executeQuery().next()
        }
    }

    fun shutdown() = executor.shutdown()

    private fun ResultSet.toInviteCodeData() = InviteCodeData(
        code = getString("code"),
        ownerUuid = UUID.fromString(getString("owner_uuid")),
        inviteTypeId = getString("invite_type_id"),
        active = getBoolean("active"),
        manual = getBoolean("manual"),
        createdAt = getLong("created_at"),
        createdBy = getString("created_by")?.let(UUID::fromString)
    )
}