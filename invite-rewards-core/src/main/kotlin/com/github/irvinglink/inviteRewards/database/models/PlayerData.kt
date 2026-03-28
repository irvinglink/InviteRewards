package com.github.irvinglink.inviteRewards.database.models

import java.util.UUID

/**
 * Represents a player registered in the InviteRewards system.
 *
 * @param uuid         Unique Minecraft player identifier
 * @param name         Current player username
 * @param inviteCode   Unique code this player shares with others
 * @param points       Points earned from successful invitations
 * @param invitedBy    UUID of the player who invited this player (null if none)
 * @param totalInvites How many players this player has successfully invited
 * @param ipAddress    Last known IP address (used for anti-multiaccount)
 */
data class PlayerData(
    val uuid: UUID,
    val name: String,
    val points: Int = 0,
    val totalInvites: Int = 0,
    val ipAddress: String? = null,
    val joinMessagesEnabled: Boolean = true
)