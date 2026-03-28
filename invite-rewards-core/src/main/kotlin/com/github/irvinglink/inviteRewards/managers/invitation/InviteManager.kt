package com.github.irvinglink.inviteRewards.managers.invitation

import com.github.irvinglink.inviteRewards.core.PluginContext
import com.github.irvinglink.inviteRewards.database.models.ClaimRecord
import com.github.irvinglink.inviteRewards.database.models.InviteCodeData
import com.github.irvinglink.inviteRewards.database.models.PlayerData
import com.github.irvinglink.inviteRewards.models.InvitationResult
import com.github.irvinglink.inviteRewards.validators.InviteValidator
import com.github.irvinglink.inviteRewards.validators.LiteInviteValidator
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

class InviteManager {

    private val plugin = PluginContext.plugin
    private val db get() = plugin.database
    private val config get() = plugin.configFile

    var validator: InviteValidator = LiteInviteValidator(config.getConfigurationSection("invite-types"))

    fun loadOrRegister(player: Player): CompletableFuture<PlayerData> {
        return db.players.load(player.uniqueId).thenCompose { existing ->
            val data = existing?.copy(
                name = player.name,
                ipAddress = player.address?.address?.hostAddress
            ) ?: PlayerData(
                uuid = player.uniqueId,
                name = player.name,
                ipAddress = player.address?.address?.hostAddress
            )

            db.players.save(data)
                .thenCompose { createAutomaticCodesIfNeeded(player) }
                .thenApply { data }
        }
    }

    private fun createAutomaticCodesIfNeeded(player: Player): CompletableFuture<Unit> {
        val typesSection = config.getConfigurationSection("invite-types") ?: return CompletableFuture.completedFuture(Unit)

        val keys = typesSection.getKeys(false).toList()

        val keysToProcess = validator.filterTypes(keys)

        val futures = keysToProcess.mapNotNull { id ->
            val section = typesSection.getConfigurationSection(id) ?: return@mapNotNull null

            val isAuto = section.getString("code.mode")?.equals("AUTO", true) ?: false
            val hasPerm = !section.getBoolean("settings.requires-permission-to-own") || player.hasPermission(section.getString("settings.owner-permission") ?: "")

            if (!section.getBoolean("enabled", true) || !isAuto || !hasPerm) return@mapNotNull null

            db.inviteCodes.loadByOwnerAndType(player.uniqueId, id).thenCompose { existing ->
                if (existing.any { it.active }) CompletableFuture.completedFuture(Unit)
                else generateUniqueCode(id, section).thenCompose { code ->
                    db.inviteCodes.save(InviteCodeData(code, player.uniqueId, id, active = true))
                }
            }
        }
        return CompletableFuture.allOf(*futures.toTypedArray()).thenApply { Unit }
    }

    fun claim(player: Player, typeId: String, code: String): CompletableFuture<InvitationResult> {
        if (!validator.isTypeAllowed(typeId)) {
            return CompletableFuture.completedFuture(InvitationResult.InvalidCode)
        }

        val section = config.getConfigurationSection("invite-types.$typeId") ?: return CompletableFuture.completedFuture(InvitationResult.InvalidCode)
        if (!section.getBoolean("enabled", true) || !section.getBoolean("claim.enabled", true)) return CompletableFuture.completedFuture(InvitationResult.InvalidCode)

        val normalized = normalizeCode(typeId, code)

        return db.players.load(player.uniqueId).thenCompose { claimer ->
            if (claimer == null) return@thenCompose CompletableFuture.completedFuture(InvitationResult.NotRegistered)

            canPlayerClaim(claimer.uuid, typeId).thenCompose { inviterUuid ->
                if (inviterUuid != null) return@thenCompose CompletableFuture.completedFuture(InvitationResult.AlreadyClaimed(inviterUuid))

                db.inviteCodes.load(normalized, typeId).thenCompose { inviteCode ->
                    if (inviteCode == null || !inviteCode.active) return@thenCompose CompletableFuture.completedFuture(InvitationResult.InvalidCode)

                    db.players.load(inviteCode.ownerUuid).thenCompose { inviter ->
                        when {
                            inviter == null -> CompletableFuture.completedFuture(InvitationResult.InvalidCode)
                            inviter.uuid == claimer.uuid -> CompletableFuture.completedFuture(InvitationResult.SelfInvite)
                            isSuspicious(claimer, inviter) -> CompletableFuture.completedFuture(InvitationResult.SuspiciousActivity)
                            else -> processClaim(claimer, inviter, typeId, inviteCode.code)
                        }
                    }
                }
            }
        }
    }

    private fun processClaim(claimer: PlayerData, inviter: PlayerData, typeId: String, code: String): CompletableFuture<InvitationResult> {
        val section = config.getConfigurationSection("invite-types.$typeId")!!
        val updatedInviter = inviter.copy(points = inviter.points + section.getInt("claim.points.inviter", 10), totalInvites = inviter.totalInvites + 1)
        val updatedClaimer = claimer.copy(points = claimer.points + section.getInt("claim.points.claimer", 5))

        return db.players.save(updatedInviter)
            .thenCompose { db.players.save(updatedClaimer) }
            .thenCompose { db.claims.save(ClaimRecord(claimer.uuid, inviter.uuid, typeId, code)) }
            .thenApply { InvitationResult.Success(updatedClaimer, updatedInviter) }
    }

    private fun canPlayerClaim(uuid: UUID, typeId: String): CompletableFuture<UUID?> {
        val global = config.getString("claim-settings.mode")?.equals("GLOBAL", true) ?: false
        val future = if (global) db.claims.loadClaimsOf(uuid) else db.claims.loadClaimsOf(uuid, typeId)
        return future.thenApply { it.firstOrNull()?.inviterUuid }
    }

    private fun generateUniqueCode(typeId: String, section: ConfigurationSection): CompletableFuture<String> {
        val len = section.getInt("code.segment-length", config.getInt("invite-code.defaults.segment-length", 4))
        val seg = section.getInt("code.segments", config.getInt("invite-code.defaults.segments", 2))
        val code = normalizeCode(typeId, InviteCodeGenerator.generate(len, seg))

        return db.inviteCodes.load(code, typeId).thenCompose { existing ->
            if (existing == null) CompletableFuture.completedFuture(code) else generateUniqueCode(typeId, section)
        }
    }

    private fun isSuspicious(claimer: PlayerData, inviter: PlayerData): Boolean {
        if (!config.getBoolean("anti-multiaccount.enabled", true) || !config.getBoolean("anti-multiaccount.block-same-ip", true)) return false
        return claimer.ipAddress != null && claimer.ipAddress == inviter.ipAddress
    }

    private fun normalizeCode(typeId: String, code: String): String {
        val sensitive = config.getBoolean("invite-types.$typeId.code.case-sensitive", config.getBoolean("invite-code.defaults.case-sensitive", false))
        return if (sensitive) code else code.uppercase()
    }

    fun getCodes(uuid: UUID) = db.inviteCodes.loadByOwner(uuid)

    fun getCode(uuid: UUID, typeId: String): CompletableFuture<String?> =
        db.inviteCodes.loadByOwnerAndType(uuid, typeId).thenApply { it.firstOrNull { c -> c.active }?.code }

    /**
     * El leaderboard ahora es seguro porque depende de si la propiedad fue inyectada.
     */
    fun getLeaderboard(limit: Int = 10): List<PlayerData> {
        return plugin.leaderboardManager?.getTopPlayers(limit) ?: emptyList()
    }
}