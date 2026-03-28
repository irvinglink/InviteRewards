package com.github.irvinglink.inviteRewards.database.repositories.claim

import com.github.irvinglink.inviteRewards.database.models.ClaimRecord
import com.github.irvinglink.inviteRewards.storage.providers.sql.SQLStorage
import java.sql.ResultSet
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class SQLClaimRepository(
    private val storage: SQLStorage
) : ClaimRepository {

    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "InviteRewards-SQL-Thread").also { it.isDaemon = true }
    }

    private fun <T> async(block: () -> T): CompletableFuture<T> =
        CompletableFuture.supplyAsync({ block() }, executor)

    init { createTable() }

    private fun createTable() {
        storage.connection.createStatement().use {
            it.executeUpdate("""
                CREATE TABLE IF NOT EXISTS claims (
                    id              INT          AUTO_INCREMENT PRIMARY KEY,
                    claimer_uuid    VARCHAR(36)  NOT NULL,
                    inviter_uuid    VARCHAR(36)  NOT NULL,
                    invite_type_id  VARCHAR(64)  NOT NULL,
                    invite_code     VARCHAR(64)  NOT NULL,
                    claimed_at      BIGINT       NOT NULL
                )
            """.trimIndent())
        }
    }

    override fun save(record: ClaimRecord): CompletableFuture<Unit> = async {
        storage.connection.prepareStatement("""
            INSERT INTO claims
                (claimer_uuid, inviter_uuid, invite_type_id, invite_code, claimed_at)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()).use {
            it.setString(1, record.claimerUuid.toString())
            it.setString(2, record.inviterUuid.toString())
            it.setString(3, record.inviteTypeId)
            it.setString(4, record.inviteCode)
            it.setLong(5, record.claimedAt)
            it.executeUpdate()
        }
    }

    override fun loadClaimsOf(claimerUuid: UUID): CompletableFuture<List<ClaimRecord>> = async {
        storage.connection.prepareStatement("""
            SELECT * FROM claims
            WHERE claimer_uuid = ?
            ORDER BY claimed_at ASC
        """.trimIndent()).use {
            it.setString(1, claimerUuid.toString())

            val rs = it.executeQuery()
            val list = mutableListOf<ClaimRecord>()
            while (rs.next()) {
                list.add(rs.toClaimRecord())
            }
            list
        }
    }

    override fun loadClaimsOf(claimerUuid: UUID, inviteTypeId: String): CompletableFuture<List<ClaimRecord>> = async {
        storage.connection.prepareStatement("""
            SELECT * FROM claims
            WHERE claimer_uuid = ? AND invite_type_id = ?
            ORDER BY claimed_at ASC
        """.trimIndent()).use {
            it.setString(1, claimerUuid.toString())
            it.setString(2, inviteTypeId)

            val rs = it.executeQuery()
            val list = mutableListOf<ClaimRecord>()
            while (rs.next()) {
                list.add(rs.toClaimRecord())
            }
            list
        }
    }

    override fun deleteAll(claimerUuid: UUID): CompletableFuture<Unit> = async {
        storage.connection.prepareStatement("""
            DELETE FROM claims
            WHERE claimer_uuid = ?
        """.trimIndent()).use {
            it.setString(1, claimerUuid.toString())
            it.executeUpdate()
        }
    }

    fun shutdown() = executor.shutdown()

    private fun ResultSet.toClaimRecord() = ClaimRecord(
        claimerUuid = UUID.fromString(getString("claimer_uuid")),
        inviterUuid = UUID.fromString(getString("inviter_uuid")),
        inviteTypeId = getString("invite_type_id"),
        inviteCode = getString("invite_code"),
        claimedAt = getLong("claimed_at")
    )
}