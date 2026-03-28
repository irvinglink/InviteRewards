package com.github.irvinglink.inviteRewards.commands.commands.invitationSubCommands

import com.github.irvinglink.inviteRewards.commands.builders.SubCommand
import com.github.irvinglink.inviteRewards.enums.config.MESSAGES
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderContext
import com.github.irvinglink.inviteRewards.utils.nms.PacketUtils
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CodeSubCommand : SubCommand {

    override val name: String get() = "code"
    override val description: String get() = "Shows your invitation code for a specific type"
    override val syntax: String get() = "/invitation code <type>"

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (sender !is Player) {
            sender.sendMessage(plugin.chat.format(MESSAGES.NO_PERMISSION_CONSOLE.message))
            return
        }

        if (args.isEmpty()) {
            sender.sendMessage(plugin.chat.format(sender, null, syntax, MESSAGES.NOT_ENOUGH_ARGS.message, true))
            return
        }

        val inviteTypeId = args[0]
        val inviteTypeSection = plugin.configFile.getConfigurationSection("invite-types.$inviteTypeId")

        if (inviteTypeSection == null || !inviteTypeSection.getBoolean("enabled", true)) {
            sender.sendMessage(plugin.chat.format(sender, null, null, MESSAGES.INVITE_CLAIM_INVALID_CODE.message, true))
            return
        }

        plugin.inviteManager.getCode(sender.uniqueId, inviteTypeId).thenAccept { code ->
            plugin.server.scheduler.runTask(plugin, Runnable {
                if (code == null) {
                    sender.sendMessage(plugin.chat.format(MESSAGES.INVITE_CLAIM_NOT_REGISTERED, sender))
                    return@Runnable
                }

                val rawMessage = plugin.langFile.getString("Invite.Code_Message") ?: ""
                val rawHover = plugin.langFile.getString("Invite.Code_Hover") ?: "&bClick to copy code!"

                val placeholders = mapOf("%code%" to code, "%invite_type_id%" to inviteTypeId)

                val formattedMessage = applyInternalPlaceholders(rawMessage, placeholders)
                val formattedHover = applyInternalPlaceholders(rawHover, placeholders)

                val component = plugin.chat.suggestCommand(
                    text = formattedMessage, suggestion = code, hoverText = formattedHover,
                    context = PlaceholderContext(player = sender)
                )

                sender.spigot().sendMessage(component)

                PacketUtils.playSound(sender, "UI_BUTTON_CLICK", 1f, 1.2f)
            })
        }

    }

    private fun applyInternalPlaceholders(text: String, placeholders: Map<String, String>): String {
        var result = text
        placeholders.forEach { (key, value) -> result = result.replace(key, value) }
        return result
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            1 -> getAvailableInviteTypes(sender).filter { it.startsWith(args[0], ignoreCase = true) }
            else -> emptyList()
        }
    }

}