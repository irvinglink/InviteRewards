package com.github.irvinglink.inviteRewards.database.repositories.pending

import com.github.irvinglink.inviteRewards.database.models.PendingReward
import java.util.UUID
import java.util.concurrent.CompletableFuture

interface PendingRewardRepository {

    fun save(reward: PendingReward): CompletableFuture<Unit>

    fun loadAll(receiverUuid: UUID): CompletableFuture<List<PendingReward>>

    fun delete(reward: PendingReward): CompletableFuture<Unit>

    fun deleteAll(receiverUuid: UUID): CompletableFuture<Unit>
}