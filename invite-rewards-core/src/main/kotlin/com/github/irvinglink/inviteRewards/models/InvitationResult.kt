package com.github.irvinglink.inviteRewards.models

import com.github.irvinglink.inviteRewards.database.models.PlayerData
import java.util.UUID

sealed class InvitationResult {

    /**
     * The invitation was claimed successfully.
     *
     * @param claimer Updated data of the player who claimed the code
     * @param inviter Updated data of the player who owned the code
     */
    data class Success(
        val claimer: PlayerData,
        val inviter: PlayerData
    ) : InvitationResult()

    /** The code provided does not match any registered player */
    data object InvalidCode : InvitationResult()

    /**
     * The player has already claimed an invitation before.
     *
     * @param inviterUuid UUID of the player they already claimed from
     */
    data class AlreadyClaimed(
        val inviterUuid: UUID
    ) : InvitationResult()

    /** The player tried to claim their own invite code */
    data object SelfInvite : InvitationResult()

    /** The claimer is not registered in the system */
    data object NotRegistered : InvitationResult()

    /** The claim was blocked because claimer and inviter share the same IP */
    data object SuspiciousActivity : InvitationResult()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = !isSuccess

}