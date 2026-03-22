package com.github.irvinglink.amethystLibKotlin.models.action

import com.github.irvinglink.amethystLibKotlin.core.PluginContext
import com.github.irvinglink.amethystLibKotlin.features.guis.manager.MenuManager
import com.github.irvinglink.amethystLibKotlin.utils.chat.Chat
import com.github.irvinglink.amethystLibKotlin.utils.nms.PacketUtils
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class ExecutableAction private constructor(
    private val instructions: List<ActionInstruction>
) {

    companion object {
        fun fromInstruction(rawInstruction: String): ExecutableAction {
            val instruction = ActionInstructionParser.parse(rawInstruction)
            return ExecutableAction(instruction?.let(::listOf) ?: emptyList())
        }

        fun fromInstructions(rawInstructions: List<String>): ExecutableAction {
            return ExecutableAction(ActionInstructionParser.parseAll(rawInstructions))
        }

        fun of(instructions: List<ActionInstruction>): ExecutableAction {
            return ExecutableAction(instructions)
        }
    }

    private val plugin = PluginContext.plugin
    private val chat: Chat = plugin.chat

    fun getInstructions(): List<ActionInstruction> = instructions.toList()

    fun execute(player: Player) {
        execute(player, null)
    }

    fun execute(player: Player, target: OfflinePlayer?) {
        instructions.forEach { instruction ->
            when (instruction) {
                is ActionInstruction.Message -> {
                    if (!player.isOnline) return@forEach
                    player.sendMessage(replace(player, target, instruction.text))
                }

                is ActionInstruction.Console -> {
                    runSync {
                        Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            replace(player, target, instruction.command)
                        )
                    }
                }

                is ActionInstruction.PlayerCommand -> {
                    if (!player.isOnline) return@forEach
                    runSync {
                        player.performCommand(replace(player, target, instruction.command))
                    }
                }

                is ActionInstruction.Title -> {
                    if (!player.isOnline) return@forEach
                    runSync {
                        PacketUtils.sendTitle(
                            player = player,
                            title = replace(player, target, instruction.title),
                            subtitle = instruction.subtitle?.let { replace(player, target, it) },
                            fadeIn = instruction.fadeIn,
                            stay = instruction.stay,
                            fadeOut = instruction.fadeOut
                        )
                    }
                }

                is ActionInstruction.Sound -> {
                    if (!player.isOnline) return@forEach
                    PacketUtils.playSound(
                        player = player,
                        soundName = instruction.sound,
                        volume = instruction.volume,
                        pitch = instruction.pitch
                    )
                }

                is ActionInstruction.ActionBar -> {
                    if (!player.isOnline) return@forEach
                    PacketUtils.sendActionBar(player, replace(player, target, instruction.text))
                }

                ActionInstruction.CloseMenu -> {
                    if (!player.isOnline) return@forEach
                    runSync { MenuManager.closeMenu(player) }
                }

                is ActionInstruction.OpenMenu -> {
                    if (!player.isOnline) return@forEach
                    runSync { MenuManager.openMenu(player, instruction.menuId) }
                }
            }
        }
    }

    private fun replace(player: Player, target: OfflinePlayer?, text: String): String {
        return chat.format(player, target, null, text, true)
    }

    private fun runSync(block: () -> Unit) {
        if (Bukkit.isPrimaryThread()) {
            block()
        } else {
            Bukkit.getScheduler().runTask(plugin, Runnable { block() })
        }
    }
}