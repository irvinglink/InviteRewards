package com.github.irvinglink.inviteRewards.utils.chat.placeholder

import com.github.irvinglink.inviteRewards.core.PluginContext
import com.github.irvinglink.inviteRewards.database.Database

interface PlaceholderProvider {

    val database : Database
        get() = PluginContext.plugin.database

    fun register(registry: MutableMap<String, (PlaceholderContext) -> String?>)
}