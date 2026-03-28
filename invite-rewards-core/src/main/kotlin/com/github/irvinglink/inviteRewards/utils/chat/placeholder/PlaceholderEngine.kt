package com.github.irvinglink.inviteRewards.utils.chat.placeholder

import me.clip.placeholderapi.PlaceholderAPI
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit

class PlaceholderEngine(
    private val registry: PlaceholderRegistry
) {

    private companion object {
        const val NAMESPACE = "inviterewards"
        val PATTERN = Regex("%([a-zA-Z0-9]+)_([a-zA-Z0-9_]+)%")

        val isPlaceholderAPIEnabled: Boolean
            get() = Bukkit.getPluginManager().getPlugin("PlaceholderAPI")?.isEnabled == true
    }

    fun replace(text: String?, context: PlaceholderContext, color: Boolean = true): String? {
        if (text.isNullOrEmpty()) return text

        var result = text

        if ('%' in result) {
            val cache = HashMap<String, String?>()

            result = PATTERN.replace(result) { match ->
                val foundNamespace = match.groupValues[1]
                val key = match.groupValues[2]
                val normalizedKey = key.lowercase()

                if (!foundNamespace.equals(NAMESPACE, ignoreCase = true)) {
                    return@replace match.value
                }

                cache.getOrPut(normalizedKey) {
                    registry.resolve(normalizedKey, context)
                } ?: "null"
            }
        }

        if (context.player != null && '%' in result && isPlaceholderAPIEnabled) {
            result = PlaceholderAPI.setPlaceholders(context.player, result)
        }

        return if (color && '&' in result) {
            ChatColor.translateAlternateColorCodes('&', result)
        } else {
            result
        }
    }
}