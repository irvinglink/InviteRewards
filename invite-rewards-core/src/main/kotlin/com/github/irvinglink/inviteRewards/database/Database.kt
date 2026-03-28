package com.github.irvinglink.inviteRewards.database

import com.github.irvinglink.inviteRewards.database.repositories.claim.*
import com.github.irvinglink.inviteRewards.database.repositories.invitecode.*
import com.github.irvinglink.inviteRewards.database.repositories.pending.*
import com.github.irvinglink.inviteRewards.database.repositories.player.*
import com.github.irvinglink.inviteRewards.storage.StorageProvider
import com.github.irvinglink.inviteRewards.storage.providers.sql.SQLStorage
import com.github.irvinglink.inviteRewards.storage.providers.yaml.YamlStorage

class Database(provider: StorageProvider) {
    val players: PlayerRepository
    val pendingRewards: PendingRewardRepository
    val inviteCodes: InviteCodeRepository
    val claims: ClaimRepository

    init {
        when (provider) {
            is SQLStorage -> {
                players = SQLPlayerRepository(provider)
                pendingRewards = SQLPendingRewardRepository(provider)
                inviteCodes = SQLInviteCodeRepository(provider)
                claims = SQLClaimRepository(provider)
            }

            is YamlStorage -> {
                players = YamlPlayerRepository(provider)
                pendingRewards = YamlPendingRewardRepository(provider)
                inviteCodes = YamlInviteCodeRepository(provider)
                claims = YamlClaimRepository(provider)
            }

            else -> throw IllegalStateException("Unsupported storage provider: ${provider::class.simpleName}")
        }
    }
}