package com.github.irvinglink.inviteRewards.database.repositories.invitecode

import com.github.irvinglink.inviteRewards.database.models.InviteCodeData
import java.util.UUID
import java.util.concurrent.CompletableFuture

interface InviteCodeRepository {

    fun save(data: InviteCodeData): CompletableFuture<Unit>

    fun load(code: String, inviteTypeId: String): CompletableFuture<InviteCodeData?>

    fun loadByOwner(ownerUuid: UUID): CompletableFuture<List<InviteCodeData>>

    fun loadByOwnerAndType(ownerUuid: UUID, inviteTypeId: String): CompletableFuture<List<InviteCodeData>>

    fun delete(code: String, inviteTypeId: String): CompletableFuture<Unit>

    fun exists(code: String, inviteTypeId: String): CompletableFuture<Boolean>
}