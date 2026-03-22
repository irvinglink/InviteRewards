package com.github.irvinglink.amethystLibKotlin.features.editor

import com.github.irvinglink.amethystLibKotlin.AmethystLibKotlin
import com.github.irvinglink.amethystLibKotlin.core.PluginContext
import org.bukkit.entity.Player

interface IChatEditor<T : Any> {

    val plugin: AmethystLibKotlin get() = PluginContext.plugin

    fun onType(player: Player, obj: T, type: EditorType, input: String): Boolean
}