package com.github.irvinglink.inviteRewards.database.repositories.claim

import com.github.irvinglink.inviteRewards.database.models.ClaimRecord
import com.github.irvinglink.inviteRewards.storage.providers.yaml.YamlStorage
import java.util.UUID
import java.util.concurrent.CompletableFuture

class YamlClaimRepository(
    private val storage: YamlStorage
) : ClaimRepository {

    private val file = storage.getFile("claims")

    private fun <T> resolved(value: T): CompletableFuture<T> =
        CompletableFuture.completedFuture(value)

    override fun save(record: ClaimRecord): CompletableFuture<Unit> {
        val id = System.nanoTime()
        val path = "claims.${record.claimerUuid}.$id"

        file.set("$path.inviter_uuid", record.inviterUuid.toString())
        file.set("$path.invite_type_id", record.inviteTypeId)
        file.set("$path.invite_code", record.inviteCode)
        file.set("$path.claimed_at", record.claimedAt)
        file.save()

        return resolved(Unit)
    }

    override fun loadClaimsOf(claimerUuid: UUID): CompletableFuture<List<ClaimRecord>> {
        val section = file.getConfigurationSection("claims.$claimerUuid")
            ?: return resolved(emptyList())

        val list = section.getKeys(false).mapNotNull { id ->
            val path = "claims.$claimerUuid.$id"

            val inviterUuid = file.getString("$path.inviter_uuid") ?: return@mapNotNull null
            val inviteTypeId = file.getString("$path.invite_type_id") ?: return@mapNotNull null
            val inviteCode = file.getString("$path.invite_code") ?: return@mapNotNull null

            ClaimRecord(
                claimerUuid = claimerUuid,
                inviterUuid = UUID.fromString(inviterUuid),
                inviteTypeId = inviteTypeId,
                inviteCode = inviteCode,
                claimedAt = file.getLong("$path.claimed_at")
            )
        }.sortedBy { it.claimedAt }

        return resolved(list)
    }

    override fun loadClaimsOf(claimerUuid: UUID, inviteTypeId: String): CompletableFuture<List<ClaimRecord>> {
        return loadClaimsOf(claimerUuid).thenApply { claims ->
            claims.filter { it.inviteTypeId == inviteTypeId }
        }
    }

    override fun deleteAll(claimerUuid: UUID): CompletableFuture<Unit> {
        file.set("claims.$claimerUuid", null)
        file.save()
        return resolved(Unit)
    }
}