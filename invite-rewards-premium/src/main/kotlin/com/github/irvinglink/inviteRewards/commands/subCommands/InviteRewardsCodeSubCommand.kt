package com.github.irvinglink.inviteRewards.commands.subCommands

import com.github.irvinglink.inviteRewards.commands.builders.SubCommand
import com.github.irvinglink.inviteRewards.database.models.InviteCodeData
import com.github.irvinglink.inviteRewards.enums.config.MESSAGES
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderContext
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.CompletableFuture

class InviteRewardsCodeSubCommand : SubCommand {

    override val name: String = "code"
    override val description: String = "Manage invite codes"
    override val syntax: String = "/inviterewards code <create|delete|list> ..."
    override val permission: String = "inviterewards.command.admin"
    override val allowConsole: Boolean = true

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            sender.sendMessage(
                chat.format(
                    "%inviterewards_prefix% " + MESSAGES.NOT_ENOUGH_ARGS,
                    PlaceholderContext(
                        player = sender as? OfflinePlayer,
                        value = syntax
                    )
                )
            )
            return
        }

        when (args[0].lowercase()) {
            "create" -> handleCreate(sender, args)
            "delete" -> handleDelete(sender, args)
            "list" -> handleList(sender, args)
            else -> sender.sendMessage(
                chat.format(
                    "%inviterewards_prefix% " + MESSAGES.WRONG_USAGE,
                    PlaceholderContext(
                        player = sender as? OfflinePlayer,
                        value = syntax
                    )
                )
            )
        }
    }

    private fun handleCreate(sender: CommandSender, args: Array<String>) {
        if (args.size < 4) {
            sender.sendMessage(
                chat.format(
                    "%inviterewards_prefix% " + MESSAGES.NOT_ENOUGH_ARGS,
                    PlaceholderContext(
                        player = sender as? OfflinePlayer,
                        value = "/inviterewards code create <type> <player> <code>"
                    )
                )
            )
            return
        }

        val inviteTypeId = args[1]
        val targetName = args[2]
        val rawCode = args[3]

        val inviteTypeSection = plugin.configFile.getConfigurationSection("invite-types.$inviteTypeId")
        if (inviteTypeSection == null || !inviteTypeSection.getBoolean("enabled", true)) {
            sender.sendMessage(chat.format("%inviterewards_prefix% &cThat invite type does not exist or is disabled&7."))
            return
        }

        val mode = inviteTypeSection.getString("code.mode", "AUTO")!!.uppercase()
        if (mode != "MANUAL") {
            sender.sendMessage(chat.format("%inviterewards_prefix% &cThis invite type does not allow manual code creation&7."))
            return
        }

        val requiresCreatorPermission = inviteTypeSection.getBoolean("settings.requires-permission-to-create", false)
        val creatorPermission = inviteTypeSection.getString("settings.creator-permission").orEmpty()

        if (requiresCreatorPermission && creatorPermission.isNotBlank() && !sender.hasPermission(creatorPermission)) {
            sender.sendMessage(
                chat.format(
                    "%inviterewards_prefix% " + MESSAGES.NO_PERMISSION,
                    PlaceholderContext(player = sender as? OfflinePlayer)
                )
            )
            return
        }

        val target = findOfflinePlayer(targetName)
        if (target == null) {
            sender.sendMessage(
                chat.format(
                    "%inviterewards_prefix% " + MESSAGES.PLAYER_NOT_FOUND,
                    PlaceholderContext(
                        player = sender as? OfflinePlayer,
                        value = targetName
                    )
                )
            )
            return
        }

        val requiresOwnerPermission = inviteTypeSection.getBoolean("settings.requires-permission-to-own", false)
        val ownerPermission = inviteTypeSection.getString("settings.owner-permission").orEmpty()

        if (requiresOwnerPermission && ownerPermission.isNotBlank()) {
            val onlineTarget = Bukkit.getPlayerExact(target.name ?: targetName)
            if (onlineTarget == null || !onlineTarget.hasPermission(ownerPermission)) {
                sender.sendMessage(chat.format("%inviterewards_prefix% &cThat player does not have permission to own this invite type&7."))
                return
            }
        }

        val normalizedCode = normalizeCode(inviteTypeId, rawCode)

        plugin.database.inviteCodes.exists(normalizedCode, inviteTypeId)
            .thenCompose { exists ->
                if (exists) {
                    CompletableFuture.completedFuture(
                        CreateCodeResult(false, "%inviterewards_prefix% &cThat code already exists for this invite type&7.")
                    )
                } else {
                    plugin.database.inviteCodes.loadByOwnerAndType(target.uniqueId, inviteTypeId)
                        .thenCompose { existingCodes ->
                            val maxActiveCodes = inviteTypeSection.getInt("settings.max-active-codes-per-player", 1)
                            val activeCodes = existingCodes.count { it.active }

                            if (activeCodes >= maxActiveCodes) {
                                CompletableFuture.completedFuture(
                                    CreateCodeResult(
                                        false,
                                        "%inviterewards_prefix% &cThat player already reached the maximum active codes for this type&7."
                                    )
                                )
                            } else {
                                val createdBy = if (sender is Player) sender.uniqueId else null

                                plugin.database.inviteCodes.save(
                                    InviteCodeData(
                                        code = normalizedCode,
                                        ownerUuid = target.uniqueId,
                                        inviteTypeId = inviteTypeId,
                                        active = true,
                                        manual = true,
                                        createdBy = createdBy
                                    )
                                ).thenApply {
                                    CreateCodeResult(
                                        true,
                                        "%inviterewards_prefix% &aInvite code &e$normalizedCode &ahas been created for &b${target.name ?: target.uniqueId} &ain type &d$inviteTypeId&a."
                                    )
                                }
                            }
                        }
                }
            }
            .thenAccept { result ->
                plugin.server.scheduler.runTask(plugin, Runnable {
                    sender.sendMessage(chat.format(result.message))
                })
            }
            .exceptionally {
                plugin.server.scheduler.runTask(plugin, Runnable {
                    sender.sendMessage(chat.format("%inviterewards_prefix% &cFailed to create invite code&7."))
                })
                it.printStackTrace()
                null
            }
    }

    private fun handleDelete(sender: CommandSender, args: Array<String>) {
        if (args.size < 3) {
            sender.sendMessage(
                chat.format(
                    "%inviterewards_prefix% " + MESSAGES.NOT_ENOUGH_ARGS,
                    PlaceholderContext(
                        player = sender as? OfflinePlayer,
                        value = "/inviterewards code delete <type> <code>"
                    )
                )
            )
            return
        }

        val inviteTypeId = args[1]
        val rawCode = args[2]

        val inviteTypeSection = plugin.configFile.getConfigurationSection("invite-types.$inviteTypeId")
        if (inviteTypeSection == null || !inviteTypeSection.getBoolean("enabled", true)) {
            sender.sendMessage(chat.format("%inviterewards_prefix% &cThat invite type does not exist or is disabled&7."))
            return
        }

        val requiresCreatorPermission = inviteTypeSection.getBoolean("settings.requires-permission-to-create", false)
        val creatorPermission = inviteTypeSection.getString("settings.creator-permission").orEmpty()

        if (requiresCreatorPermission && creatorPermission.isNotBlank() && !sender.hasPermission(creatorPermission)) {
            sender.sendMessage(
                chat.format(
                    "%inviterewards_prefix% " + MESSAGES.NO_PERMISSION,
                    PlaceholderContext(player = sender as? OfflinePlayer)
                )
            )
            return
        }

        val normalizedCode = normalizeCode(inviteTypeId, rawCode)

        plugin.database.inviteCodes.load(normalizedCode, inviteTypeId)
            .thenCompose { existing ->
                if (existing == null) {
                    CompletableFuture.completedFuture("%inviterewards_prefix% &cThat code does not exist for this invite type&7.")
                } else {
                    plugin.database.inviteCodes.delete(normalizedCode, inviteTypeId)
                        .thenApply {
                            "%inviterewards_prefix% &aInvite code &e$normalizedCode &ahas been deleted from type &d$inviteTypeId&a."
                        }
                }
            }
            .thenAccept { message ->
                plugin.server.scheduler.runTask(plugin, Runnable {
                    sender.sendMessage(chat.format(message))
                })
            }
            .exceptionally {
                plugin.server.scheduler.runTask(plugin, Runnable {
                    sender.sendMessage(chat.format("%inviterewards_prefix% &cFailed to delete invite code&7."))
                })
                it.printStackTrace()
                null
            }
    }

    private fun handleList(sender: CommandSender, args: Array<String>) {
        if (args.size < 2) {
            sender.sendMessage(
                chat.format(
                    "%inviterewards_prefix% " + MESSAGES.NOT_ENOUGH_ARGS,
                    PlaceholderContext(
                        player = sender as? OfflinePlayer,
                        value = "/inviterewards code list <player>"
                    )
                )
            )
            return
        }

        val targetName = args[1]
        val target = findOfflinePlayer(targetName)

        if (target == null) {
            sender.sendMessage(
                chat.format(
                    "%inviterewards_prefix% " + MESSAGES.PLAYER_NOT_FOUND,
                    PlaceholderContext(
                        player = sender as? OfflinePlayer,
                        value = targetName
                    )
                )
            )
            return
        }

        plugin.database.inviteCodes.loadByOwner(target.uniqueId)
            .thenAccept { codes ->
                plugin.server.scheduler.runTask(plugin, Runnable {
                    sender.sendMessage(chat.format("&8&m----------------------------------------"))
                    sender.sendMessage(
                        chat.format("&eInvite codes for &b${target.name ?: target.uniqueId}&7:")
                    )

                    if (codes.isEmpty()) {
                        sender.sendMessage(chat.format("&7No invite codes found&7."))
                        sender.sendMessage(chat.format("&8&m----------------------------------------"))
                        return@Runnable
                    }

                    codes.sortedWith(compareBy<InviteCodeData> { it.inviteTypeId }.thenBy { it.code })
                        .forEach { code ->
                            val creatorName = code.createdBy?.let { Bukkit.getOfflinePlayer(it).name } ?: "SYSTEM"
                            val status = if (code.active) "&aACTIVE" else "&cINACTIVE"

                            sender.sendMessage(
                                chat.format(
                                    "&7- &d${code.inviteTypeId} &8| &e${code.code} &8| $status &8| &7manual: &f${code.manual} &8| &7createdBy: &b$creatorName"
                                )
                            )
                        }

                    sender.sendMessage(chat.format("&8&m----------------------------------------"))
                })
            }
            .exceptionally {
                plugin.server.scheduler.runTask(plugin, Runnable {
                    sender.sendMessage(chat.format("%inviterewards_prefix% &cFailed to load invite codes&7."))
                })
                it.printStackTrace()
                null
            }
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            1 -> listOf("create", "delete", "list")
                .filter { it.startsWith(args[0], ignoreCase = true) }

            2 -> when (args[0].lowercase()) {
                "create", "delete" -> getManualInviteTypes(sender)
                    .filter { it.startsWith(args[1], ignoreCase = true) }
                "list" -> playerNames(args[1])
                else -> emptyList()
            }

            3 -> when (args[0].lowercase()) {
                "create" -> playerNames(args[2])
                else -> emptyList()
            }

            else -> emptyList()
        }
    }

    private fun playerNames(input: String): List<String> {
        return Bukkit.getOnlinePlayers()
            .map { it.name }
            .filter { it.startsWith(input, ignoreCase = true) }
    }

    private fun getManualInviteTypes(sender: CommandSender): List<String> {
        val section = plugin.configFile.getConfigurationSection("invite-types") ?: return emptyList()

        return section.getKeys(false).filter { typeId ->
            val typeSection = plugin.configFile.getConfigurationSection("invite-types.$typeId")
                ?: return@filter false

            if (!typeSection.getBoolean("enabled", true)) return@filter false

            val mode = typeSection.getString("code.mode", "AUTO")!!.uppercase()
            if (mode != "MANUAL") return@filter false

            val requiresCreatorPermission = typeSection.getBoolean("settings.requires-permission-to-create", false)
            val creatorPermission = typeSection.getString("settings.creator-permission").orEmpty()

            if (!requiresCreatorPermission || creatorPermission.isBlank()) return@filter true

            sender.hasPermission(creatorPermission)
        }
    }

    private fun normalizeCode(inviteTypeId: String, code: String): String {
        val section = plugin.configFile.getConfigurationSection("invite-types.$inviteTypeId")
        val caseSensitive = section?.getBoolean(
            "code.case-sensitive",
            plugin.configFile.getBoolean("invite-code.defaults.case-sensitive", false)
        ) ?: false

        return if (caseSensitive) code else code.uppercase()
    }

    private fun findOfflinePlayer(input: String): OfflinePlayer? {
        Bukkit.getPlayerExact(input)?.let { return it }

        runCatching { UUID.fromString(input) }
            .getOrNull()
            ?.let { uuid ->
                val player = Bukkit.getOfflinePlayer(uuid)
                if (isValidOfflinePlayer(player, input)) return player
            }

        val player = Bukkit.getOfflinePlayer(input)
        return if (isValidOfflinePlayer(player, input)) player else null
    }

    private fun isValidOfflinePlayer(player: OfflinePlayer, inputName: String): Boolean {
        return player.name != null ||
                player.hasPlayedBefore() ||
                inputName.equals(player.uniqueId.toString(), ignoreCase = true)
    }

    private data class CreateCodeResult(
        val created: Boolean,
        val message: String
    )
}