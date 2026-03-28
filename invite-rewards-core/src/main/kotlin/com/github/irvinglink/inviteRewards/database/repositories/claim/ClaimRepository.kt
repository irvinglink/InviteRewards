package com.github.irvinglink.inviteRewards.database.repositories.claim

import com.github.irvinglink.inviteRewards.database.models.ClaimRecord
import java.util.UUID
import java.util.concurrent.CompletableFuture

interface ClaimRepository {

    fun save(record: ClaimRecord): CompletableFuture<Unit>

    fun loadClaimsOf(claimerUuid: UUID): CompletableFuture<List<ClaimRecord>>

    fun loadClaimsOf(claimerUuid: UUID, inviteTypeId: String): CompletableFuture<List<ClaimRecord>>

    fun deleteAll(claimerUuid: UUID): CompletableFuture<Unit>
}