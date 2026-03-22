package com.github.irvinglink.amethystLibKotlin.utils.chat.placeholder

import org.bukkit.OfflinePlayer

data class PlaceholderContext(
    val player: OfflinePlayer? = null,
    val target: OfflinePlayer? = null,
    val value: String? = null
)
