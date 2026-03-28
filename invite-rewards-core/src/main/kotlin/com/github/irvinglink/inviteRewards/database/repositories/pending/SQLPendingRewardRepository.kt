package com.github.irvinglink.inviteRewards.database.repositories.pending

import com.github.irvinglink.inviteRewards.database.models.PendingReward
import com.github.irvinglink.inviteRewards.database.models.RewardType
import com.github.irvinglink.inviteRewards.storage.providers.sql.SQLStorage
import java.sql.Types
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class SQLPendingRewardRepository(
    private val storage: SQLStorage
) : PendingRewardRepository {

    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "InviteRewards-SQL-Thread").also { it.isDaemon = true }
    }

    private fun <T> async(block: () -> T): CompletableFuture<T> =
        CompletableFuture.supplyAsync({ block() }, executor)

    init { createTable() }

    private fun createTable() {
        storage.connection.createStatement().use {
            it.executeUpdate("""
                CREATE TABLE IF NOT EXISTS pending_rewards (
                    id              INT          AUTO_INCREMENT PRIMARY KEY,
                    receiver_uuid   VARCHAR(36)  NOT NULL,
                    reward_type     VARCHAR(32)  NOT NULL,
                    invite_type_id  VARCHAR(64)  NOT NULL,
                    claimer_uuid    VARCHAR(36)  DEFAULT NULL,
                    inviter_uuid    VARCHAR(36)  DEFAULT NULL,
                    tier            INT          DEFAULT NULL,
                    created_at      BIGINT       NOT NULL
                )
            """.trimIndent())
        }
    }

    override fun save(reward: PendingReward): CompletableFuture<Unit> = async {
        storage.connection.prepareStatement("""
            INSERT INTO pending_rewards
                (receiver_uuid, reward_type, invite_type_id, claimer_uuid, inviter_uuid, tier, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()).use {
            it.setString(1, reward.receiverUuid.toString())
            it.setString(2, reward.rewardType.name)
            it.setString(3, reward.inviteTypeId)

            if (reward.claimerUuid != null) it.setString(4, reward.claimerUuid.toString())
            else it.setNull(4, Types.VARCHAR)

            if (reward.inviterUuid != null) it.setString(5, reward.inviterUuid.toString())
            else it.setNull(5, Types.VARCHAR)

            if (reward.tier != null) it.setInt(6, reward.tier)
            else it.setNull(6, Types.INTEGER)

            it.setLong(7, reward.createdAt)
            it.executeUpdate()
        }
    }

    override fun loadAll(receiverUuid: UUID): CompletableFuture<List<PendingReward>> = async {
        storage.connection.prepareStatement(
            "SELECT * FROM pending_rewards WHERE receiver_uuid = ?"
        ).use {
            it.setString(1, receiverUuid.toString())
            val rs = it.executeQuery()
            val list = mutableListOf<PendingReward>()

            while (rs.next()) {
                list.add(
                    PendingReward(
                        receiverUuid = UUID.fromString(rs.getString("receiver_uuid")),
                        rewardType = RewardType.valueOf(rs.getString("reward_type")),
                        inviteTypeId = rs.getString("invite_type_id"),
                        claimerUuid = rs.getString("claimer_uuid")?.let(UUID::fromString),
                        inviterUuid = rs.getString("inviter_uuid")?.let(UUID::fromString),
                        tier = rs.getObject("tier") as? Int,
                        createdAt = rs.getLong("created_at")
                    )
                )
            }

            list
        }
    }

    override fun delete(reward: PendingReward): CompletableFuture<Unit> = async {
        storage.connection.prepareStatement("""
            DELETE FROM pending_rewards
            WHERE receiver_uuid = ?
              AND reward_type = ?
              AND invite_type_id = ?
              AND ((claimer_uuid IS NULL AND ? IS NULL) OR claimer_uuid = ?)
              AND ((inviter_uuid IS NULL AND ? IS NULL) OR inviter_uuid = ?)
              AND ((tier IS NULL AND ? IS NULL) OR tier = ?)
              AND created_at = ?
        """.trimIndent()).use {
            it.setString(1, reward.receiverUuid.toString())
            it.setString(2, reward.rewardType.name)
            it.setString(3, reward.inviteTypeId)

            if (reward.claimerUuid != null) {
                val value = reward.claimerUuid.toString()
                it.setString(4, value)
                it.setString(5, value)
            } else {
                it.setNull(4, Types.VARCHAR)
                it.setNull(5, Types.VARCHAR)
            }

            if (reward.inviterUuid != null) {
                val value = reward.inviterUuid.toString()
                it.setString(6, value)
                it.setString(7, value)
            } else {
                it.setNull(6, Types.VARCHAR)
                it.setNull(7, Types.VARCHAR)
            }

            if (reward.tier != null) {
                it.setInt(8, reward.tier)
                it.setInt(9, reward.tier)
            } else {
                it.setNull(8, Types.INTEGER)
                it.setNull(9, Types.INTEGER)
            }

            it.setLong(10, reward.createdAt)
            it.executeUpdate()
        }
    }

    override fun deleteAll(receiverUuid: UUID): CompletableFuture<Unit> = async {
        storage.connection.prepareStatement(
            "DELETE FROM pending_rewards WHERE receiver_uuid = ?"
        ).use {
            it.setString(1, receiverUuid.toString())
            it.executeUpdate()
        }
    }

    fun shutdown() = executor.shutdown()
}