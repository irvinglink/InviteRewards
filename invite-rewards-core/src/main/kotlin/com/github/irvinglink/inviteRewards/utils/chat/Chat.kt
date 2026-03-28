package com.github.irvinglink.inviteRewards.utils.chat

import com.github.irvinglink.inviteRewards.InviteRewardsPlugin
import com.github.irvinglink.inviteRewards.enums.config.MESSAGES
import com.github.irvinglink.inviteRewards.utils.chat.hex.HexManager
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderContext
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderEngine
import com.github.irvinglink.inviteRewards.utils.version.VersionUtils
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.Locale

class Chat(
    private val plugin: InviteRewardsPlugin,
    private val placeholderEngine: PlaceholderEngine
) {

    private val supportsHex: Boolean
        get() = VersionUtils.isHexSupported

    fun colorize(text: String?): String {
        if (text.isNullOrEmpty()) return ""

        var result = text

        if (supportsHex && '#' in result) {
            result = HexManager.toChatColorString(result)
        }

        return if ('&' in result || '§' in result) {
            ChatColor.translateAlternateColorCodes('&', result)
        } else {
            result
        }
    }

    fun stripColor(text: String?): String {
        return ChatColor.stripColor(text ?: "") ?: ""
    }

    fun firstCharUpper(value: String?): String {
        if (value.isNullOrBlank()) return ""

        return value.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }

    fun format(
        text: String?,
        context: PlaceholderContext = PlaceholderContext(),
        color: Boolean = true
    ): String {
        return placeholderEngine.replace(text, context, color).orEmpty()
    }

    fun format(
        message: MESSAGES?,
        context: PlaceholderContext = PlaceholderContext(),
        color: Boolean = true
    ): String {
        return format(message?.message, context, color)
    }

    fun format(
        player: OfflinePlayer?,
        text: String?,
        color: Boolean = true
    ): String {
        return format(
            text = text,
            context = PlaceholderContext(player = player),
            color = color
        )
    }

    fun format(
        player: OfflinePlayer?,
        message: MESSAGES?,
        color: Boolean = true
    ): String {
        return format(
            text = message?.message,
            context = PlaceholderContext(player = player),
            color = color
        )
    }

    fun format(
        player: OfflinePlayer?,
        target: OfflinePlayer?,
        value: String?,
        text: String?,
        color: Boolean = true
    ): String {
        return format(
            text = text,
            context = PlaceholderContext(
                player = player,
                target = target,
                value = value
            ),
            color = color
        )
    }

    fun format(
        player: OfflinePlayer?,
        target: OfflinePlayer?,
        value: String?,
        message: MESSAGES?,
        color: Boolean = true
    ): String {
        return format(
            text = message?.message,
            context = PlaceholderContext(
                player = player,
                target = target,
                value = value
            ),
            color = color
        )
    }

    fun format(
        text: String?,
        sender: CommandSender? = null,
        target: OfflinePlayer? = null,
        value: String? = null,
        color: Boolean = true
    ): String {
        val context = PlaceholderContext(
            player = sender as? OfflinePlayer,
            target = target,
            value = value
        )
        return placeholderEngine.replace(text, context, color).orEmpty()
    }

    fun format(
        message: MESSAGES?,
        sender: CommandSender? = null,
        target: OfflinePlayer? = null,
        value: String? = null,
        color: Boolean = true
    ): String {
        return format(
            text = message?.message,
            sender = sender,
            target = target,
            value = value,
            color = color
        )
    }

    fun component(
        text: String?,
        context: PlaceholderContext = PlaceholderContext(),
        color: Boolean = true
    ): TextComponent {
        return TextComponent(format(text, context, color))
    }

    fun component(
        message: MESSAGES?,
        context: PlaceholderContext = PlaceholderContext(),
        color: Boolean = true
    ): TextComponent {
        return TextComponent(format(message, context, color))
    }

    fun suggestCommand(
        text: String?,
        suggestion: String?,
        hoverText: String? = null,
        context: PlaceholderContext = PlaceholderContext(),
        color: Boolean = true
    ): TextComponent {
        return interactiveComponent(
            text = text,
            hoverText = hoverText,
            clickAction = ClickEvent.Action.SUGGEST_COMMAND,
            clickValue = suggestion,
            context = context,
            color = color
        )
    }

    fun runCommand(
        text: String?,
        command: String?,
        hoverText: String? = null,
        context: PlaceholderContext = PlaceholderContext(),
        color: Boolean = true
    ): TextComponent {
        return interactiveComponent(
            text = text,
            hoverText = hoverText,
            clickAction = ClickEvent.Action.RUN_COMMAND,
            clickValue = command,
            context = context,
            color = color
        )
    }

    fun openUrl(
        text: String?,
        url: String?,
        hoverText: String? = null,
        context: PlaceholderContext = PlaceholderContext(),
        color: Boolean = true
    ): TextComponent {
        return interactiveComponent(
            text = text,
            hoverText = hoverText,
            clickAction = ClickEvent.Action.OPEN_URL,
            clickValue = url,
            context = context,
            color = color
        )
    }

    private fun interactiveComponent(
        text: String?,
        hoverText: String?,
        clickAction: ClickEvent.Action?,
        clickValue: String?,
        context: PlaceholderContext,
        color: Boolean
    ): TextComponent {
        val component = TextComponent(format(text, context, color))

        if (!clickValue.isNullOrBlank() && clickAction != null) {
            component.clickEvent = ClickEvent(clickAction, clickValue)
        }

        if (!hoverText.isNullOrBlank()) {
            component.hoverEvent = HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                ComponentBuilder(format(hoverText, context, color)).create()
            )
        }

        return component
    }

    fun sendClickableCommand(
        sender: CommandSender,
        text: String,
        command: String,
        hover: String? = null,
        context: PlaceholderContext = PlaceholderContext()
    ) {
        val component = runCommand(text, command, hover, context)
        sendComponent(sender, component)
    }

    fun sendCompositeMessage(
        sender: CommandSender,
        prefix: String,
        clickableText: String,
        suffix: String,
        command: String,
        hover: String? = null,
        context: PlaceholderContext = PlaceholderContext()
    ) {
        val message = TextComponent(format(prefix, context))
        val clickable = runCommand(clickableText, command, hover, context)
        val end = TextComponent(format(suffix, context))

        message.addExtra(clickable)
        message.addExtra(end)

        sendComponent(sender, message)
    }

    fun sendComponent(sender: CommandSender, component: TextComponent) {
        if (sender is Player) {
            sender.spigot().sendMessage(component)
        } else {
            sender.sendMessage(component.toPlainText())
        }
    }
}