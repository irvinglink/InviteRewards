package com.github.irvinglink.amethystLibKotlin.utils.nms

import com.github.irvinglink.amethystLibKotlin.AmethystLibKotlin
import com.github.irvinglink.amethystLibKotlin.core.PluginContext
import com.github.irvinglink.amethystLibKotlin.utils.version.VersionUtils
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.reflect.Constructor
import java.lang.reflect.Method

object PacketUtils {

    private val plugin = PluginContext.plugin

    fun sendActionBar(player: Player, message: String?) {
        if (!player.isOnline || message.isNullOrBlank()) return

        try {
            if (VersionUtils.isAtLeast(9)) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(message))
                return
            }
        } catch (_: Throwable) {
        }

        // Fallback legacy 1.8 NMS
        try {
            sendLegacyActionBar(player, message)
        } catch (ex: Throwable) {
            plugin.logger.warning("Could not send action bar to ${player.name}: ${ex.message}")
        }
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

        try {
            val method = player.javaClass.getMethod(
                "sendTitle",
                String::class.java,
                String::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            )

            method.invoke(player, title, subtitle, fadeIn, stay, fadeOut)
            return
        } catch (_: NoSuchMethodException) {
        } catch (ex: Throwable) {
            plugin.logger.warning("Modern title method failed for ${player.name}: ${ex.message}")
        }

        // Fallback legacy 1.8 NMS
        try {
            sendLegacyTitle(player, title, subtitle, fadeIn, stay, fadeOut)
        } catch (ex: Throwable) {
            plugin.logger.warning("Could not send title to ${player.name}: ${ex.message}")
        }
    }

    fun playSound(player: Player, soundName: String, volume: Float = 1f, pitch: Float = 1f) {
        if (!player.isOnline || soundName.isBlank()) return

        try {
            player.playSound(player.location, soundName, volume, pitch)
        } catch (ex: Throwable) {
            plugin.logger.warning("Invalid sound '$soundName' for ${player.name}: ${ex.message}")
        }
    }

    private fun sendLegacyActionBar(player: Player, message: String) {
        val version = getServerVersion()

        val packetPlayOutChatClass = nmsClass(version, "PacketPlayOutChat")
        val iChatBaseComponentClass = nmsClass(version, "IChatBaseComponent")
        val chatSerializerClass = nmsClass(version, "IChatBaseComponent\$ChatSerializer")

        val serializerMethod = chatSerializerClass.getMethod("a", String::class.java)
        val component = serializerMethod.invoke(null, "{\"text\":\"${escapeJson(message)}\"}")

        val constructor: Constructor<*> = try {
            packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, Byte::class.javaPrimitiveType)
        } catch (_: NoSuchMethodException) {
            packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, Int::class.javaPrimitiveType)
        }

        val packet = try {
            constructor.newInstance(component, 2.toByte())
        } catch (_: Throwable) {
            constructor.newInstance(component, 2)
        }

        sendPacket(player, packet)
    }

    private fun sendLegacyTitle(
        player: Player,
        title: String?,
        subtitle: String?,
        fadeIn: Int,
        stay: Int,
        fadeOut: Int
    ) {
        val version = getServerVersion()

        val packetTitleClass = nmsClass(version, "PacketPlayOutTitle")
        val enumTitleActionClass = nmsClass(version, "PacketPlayOutTitle\$EnumTitleAction")
        val iChatBaseComponentClass = nmsClass(version, "IChatBaseComponent")
        val chatSerializerClass = nmsClass(version, "IChatBaseComponent\$ChatSerializer")

        val serializerMethod = chatSerializerClass.getMethod("a", String::class.java)
        val timesEnum = java.lang.Enum.valueOf(
            enumTitleActionClass as Class<out Enum<*>>,
            "TIMES"
        )
        val titleEnum = java.lang.Enum.valueOf(
            enumTitleActionClass as Class<out Enum<*>>,
            "TITLE"
        )
        val subtitleEnum = java.lang.Enum.valueOf(
            enumTitleActionClass as Class<out Enum<*>>,
            "SUBTITLE"
        )

        val timingConstructor = packetTitleClass.getConstructor(
            enumTitleActionClass,
            iChatBaseComponentClass,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType
        )

        val componentConstructor = packetTitleClass.getConstructor(
            enumTitleActionClass,
            iChatBaseComponentClass
        )

        val emptyComponent = serializerMethod.invoke(null, "{\"text\":\"\"}")

        val timesPacket = timingConstructor.newInstance(timesEnum, emptyComponent, fadeIn, stay, fadeOut)
        sendPacket(player, timesPacket)

        if (!title.isNullOrBlank()) {
            val titleComponent = serializerMethod.invoke(null, "{\"text\":\"${escapeJson(title)}\"}")
            val titlePacket = componentConstructor.newInstance(titleEnum, titleComponent)
            sendPacket(player, titlePacket)
        }

        if (!subtitle.isNullOrBlank()) {
            val subtitleComponent = serializerMethod.invoke(null, "{\"text\":\"${escapeJson(subtitle)}\"}")
            val subtitlePacket = componentConstructor.newInstance(subtitleEnum, subtitleComponent)
            sendPacket(player, subtitlePacket)
        }
    }

    private fun sendPacket(player: Player, packet: Any) {
        val craftPlayer = player.javaClass
        val getHandle = craftPlayer.getMethod("getHandle")
        val entityPlayer = getHandle.invoke(player)

        val playerConnectionField = entityPlayer.javaClass.getField("playerConnection")
        val playerConnection = playerConnectionField.get(entityPlayer)

        val sendPacketMethod: Method = playerConnection.javaClass.methods.first {
            it.name == "sendPacket" && it.parameterTypes.size == 1
        }

        sendPacketMethod.invoke(playerConnection, packet)
    }

    private fun nmsClass(version: String, simpleName: String): Class<*> {
        return Class.forName("net.minecraft.server.$version.$simpleName")
    }

    private fun getServerVersion(): String {
        return Bukkit.getServer()::class.java.`package`.name.substringAfterLast('.')
    }

    private fun escapeJson(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
    }
}