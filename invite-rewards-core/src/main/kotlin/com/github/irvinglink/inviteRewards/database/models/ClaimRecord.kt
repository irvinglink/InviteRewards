package com.github.irvinglink.inviteRewards.database.models

import java.util.UUID

data class ClaimRecord(
    val claimerUuid: UUID,
    val inviterUuid: UUID,
    val inviteTypeId: String,
    val inviteCode: String,
    val claimedAt: Long = System.currentTimeMillis()
)