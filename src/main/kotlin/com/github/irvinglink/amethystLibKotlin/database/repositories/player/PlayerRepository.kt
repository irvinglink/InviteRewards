package com.github.irvinglink.amethystLibKotlin.database.repositories.player

import com.github.irvinglink.amethystLibKotlin.database.models.PlayerData
import java.util.UUID

interface PlayerRepository {

    fun save(data: PlayerData)

    fun load(uuid: UUID): PlayerData?

    fun delete(uuid: UUID)

    fun exists(uuid: UUID): Boolean

}