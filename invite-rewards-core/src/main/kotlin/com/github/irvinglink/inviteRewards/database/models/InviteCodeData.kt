package com.github.irvinglink.inviteRewards.database.models

import java.util.UUID

data class InviteCodeData(
    val code: String,
    val ownerUuid: UUID,
    val inviteTypeId: String,
    val active: Boolean = true,
    val manual: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: UUID? = null
)