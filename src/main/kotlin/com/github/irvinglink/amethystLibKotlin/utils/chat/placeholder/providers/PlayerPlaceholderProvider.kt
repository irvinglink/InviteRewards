package com.github.irvinglink.amethystLibKotlin.utils.chat.placeholder.providers

import com.github.irvinglink.amethystLibKotlin.utils.chat.placeholder.PlaceholderContext
import com.github.irvinglink.amethystLibKotlin.utils.chat.placeholder.PlaceholderProvider

class PlayerPlaceholderProvider : PlaceholderProvider {
    override fun register(registry: MutableMap<String, (PlaceholderContext) -> String?>) {
        registry["player_name"] = { ctx -> ctx.player?.name ?: "null" }
        registry["target_name"] = { ctx -> ctx.target?.name ?: "null" }
    }
}