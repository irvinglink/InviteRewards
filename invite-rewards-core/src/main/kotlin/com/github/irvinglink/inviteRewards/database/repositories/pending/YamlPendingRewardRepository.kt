package com.github.irvinglink.inviteRewards.database.repositories.pending

import com.github.irvinglink.inviteRewards.database.models.PendingReward
import com.github.irvinglink.inviteRewards.database.models.RewardType
import com.github.irvinglink.inviteRewards.storage.providers.yaml.YamlStorage
import java.util.UUID
import java.util.concurrent.CompletableFuture

class YamlPendingRewardRepository(
    private val storage: YamlStorage,
) : PendingRewardRepository {

    private val file = storage.getFile("pending_rewards")

    private fun <T> resolved(value: T): CompletableFuture<T> =
        CompletableFuture.completedFuture(value)

    override fun save(reward: PendingReward): CompletableFuture<Unit> {
        val id = System.nanoTime()
        val path = "pending.${reward.receiverUuid}.$id"

        file.set("$path.reward_type", reward.rewardType.name)
        file.set("$path.invite_type_id", reward.inviteTypeId)
        file.set("$path.claimer_uuid", reward.claimerUuid?.toString())
        file.set("$path.inviter_uuid", reward.inviterUuid?.toString())
        file.set("$path.tier", reward.tier)
        file.set("$path.created_at", reward.createdAt)
        file.save()

        return resolved(Unit)
    }

    override fun loadAll(receiverUuid: UUID): CompletableFuture<List<PendingReward>> {
        val section = file.getConfigurationSection("pending.$receiverUuid")
            ?: return resolved(emptyList())

        val list = section.getKeys(false).mapNotNull { id ->
            val path = "pending.$receiverUuid.$id"

            val rewardType = file.getString("$path.reward_type") ?: return@mapNotNull null
            val inviteTypeId = file.getString("$path.invite_type_id") ?: return@mapNotNull null

            PendingReward(
                receiverUuid = receiverUuid,
                rewardType = RewardType.valueOf(rewardType),
                inviteTypeId = inviteTypeId,
                claimerUuid = file.getString("$path.claimer_uuid")?.let(UUID::fromString),
                inviterUuid = file.getString("$path.inviter_uuid")?.let(UUID::fromString),
                tier = if (file.contains("$path.tier")) file.getInt("$path.tier") else null,
                createdAt = file.getLong("$path.created_at")
            )
        }

        return resolved(list)
    }

    override fun delete(reward: PendingReward): CompletableFuture<Unit> {
        val section = file.getConfigurationSection("pending.${reward.receiverUuid}")
            ?: return resolved(Unit)

        val entryKey = section.getKeys(false).firstOrNull { id ->
            val path = "pending.${reward.receiverUuid}.$id"

            val rewardType = file.getString("$path.reward_type")
            val inviteTypeId = file.getString("$path.invite_type_id")
            val claimerUuid = file.getString("$path.claimer_uuid")
            val inviterUuid = file.getString("$path.inviter_uuid")
            val tier = if (file.contains("$path.tier")) file.getInt("$path.tier") else null
            val createdAt = file.getLong("$path.created_at")

            rewardType == reward.rewardType.name &&
                    inviteTypeId == reward.inviteTypeId &&
                    claimerUuid == reward.claimerUuid?.toString() &&
                    inviterUuid == reward.inviterUuid?.toString() &&
                    tier == reward.tier &&
                    createdAt == reward.createdAt
        } ?: return resolved(Unit)

        file.set("pending.${reward.receiverUuid}.$entryKey", null)
        file.save()
        return resolved(Unit)
    }

    override fun deleteAll(receiverUuid: UUID): CompletableFuture<Unit> {
        file.set("pending.$receiverUuid", null)
        file.save()
        return resolved(Unit)
    }
}