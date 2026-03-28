package com.github.irvinglink.inviteRewards.api

import com.github.irvinglink.inviteRewards.database.models.InviteCodeData
import com.github.irvinglink.inviteRewards.database.models.PlayerData
import com.github.irvinglink.inviteRewards.models.InvitationResult
import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Global entry point for the InviteRewards API.
 * This interface provides a decoupled way for third-party plugins to interact
 * with invitations, rankings, and player data.
 *
 * Access the implementation via [InviteRewardsAPI.get].
 */
interface InviteRewardsAPI {

    /**
     * Retrieves the complete data profile for a player.
     * * @param uuid The unique identifier of the player.
     * @return A [java.util.concurrent.CompletableFuture] containing the [com.github.irvinglink.inviteRewards.database.models.PlayerData], or null if the player is not registered.
     * @note This performs a database lookup. Always handle the result asynchronously.
     */
    fun getPlayerData(uuid: UUID): CompletableFuture<PlayerData?>

    /**
     * Manually modifies a player's points balance.
     * * @param uuid The unique identifier of the player to modify.
     * @param points The amount to add (use positive numbers) or subtract (use negative numbers).
     * @return A [CompletableFuture] that completes once the data is persisted to the database.
     */
    fun updatePoints(uuid: UUID, points: Int): CompletableFuture<Void?>

    /**
     * Attempts to process an invitation claim.
     * This triggers the internal validation logic (anti-multiaccount, self-invite checks, etc.)
     * and executes the configured rewards if successful.
     * * @param player The player attempting to claim the code.
     * @param inviteTypeId The ID of the invite category (e.g., "GLOBAL", "MEDIEVAL").
     * @param code The invite code string provided by the player.
     * @return A [CompletableFuture] containing the [com.github.irvinglink.inviteRewards.models.InvitationResult] (Success, InvalidCode, Suspicious, etc.).
     */
    fun claimInvite(player: Player, inviteTypeId: String, code: String): CompletableFuture<InvitationResult>

    /**
     * Fetches all invite codes (active and inactive) owned by a specific player.
     * * @param uuid The unique identifier of the code owner.
     * @return A [CompletableFuture] with a list of all [com.github.irvinglink.inviteRewards.database.models.InviteCodeData] found in storage.
     */
    fun getPlayerCodes(uuid: UUID): CompletableFuture<List<InviteCodeData>>

    /**
     * Retrieves the currently active invite code for a specific player and category.
     * * @param uuid The unique identifier of the player.
     * @param inviteTypeId The ID of the invite category.
     * @return A [CompletableFuture] containing the code string, or null if no active code exists for that type.
     */
    fun getActiveCode(uuid: UUID, inviteTypeId: String): CompletableFuture<String?>

    /**
     * Retrieves the current top players from the local ranking cache.
     * * @param limit The maximum number of entries to return (Default: 10).
     * @return A list of [PlayerData] currently in the top positions.
     * @warning This pulls from RAM cache for high performance. It may be slightly behind
     * the database depending on the refresh interval.
     */
    fun getLeaderboard(limit: Int = 10): List<PlayerData>

    /**
     * Retrieves the 1-based rank position of a player.
     * * @param uuid The unique identifier of the player.
     * @return The rank position (e.g., 1 for #1), or -1 if the player is not within the cached leaderboard.
     */
    fun getPlayerRank(uuid: UUID): Int

    /**
     * Forcefully synchronizes the local leaderboard cache with the database.
     * Use this if you have manually modified points via external sources and need
     * the rankings to update immediately.
     * * @return A [CompletableFuture] that completes once the cache has been refreshed.
     */
    fun refreshLeaderboard(): CompletableFuture<Void?>

    /**
     * Manually triggers the delivery of rewards that were stored while the player was offline.
     * This is usually called automatically on join, but can be forced if needed.
     * * @param player The player who should receive their pending rewards.
     */
    fun processPendingRewards(player: Player)

    companion object {
        private var instance: InviteRewardsAPI? = null

        /**
         * Returns the registered implementation of the API.
         * @return The [InviteRewardsAPI] instance.
         * @throws IllegalStateException if the plugin is not yet enabled.
         */
        @JvmStatic
        fun get(): InviteRewardsAPI {
            return instance ?: throw IllegalStateException("InviteRewardsAPI has not been initialized yet. Is InviteRewards enabled?")
        }

        /**
         * Internal method to register the API provider.
         * This is called automatically by the plugin core during onEnable.
         * * @param api The implementation provided by the plugin core.
         */
        @ApiStatus.Internal
        internal fun register(api: InviteRewardsAPI) {
            instance = api
        }
    }
}