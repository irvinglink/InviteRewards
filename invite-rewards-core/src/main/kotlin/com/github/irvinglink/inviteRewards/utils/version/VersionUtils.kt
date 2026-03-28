package com.github.irvinglink.inviteRewards.utils.version

import org.bukkit.Bukkit

object VersionUtils {

    val minecraftVersion: String by lazy {
        Bukkit.getBukkitVersion().substringBefore("-")
    }

    val minorVersion: Int by lazy {
        minecraftVersion.split(".")
            .getOrNull(1)
            ?.toIntOrNull() ?: 0
    }

    val patchVersion: Int by lazy {
        minecraftVersion.split(".")
            .getOrNull(2)
            ?.toIntOrNull() ?: 0
    }

    val isLegacy: Boolean by lazy { minorVersion < 14 }
    val isModern: Boolean by lazy { minorVersion >= 14 }
    val isPdcSupported: Boolean by lazy { minorVersion >= 14 }
    val isHexSupported: Boolean by lazy { minorVersion >= 16 }

    fun isAtLeast(version: Int): Boolean {
        return minorVersion >= version
    }
}