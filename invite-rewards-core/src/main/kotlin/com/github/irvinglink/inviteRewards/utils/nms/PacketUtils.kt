package com.github.irvinglink.inviteRewards.utils.nms

import com.cryptomorin.xseries.XSound
import com.cryptomorin.xseries.messages.ActionBar
import com.cryptomorin.xseries.messages.Titles
import com.github.irvinglink.inviteRewards.core.PluginContext
import org.bukkit.entity.Player
import java.util.*

object PacketUtils {

    private val plugin = PluginContext.plugin

    fun sendActionBar(player: Player, message: String?) {
        if (!player.isOnline || message.isNullOrBlank()) return

        // XSeries handles the version-specific packet sending internally
        ActionBar.sendActionBar(plugin, player, message)
    }

    fun sendTitle(
        player: Player,
        title: String?,
        subtitle: String?,
        fadeIn: Int = 10,
        stay: Int = 40,
        fadeOut: Int = 10
    ) {
        if (!player.isOnline) return
        Titles.sendTitle(player, fadeIn, stay, fadeOut, title, subtitle)
    }

    fun playSound(player: Player, soundName: String, volume: Float = 1f, pitch: Float = 1f) {
        if (!player.isOnline || soundName.isBlank()) return
        val soundRecord = XSound.of(soundName.uppercase(Locale.ENGLISH)).orElse(XSound.ENTITY_EXPERIENCE_ORB_PICKUP)

        try {
            soundRecord.play(player, volume, pitch)
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to play sound '$soundName': ${ex.message}")
        }

    }
}