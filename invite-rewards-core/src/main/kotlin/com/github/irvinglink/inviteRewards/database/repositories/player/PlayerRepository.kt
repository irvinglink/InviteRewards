package com.github.irvinglink.inviteRewards.database.repositories.player

import com.github.irvinglink.inviteRewards.database.models.PlayerData
import java.util.UUID
import java.util.concurrent.CompletableFuture

interface PlayerRepository {

    fun save(data: PlayerData): CompletableFuture<Unit>

    fun load(uuid: UUID): CompletableFuture<PlayerData?>

    fun delete(uuid: UUID): CompletableFuture<Unit>

    fun exists(uuid: UUID): CompletableFuture<Boolean>

    fun findByIp(ip: String): CompletableFuture<List<PlayerData>>

    fun getTopPlayers(limit: Int): CompletableFuture<List<PlayerData>>
}