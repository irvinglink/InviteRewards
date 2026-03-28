package com.github.irvinglink.inviteRewards.database.models

import java.util.UUID

data class PendingReward(
    val receiverUuid: UUID,
    val rewardType: RewardType,
    val inviteTypeId: String,
    val claimerUuid: UUID? = null,
    val inviterUuid: UUID? = null,
    val tier: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class RewardType {
    INVITER_CLAIM,
    CLAIMER_CLAIM,
    INVITER_MILESTONE
}